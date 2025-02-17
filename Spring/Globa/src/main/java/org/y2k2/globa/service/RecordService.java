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
import org.y2k2.globa.dto.request.quiz.RequestQuizDto;
import org.y2k2.globa.dto.request.record.RequestRecordNameDto;
import org.y2k2.globa.dto.response.analysis.ResponseAnalysisDto;
import org.y2k2.globa.dto.response.analysis.ResponseRecordAnalysisDto;
import org.y2k2.globa.dto.response.folder.ResponseDetailFolderDto;
import org.y2k2.globa.dto.response.highlights.ResponseDetailHighlightDto;
import org.y2k2.globa.dto.request.kafka.RequestKafkaDto;
import org.y2k2.globa.dto.response.keyword.ResponseKeywordDto;
import org.y2k2.globa.dto.common.quiz.QuizDto;
import org.y2k2.globa.dto.response.quiz.ResponseQuizGradeDto;
import org.y2k2.globa.dto.response.record.ResponseRecordDetailDto;
import org.y2k2.globa.dto.response.record.ResponseRecordSearchDto;
import org.y2k2.globa.dto.response.record.ResponseRecordsByFolderDto;
import org.y2k2.globa.dto.response.record.ResponseRecordsDto;
import org.y2k2.globa.dto.response.section.ResponseSectionDto;
import org.y2k2.globa.dto.request.study.RequestStudyDto;
import org.y2k2.globa.dto.response.study.ResponseStudyTimesDto;
import org.y2k2.globa.dto.response.summary.ResponseDetailSummaryDto;
import org.y2k2.globa.dto.common.user.UserIntroDto;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.*;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.util.jwt.JWTProvider;
import org.y2k2.globa.util.KafkaProducer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {
    private final JWTProvider jwtTokenProvider;
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

    public ResponseRecordsByFolderDto getRecords(Long folderId, int page, int count, UserEntity user) {
        FolderEntity folder = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folder.getFolderId(), InvitationStatus.ACCEPT);
        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<RecordEntity> records = recordRepository.findAllByFolderFolderId(folderId, pageable);

        boolean isOwner = folder.getUser().getUserId().equals(user.getUserId());

        return new ResponseRecordsByFolderDto(
                records.stream().map(RecordMapper.INSTANCE::toRequestRecordDto).toList(),
                isOwner,
                records.getTotalElements()
        );
    }

    public ResponseRecordsDto getRecentRecords(int page, int count, UserEntity user) {
        Pageable pageable = PageRequest.of(page - 1, count);
        Page<RecordEntity> recordPages = recordRepository.findAllByAccessibleRecord(user, pageable);

        if (recordPages == null || recordPages.getContent().isEmpty()) {
            return new ResponseRecordsDto(new ArrayList<>(), 0L);
        }

        List<RecordEntity> records = recordPages.getContent();
        List<KeywordProjection> keywords = keywordRepository.findAllByRecordInOrderByImportanceDesc(records);

        return new ResponseRecordsDto(
                records.stream()
                        .map(record -> {
                            List<ResponseKeywordDto> responseKeywords = keywords.stream()
                                    .filter(keyword -> keyword.getRecordId().equals(record.getRecordId()))
                                    .map(keyword -> ResponseKeywordDto.builder()
                                            .word(keyword.getWord())
                                            .importance(keyword.getImportance())
                                            .build()
                                    ).toList();

                            return RecordMapper.INSTANCE.toResponseRecordDto(record, record.getFolder().getFolderId(), responseKeywords);
                        }).toList(),
                recordPages.getTotalElements());
    }

    public ResponseRecordDetailDto getRecordDetail(Long folderId, Long recordId, UserEntity user){
        RecordEntity record = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
        
        if (!record.getIsShare()) {
            boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
            if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
        }

        /*
         * 1. 문서 내 섹션 찾기
         * 2. 섹션 내 분석 찾기
         * 3. 섹션 내 하이라이트 찾기
         * 4. 섹션 내 요약 찾기
         * */
        List<SectionEntity> sections = sectionRepository.findAllByRecordOrderByStartTimeAsc(record);
        List<AnalysisEntity> analyses = analysisRepository.findALlBySectionIn(sections);
        List<HighlightEntity> highlights = highlightRepository.findAllBySectionIn(sections);
        List<SummaryEntity> summaries = summaryRepository.findAllBySectionIn(sections);
        List<ResponseSectionDto> responseSections = new ArrayList<>();

        for (SectionEntity section : sections) {
            AnalysisEntity analysisToSection = analyses.stream()
                    .filter(analysis -> analysis.getSection().getSectionId().equals(section.getSectionId()))
                    .findFirst()
                    .orElse(null);
            List<HighlightEntity> highlightToSection = highlights.stream()
                    .filter(highlight -> highlight.getSection().getSectionId().equals(section.getSectionId()))
                    .toList();
            List<SummaryEntity> summaryToSection = summaries.stream()
                    .filter(summary -> summary.getSection().getSectionId().equals(section.getSectionId()))
                    .toList();

            List<ResponseDetailHighlightDto> responseHighlights = highlightToSection.stream()
                    .map(HighlightMapper.INSTANCE::toResponseDetailHighlightDto)
                    .toList();
            ResponseRecordAnalysisDto responseAnalysis = AnalysisMapper.INSTANCE.toResponseRecordAnalysisDto(analysisToSection, responseHighlights);
            List<ResponseDetailSummaryDto> responseSummaries = summaryToSection.stream()
                    .map(SummaryMapper.INSTANCE::toResponseDetailSummaryDto)
                    .toList();

            responseSections.add(SectionMapper.INSTANCE.toResponseDetailSummaryDto(section, responseAnalysis, responseSummaries));
        }

        ResponseDetailFolderDto folder = FolderMapper.INSTANCE.toResponseDetailFolderDto(record.getFolder());
        return RecordMapper.INSTANCE.toResponseRecordDetailDto(record, folder, responseSections);
    }

    public ResponseAnalysisDto getAnalysis(Long recordId, Long folderId, UserEntity user) {
        boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        List<StudyEntity> studies = studyRepository.findAllByUserAndRecordRecordId(user, recordId);
        List<QuizGradeProjection> quizzes = quizRepository.findQuizGradeByUserAndRecordRecordId(user, recordId);
        List<KeywordProjection> keywords = keywordRepository.findAllByRecordId(recordId);

        List<ResponseStudyTimesDto> responseStudyTimes = studies.stream()
                .map(StudyTimeMapper.INSTANCE::toResponseStudyTimesDto)
                .toList();
        List<ResponseQuizGradeDto> responseQuizGrades = quizzes.stream()
                .map(QuizMapper.INSTANCE::toResponseQuizGradeDto)
                .toList();
        List<ResponseKeywordDto> responseKeywords = keywords.stream()
                .map(KeywordMapper.INSTANCE::toResponseKeywordDto)
                .toList();

        return new ResponseAnalysisDto(responseKeywords, responseStudyTimes, responseQuizGrades);
    }

    public ResponseRecordSearchDto searchRecord(String keyword, int page, int count, UserEntity user) {
        Page<RecordSearchProjection> recordEntities = recordRepository.findAllSharedOrOwnedRecords(
                user,
                keyword,
                PageRequest.of(page - 1, count)
        );

        return new ResponseRecordSearchDto(recordEntities.stream()
                .map(record -> {
                    UserIntroDto uploader = new UserIntroDto(record.getUserId(), record.getProfilePath(), record.getName());
                    return RecordMapper.INSTANCE.toResponseRecordSearch(record.getFolderId(), record, uploader);
                }).toList(),
                recordEntities.getTotalElements());
    }

    public ResponseRecordsDto getReceivingRecords(int page, int count, UserEntity user) {
        Pageable pageable = PageRequest.of(page - 1, count);
        Page<RecordEntity> records = recordRepository.findReceivingRecordsByUserOrderByCreatedTimeDesc(user, pageable);
        return getResponseRecordsDto(records.getContent(), records.getTotalElements());
    }

    public ResponseRecordsDto getSharingRecords(int page, int count, UserEntity user) {
        Pageable pageable = PageRequest.of(page - 1, count);
        Page<RecordEntity> records = recordRepository.findSharingRecordsByUserOrderByCreatedTimeDesc(user, pageable);
        return getResponseRecordsDto(records.getContent(), records.getTotalElements());
    }

    private ResponseRecordsDto getResponseRecordsDto(List<RecordEntity> records, Long total) {
        if (records.isEmpty()) {
            return new ResponseRecordsDto(new ArrayList<>(), 0L);
        }

        List<KeywordProjection> keywords = keywordRepository.findAllByRecordInOrderByImportanceDesc(records);

        return new ResponseRecordsDto(
                records.stream()
                        .map(record -> {
                            List<ResponseKeywordDto> responseKeywords = keywords.stream()
                                    .filter(keyword -> keyword.getRecordId().equals(record.getRecordId()))
                                    .map(KeywordMapper.INSTANCE::toResponseKeywordDto)
                                    .toList();

                            return RecordMapper.INSTANCE.toResponseRecordDto(
                                    record,
                                    record.getFolder().getFolderId(),
                                    responseKeywords
                            );
                        }).toList(),
                total
        );
    }

    @Transactional
    public void changeLinkShare(Long folderId, Long recordId, boolean isShare, UserEntity user){
        FolderEntity folder = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));
        RecordEntity record = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!folder.getUser().getUserId().equals(user.getUserId())) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        if (!record.getUser().getUserId().equals(user.getUserId())) throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);

        record.setIsShare(isShare);
        recordRepository.save(record);
    }

    @Transactional
    public HttpStatus postRecord(String accessToken, Long folderId, String title, String path, String size){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity userEntity = userRepository.findByUserId(userId);

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));;

        boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity, folderEntity.getFolderId(), InvitationStatus.ACCEPT);
        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

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

    @Transactional
    public void modifyRecordName(Long folderId, Long recordId, RequestRecordNameDto dto, UserEntity user) {
        RecordEntity record = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        if (!user.getUserId().equals(record.getUser().getUserId())) {
            throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
        }

        record.setTitle(dto.title());
        recordRepository.save(record);
    }

    @Transactional
    public HttpStatus patchRecordMove(String accessToken, Long recordId, Long folderId, Long targetId){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        RecordEntity recordEntity = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ORIGIN_FOLDER));
        FolderEntity targetEntity = folderRepository.findFirstByFolderId(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TARGET_FOLDER));

        boolean hasAccessFromFolder = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity, folderId, InvitationStatus.ACCEPT);
        if (!hasAccessFromFolder) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        boolean hasAccessToFolder = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity, targetId, InvitationStatus.ACCEPT);
        if (!hasAccessToFolder) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

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

    @Transactional
    public void patchStudyTime(String accessToken, Long recordId, Long folderId, RequestStudyDto dto) {
        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        UserEntity user = userRepository.findByUserId(userId);
        RecordEntity record = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
        Boolean existsByFolderShare = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);

        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
        if (!record.getFolder().getFolderId().equals(folderId)) throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
        if (!existsByFolderShare) throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);

        StudyEntity study = studyRepository.findByCreatedTime(userId, recordId)
                .orElse(StudyEntity.builder()
                        .user(user)
                        .record(record)
                        .build());

        if (study.getStudyTime() != null) {
            study.setStudyTime(study.getStudyTime() + dto.getStudyTime());
        } else {
            study.setStudyTime(dto.getStudyTime());
        }

        studyRepository.save(study);
    }

    @Transactional
    public HttpStatus deleteRecord(String accessToken, Long recordId, Long folderId){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        RecordEntity recordEntity = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        FolderShareEntity folderShareEntities = folderShareRepository.findByFolderAndTargetUser(recordEntity.getFolder(), userEntity);
        if (folderShareEntities == null) { throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER); }
        if (!folderShareEntities.getInvitationStatus().equals(InvitationStatus.ACCEPT.toString())) { throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER); }

        if(!Objects.equals(folderId, recordEntity.getFolder().getFolderId())) { throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER); }

        Blob blob = bucket.get(recordEntity.getPath());
        if( blob != null ) {
            blob.delete();
        }

        recordRepository.delete(recordEntity);

        return HttpStatus.OK;
    }
}
