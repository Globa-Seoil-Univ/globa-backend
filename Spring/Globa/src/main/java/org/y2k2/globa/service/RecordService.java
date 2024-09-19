package org.y2k2.globa.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.Projection.RecordSearchProjection;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.QuizMapper;
import org.y2k2.globa.mapper.RecordMapper;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.util.CustomTimestamp;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.KafkaProducer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final JwtTokenProvider jwtTokenProvider;
    private final KafkaProducer kafkaProducer;

    public final UserRepository userRepository;;
    public final StudyRepository studyRepository;
    public final SurveyRepository surveyRepository;
    public final FolderRepository folderRepository;
    public final RecordRepository recordRepository;
    public final FolderShareRepository folderShareRepository;
    public final FolderRoleRepository folderRoleRepository;
    public final SectionRepository sectionRepository;
    public final AnalysisRepository analysisRepository;
    public final HighlightRepository highlightRepository;
    public final SummaryRepository summaryRepository;
    public final QuizRepository quizRepository;
    public final QuizAttemptRepository quizAttemptRepository;
    public final KeywordRepository keywordRepository;

    @Autowired
    private Bucket bucket;

    @Value("${firebase.bucket-path}")
    private String firebaseBucketPath;
    @Value("${kafka.topic.audio}")
    private String topic;
    @Value("${kafka.topic.audio.key}")
    private String topicKey;


    public ResponseRecordsByFolderDto getRecords(String accessToken, Long folderId, int page, int count){

        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity, folderId,"ACCEPT");

        if(folderShareEntity == null)
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        Pageable pageable = PageRequest.of(page-1, count);
        Page<RecordEntity> records = recordRepository.findAllByFolderFolderId(pageable, folderId);


        return new ResponseRecordsByFolderDto(records.stream()
                .map(RecordMapper.INSTANCE::toRequestRecordDto)
                .collect(Collectors.toList()), (int) records.getTotalElements());

    }

    public ResponseAllRecordWithTotalDto getAllRecords(String accessToken, int count){

        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
        System.out.println(userId);
        List<FolderShareEntity> folderShareEntities = folderShareRepository.findFolderShareEntitiesByTargetUserAndInvitationStatus(userEntity, "ACCEPT");

        if(folderShareEntities == null)
            throw new CustomException(ErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER);

        List<Long> folderIds = new ArrayList<>();

        for(FolderShareEntity folderShareEntity : folderShareEntities){
            folderIds.add(folderShareEntity.getFolder().getFolderId());
        }

        Pageable pageable = PageRequest.of(0, count);
        Page<RecordEntity> records = recordRepository.findRecordEntitiesByFolder(pageable,folderIds);

        return new ResponseAllRecordWithTotalDto(records.stream()
                .map(record -> {
                    List<KeywordProjection> keywordProjectionList = userRepository.findKeywordByRecordId(record.getRecordId());
                    List<ResponseKeywordDto> keywords = new ArrayList<>();
                    for( KeywordProjection keywordProjection : keywordProjectionList ){
                        ResponseKeywordDto responseKeywordDto = new ResponseKeywordDto();
                        responseKeywordDto.setWord(keywordProjection.getWord());
                        responseKeywordDto.setImportance(keywordProjection.getImportance());
                        keywords.add(responseKeywordDto);
                    }

                   return RecordMapper.INSTANCE.toResponseAllRecordDto(record, recordRepository.findRecordEntityByRecordId(record.getRecordId()).getFolder().getFolderId(),keywords);

                })
                .collect(Collectors.toList()), (int) records.getTotalElements());

    }

    public ResponseRecordDetailDto getRecordDetail(String accessToken, Long folderId, Long recordId){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        RecordEntity record = recordRepository.findRecordEntityByRecordId(recordId);

        if (record == null) throw new CustomException(ErrorCode.NOT_FOUND_RECORD);

        if (!record.getIsShare()) {
            FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity, folderId,"ACCEPT");
            if(folderShareEntity == null)
                throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
        }

        ResponseRecordDetailDto responseDto = new ResponseRecordDetailDto();

        List<SectionEntity> sections = sectionRepository.findAllByRecordRecordIdOrderByStartTimeAsc(recordId);
        List<ResponseSectionDto> responseSections = new ArrayList<>();

        for(SectionEntity section : sections){
            ResponseSectionDto responseSectionDto = new ResponseSectionDto();

            responseSectionDto.setSectionId(section.getSectionId());
            responseSectionDto.setTitle(section.getTitle());
            responseSectionDto.setStartTime(section.getStartTime());
            responseSectionDto.setEndTime(section.getEndTime());
            responseSectionDto.setCreatedTime(section.getCreatedTime());

            AnalysisEntity analysis = analysisRepository.findAllBySectionSectionId(section.getSectionId());

            if(analysis == null)
                throw new CustomException(ErrorCode.NOT_FOUND_ANALYSIS);

            ResponseRecordAnalysisDto responseRecordAnalysisDto = new ResponseRecordAnalysisDto();
            responseRecordAnalysisDto.setAnalysisId(analysis.getAnalysisId());
            responseRecordAnalysisDto.setContent(analysis.getContent());
            List<HighlightEntity> highlights = highlightRepository.findAllBySectionSectionId(section.getSectionId());
            List<ResponseDetailHighlightDto> responseHighlights = new ArrayList<>();

            for(HighlightEntity highlight : highlights){
                ResponseDetailHighlightDto rdhd = new ResponseDetailHighlightDto();
                rdhd.setHighlightId(highlight.getHighlightId());
                rdhd.setType(highlight.getType());
                rdhd.setStartIndex(highlight.getStartIndex());
                rdhd.setEndIndex(highlight.getEndIndex());

                responseHighlights.add(rdhd);
            }

            responseRecordAnalysisDto.setHighlights(responseHighlights);
            responseSectionDto.setAnalysis(responseRecordAnalysisDto);

            List<ResponseDetailSummaryDto> responseSummaries = new ArrayList<>();

            for(SummaryEntity summaryEntity : summaryRepository.findAllBySectionSectionId(section.getSectionId())){
                ResponseDetailSummaryDto rdsd = new ResponseDetailSummaryDto();
                rdsd.setContent(summaryEntity.getContent());

                responseSummaries.add(rdsd);
            }

            responseSectionDto.setSummary(responseSummaries);
            responseSections.add(responseSectionDto);
        }

        ResponseDetailFolderDto rdfd = new ResponseDetailFolderDto();
        rdfd.setTitle(record.getFolder().getTitle());
        rdfd.setCreatedTime(record.getFolder().getCreatedTime());

        responseDto.setRecordId(record.getRecordId());
        responseDto.setTitle(record.getTitle());
        responseDto.setPath(record.getPath());
        responseDto.setSize(record.getSize());
        responseDto.setFolder(rdfd);
        responseDto.setSection(responseSections);
        responseDto.setCreatedTime(record.getCreatedTime());

        return responseDto;

    }

    public ResponseAnalysisDto getAnalysis(String accessToken, Long recordId, Long folderId){

        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity targetEntity = userRepository.findByUserId(userId);

        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(targetEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(targetEntity,folderId,"ACCEPT");
        if (folderShareEntity == null) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        List<StudyEntity> studyEntities = studyRepository.findAllByUserUserIdAndRecordRecordId(userId,recordId);
        List<QuizGradeProjection> quizGradeProjectionList = analysisRepository.findQuizGradeByUserUserIdAndRecordRecordId(userId, recordId);
        List<KeywordProjection> keywordProjectionList = analysisRepository.findKeywordByRecordId(recordId);

        ResponseAnalysisDto responseAnalysisDto = new ResponseAnalysisDto();
        List<ResponseStudyTimesDto> studyTimes = new ArrayList<>();
        List<ResponseQuizGradeDto> quizGrades = new ArrayList<>();
        List<ResponseKeywordDto> keywords = new ArrayList<>();

        for( StudyEntity studyEntitiy : studyEntities ){
            CustomTimestamp timestamp = new CustomTimestamp();
            timestamp.setTimestamp(studyEntitiy.getCreatedTime());

            ResponseStudyTimesDto responseStudyTimesDto = new ResponseStudyTimesDto();
            responseStudyTimesDto.setStudyTime(studyEntitiy.getStudyTime());
            responseStudyTimesDto.setCreatedTime(timestamp.toString());
            studyTimes.add(responseStudyTimesDto);
        }

        for( QuizGradeProjection quizGradeProjection : quizGradeProjectionList ){
            ResponseQuizGradeDto responseQuizGradeDto = new ResponseQuizGradeDto();
            responseQuizGradeDto.setQuizGrade(quizGradeProjection.getQuizGrade());
            responseQuizGradeDto.setCreatedTime(quizGradeProjection.getCreatedTime());
            quizGrades.add(responseQuizGradeDto);
        }

        for( KeywordProjection keywordProjection : keywordProjectionList ){
            ResponseKeywordDto responseKeywordDto = new ResponseKeywordDto();
            responseKeywordDto.setWord(keywordProjection.getWord());
            responseKeywordDto.setImportance(keywordProjection.getImportance());
            keywords.add(responseKeywordDto);
        }

        responseAnalysisDto.setStudyTimes(studyTimes);
        responseAnalysisDto.setQuizGrades(quizGrades);
        responseAnalysisDto.setKeywords(keywords);
        return responseAnalysisDto;
    }

    public List<QuizDto> getQuiz(String accessToken, Long recordId, Long folderId){

        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity targetEntity = userRepository.findByUserId(userId);

        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(targetEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(targetEntity,folderId,"ACCEPT");
        if (folderShareEntity == null) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        List<QuizEntity> quizEntities = quizRepository.findAllByRecordRecordId(recordId);
        if(quizEntities.isEmpty())
            throw new CustomException(ErrorCode.NOT_FOUND_QUIZ);

        return quizEntities.stream()
                .map(QuizMapper.INSTANCE::toQuizDto)
                .collect(Collectors.toList());
    }

    public ResponseRecordSearchDto searchRecord(String accessToken, String keyword, int page, int count) {
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        Page<RecordSearchProjection> recordEntities = recordRepository.findAllSharedOrOwnedRecords(PageRequest.of(page - 1, count), userEntity.getUserId(), keyword);

        return new ResponseRecordSearchDto(recordEntities.stream()
                .map(record -> {
                    UserIntroDto uploader = new UserIntroDto(record.getUserId(), record.getProfilePath(), record.getName());
                    return RecordMapper.INSTANCE.toResponseRecordSearch(record.getFolderId(), record, uploader);
                })
                .toList(), recordEntities.getTotalElements());
    }

    public ResponseAllRecordWithTotalDto getReceivingRecords(String accessToken, int page, int count) {
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<RecordEntity> records = recordRepository.findReceivingRecordsByUserId(pageable, userId);

        return new ResponseAllRecordWithTotalDto(records.stream()
                .map(record -> {
                    List<KeywordEntity> keywords = keywordRepository.findAllByRecord(record);
                    List<ResponseKeywordDto> responseKeywordDto = keywords.stream().map(keyword -> ResponseKeywordDto.builder()
                            .word(keyword.getWord())
                            .importance(keyword.getImportance().doubleValue())
                            .build()
                    ).toList();

                    return RecordMapper.INSTANCE.toResponseAllRecordDto(record, recordRepository.findRecordEntityByRecordId(record.getRecordId()).getFolder().getFolderId(), responseKeywordDto);
                })
                .collect(Collectors.toList()), (int) records.getTotalElements());
    }

    public ResponseAllRecordWithTotalDto getSharingRecords(String accessToken, int page, int count) {
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<RecordEntity> records = recordRepository.findSharingRecordsByUserId(pageable, userId);

        return new ResponseAllRecordWithTotalDto(records.stream()
                .map(record -> {
                    List<KeywordEntity> keywords = keywordRepository.findAllByRecord(record);
                    List<ResponseKeywordDto> responseKeywordDto = keywords.stream().map(keyword -> ResponseKeywordDto.builder()
                            .word(keyword.getWord())
                            .importance(keyword.getImportance().doubleValue())
                            .build()
                    ).toList();

                    return RecordMapper.INSTANCE.toResponseAllRecordDto(record, recordRepository.findRecordEntityByRecordId(record.getRecordId()).getFolder().getFolderId(), responseKeywordDto);
                })
                .collect(Collectors.toList()), (int) records.getTotalElements());
    }

    public HttpStatus postQuiz(String accessToken, Long recordId, Long folderId, List<RequestQuizDto> quizs){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity targetEntity = userRepository.findByUserId(userId);

        if (targetEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(targetEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(targetEntity,folderId,"ACCEPT");
        if (folderShareEntity == null) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        RecordEntity recordEntity = recordRepository.findRecordEntityByRecordId(recordId);

        for(RequestQuizDto requestQuizDto : quizs){

            if( requestQuizDto.getQuizId() == null)
                throw new CustomException(ErrorCode.REQUIRED_QUIZ_ID);

            QuizEntity quizEntity = quizRepository.findQuizEntityByQuizId(requestQuizDto.getQuizId());

            if(!Objects.equals(recordEntity.getRecordId(), quizEntity.getRecord().getRecordId()))
                throw new CustomException(ErrorCode.MISMATCH_QUIZ_RECORD_ID);

            QuizAttemptEntity quizAttemptEntity = new QuizAttemptEntity();
            quizAttemptEntity.setUser(targetEntity);
            quizAttemptEntity.setQuiz(quizRepository.findQuizEntityByQuizId(requestQuizDto.getQuizId()));
            quizAttemptEntity.setCorrect(requestQuizDto.isCorrect());
            quizAttemptEntity.setCreatedTime(LocalDateTime.now());

            quizAttemptRepository.save(quizAttemptEntity);
        }

        return HttpStatus.CREATED;
    }

    public HttpStatus postRecord(String accessToken, Long folderId, String title, String path, String size){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);
        if (folderEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity,folderId,"ACCEPT");        if (folderShareEntity == null)
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        RecordEntity recordEntity = new RecordEntity();
        recordEntity.setCreatedTime(LocalDateTime.now());
        recordEntity.setSize(size);
        recordEntity.setPath(path);
        recordEntity.setTitle(title);
        recordEntity.setUser(userEntity);
        recordEntity.setFolder(folderEntity);

        Blob blob = bucket.get(path);
        if( blob == null )
            throw new CustomException(ErrorCode.NOT_FOUND_RECORD_FIREBASE);


        recordRepository.save(recordEntity);

        kafkaProducer.send(topic, topicKey, new RequestKafkaDto(recordEntity.getRecordId(), userEntity.getUserId()));

        return HttpStatus.CREATED;
    }

    public HttpStatus patchRecordName(String accessToken, Long recordId, Long folderId, String title){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        RecordEntity recordEntity = recordRepository.findRecordEntityByRecordId(recordId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity,folderId,"ACCEPT");
        if (folderShareEntity == null)
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        if(recordEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_RECORD);

        if (!Objects.equals(userId, recordEntity.getUser().getUserId())){
            throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
        }


        recordEntity.setTitle(title);

        recordRepository.save(recordEntity);


        return HttpStatus.OK;
    }

    @Transactional(rollbackFor = Exception.class)
    public HttpStatus patchRecordMove(String accessToken, Long recordId, Long folderId, Long targetId){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        RecordEntity recordEntity = recordRepository.findRecordEntityByRecordId(recordId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity folderEntity = folderRepository.findFolderEntityByFolderId(folderId);
        FolderEntity targetEntity = folderRepository.findFolderEntityByFolderId(targetId);

        FolderShareEntity folderShareEntity1 = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity,folderId,"ACCEPT");
        FolderShareEntity folderShareEntity2 = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity,targetId,"ACCEPT");

        if (folderEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_ORIGIN_FOLDER);

        if (targetEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_TARGET_FOLDER);

        if (folderShareEntity1 == null)
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        if (folderShareEntity2 == null)
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        if(recordEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_RECORD);

        if (!Objects.equals(userId, recordEntity.getUser().getUserId())){
            throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
        }

        // Update local database
        String oldPath = recordEntity.getPath();
        String paramPath = "folders/" + folderId + oldPath.substring(oldPath.lastIndexOf("/"));
        String newPath = "folders/" + targetId + oldPath.substring(oldPath.lastIndexOf("/"));
        recordEntity.setFolder(targetEntity);
        recordEntity.setPath(newPath);
        recordRepository.save(recordEntity);

        if(!oldPath.equals(paramPath))
            throw new CustomException(ErrorCode.NOT_FOUND_RECORD);

        // Now move the file in Firebase
        Blob blob = bucket.get(oldPath);
        if (blob != null) {
            try {
                blob.copyTo(BlobId.of(firebaseBucketPath, newPath));
                blob.delete();
            } catch (Exception e) {
                // Rollback local DB changes if Firebase operation fails
                recordEntity.setFolder(folderEntity);
                recordEntity.setPath(oldPath);
                recordRepository.save(recordEntity);
                throw new CustomException(ErrorCode.FAILED_FIREBASE);
            }
        } else {
            throw new CustomException(ErrorCode.NOT_FOUND_RECORD_FIREBASE);
        }

        return HttpStatus.OK;
    }

    public void patchStudyTime(String accessToken, Long recordId, Long folderId, RequestStudyDto dto) {
        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        UserEntity user = userRepository.findByUserId(userId);
        RecordEntity record = recordRepository.findRecordEntityByRecordId(recordId);
        Boolean existsByFolderShare = folderShareRepository.existsByFolderAndInvitationStatusAndTargetUserOrOwnerUser(record.getFolder(), InvitationStatus.ACCEPT.toString(), user, user);

        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
        if (!record.getFolder().getFolderId().equals(folderId)) throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
        if (!existsByFolderShare) throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);

        LocalDateTime dateTime = CustomTimestamp.toLocalDateTime(dto.getCreatedTime());
        StudyEntity study = studyRepository.findByCreatedTime(dateTime)
                .orElseGet(() -> StudyEntity.builder()
                        .user(user)
                        .record(record)
                        .build()
                );

        study.setStudyTime(dto.getStudyTime());

        studyRepository.save(study);
    }

    public void changeLinkShare(String accessToken, Long folderId, Long recordId, boolean isShare){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity user = userRepository.findByUserId(userId);
        FolderEntity folder = folderRepository.findFirstByFolderId(folderId);
        RecordEntity record = recordRepository.findRecordEntityByRecordId(recordId);

        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
        if (folder == null) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);
        if (record == null) throw new CustomException(ErrorCode.NOT_FOUND_RECORD);
        if (!Objects.equals(folder.getUser().getUserId(), user.getUserId())) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        if (!Objects.equals(record.getUser().getUserId(), user.getUserId())) throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);

        record.setIsShare(isShare);
        recordRepository.save(record);
    }

    public HttpStatus deleteRecord(String accessToken, Long recordId, Long folderId){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        RecordEntity recordEntity = recordRepository.findRecordEntityByRecordId(recordId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        if(recordEntity == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_RECORD);
        }

        if(!Objects.equals(folderId, recordEntity.getFolder().getFolderId()))
            throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);

        if (!Objects.equals(userId, recordEntity.getUser().getUserId())){
            throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
        }


        Blob blob = bucket.get(recordEntity.getPath());
        if( blob == null )
            throw new CustomException(ErrorCode.NOT_FOUND_RECORD_FIREBASE);
        else {
            blob.delete();
            recordRepository.delete(recordEntity);
        }

        return HttpStatus.OK;
    }
}
