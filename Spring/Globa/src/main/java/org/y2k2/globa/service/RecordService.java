package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.common.file.FileDto;
import org.y2k2.globa.dto.request.kafka.RequestKafkaDto;
import org.y2k2.globa.dto.request.record.RequestPostRecordDto;
import org.y2k2.globa.projection.KeywordProjection;
import org.y2k2.globa.projection.QuizGradeProjection;
import org.y2k2.globa.projection.RecordSearchProjection;
import org.y2k2.globa.annotation.FileCleanup;
import org.y2k2.globa.dto.request.record.RequestRecordMoveDto;
import org.y2k2.globa.dto.request.record.RequestRecordNameDto;
import org.y2k2.globa.dto.response.analysis.ResponseAnalysisDto;
import org.y2k2.globa.dto.response.analysis.ResponseRecordAnalysisDto;
import org.y2k2.globa.dto.response.folder.ResponseDetailFolderDto;
import org.y2k2.globa.dto.response.highlights.ResponseDetailHighlightDto;
import org.y2k2.globa.dto.response.keyword.ResponseKeywordDto;
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
import org.y2k2.globa.exception.FileUploadException;
import org.y2k2.globa.mapper.*;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.FolderRole;
import org.y2k2.globa.util.file.FileStore;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {
    private final FileStore fileStore;

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
    public void createdRecord(Long folderId, RequestPostRecordDto dto, UserEntity user){
        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));;

        boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderEntity.getFolderId(), InvitationStatus.ACCEPT);
        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        FileDto file = fileStore.getFile(dto.path())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD_FIREBASE));
        RecordEntity record = RecordMapper.INSTANCE.toEntity(dto, folderEntity, user, file.size());

        recordRepository.save(record);

        // TODO : Kafka를 통해 분석 요청 (Front한테 넘길 떄 주석 해제)
        // kafkaProducer.send(topic, topicKey, new RequestKafkaDto(recordEntity.getRecordId(), user.getUserId()));
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
    @FileCleanup
    public void moveRecord(Long folderId, Long recordId, RequestRecordMoveDto dto, UserEntity user) {
        RecordEntity record = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
        FolderEntity target = folderRepository.findFirstByFolderId(dto.targetId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TARGET_FOLDER));

        boolean hasAccessFromFolder = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
        if (!hasAccessFromFolder) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        boolean hasAccessToFolder = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, dto.targetId(), InvitationStatus.ACCEPT);
        if (!hasAccessToFolder) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        if (!record.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
        } else if (!record.getFolder().getFolderId().equals(folderId)) {
            throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
        }

        String oldPath = record.getPath();
        String newPath = "folders/" + target.getFolderId() + oldPath.substring(oldPath.lastIndexOf("/"));
        fileStore.moveFile(oldPath, newPath);

        try {
            record.setFolder(target);
            record.setPath(newPath);
            recordRepository.save(record);
        } catch (Exception e) {
            throw new FileUploadException(newPath);
        }

        fileStore.deleteFile(oldPath);
    }

    @Transactional
    public void modifyStudyTime(Long folderId, Long recordId, RequestStudyDto dto, UserEntity user) {
        RecordEntity record = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
        if (!record.getFolder().getFolderId().equals(folderId)) throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);

        Boolean existsByFolderShare = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
        if (!existsByFolderShare) throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);

        StudyEntity study = studyRepository.findByCreatedTime(user, record)
                .orElse(
                        StudyEntity.builder()
                            .user(user)
                            .record(record)
                            .build()
                );

        if (study.getStudyTime() != null) {
            study.setStudyTime(study.getStudyTime() + dto.studyTime());
        } else {
            study.setStudyTime(dto.studyTime());
        }

        studyRepository.save(study);
    }

    @Transactional
    public void deleteRecord(Long folderId, Long recordId, UserEntity user){
        RecordEntity record = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatusAndRole_RoleName(
                user,
                folderId,
                InvitationStatus.ACCEPT,
                FolderRole.OWNER.getRoleName()
        );
        if (!hasAccess) {
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
        }
        if(!record.getFolder().getFolderId().equals(folderId)) {
            throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
        }

        recordRepository.delete(record);
        fileStore.deleteFile(record.getPath());
    }
}
