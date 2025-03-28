//package org.y2k2.globa.application.record.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.y2k2.globa.application.analysis.mapper.AnalysisMapper;
//import org.y2k2.globa.application.folder.mapper.FolderMapper;
//import org.y2k2.globa.application.hightlight.mapper.HighlightMapper;
//import org.y2k2.globa.application.keyword.mapper.KeywordMapper;
//import org.y2k2.globa.application.quiz.mapper.QuizMapper;
//import org.y2k2.globa.application.record.mapper.RecordMapper;
//import org.y2k2.globa.application.section.mapper.SectionMapper;
//import org.y2k2.globa.application.studytime.mapper.StudyTimeMapper;
//import org.y2k2.globa.application.summary.mapper.SummaryMapper;
//import org.y2k2.globa.domain.analysis.repository.AnalysisRepository;
//import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
//import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
//import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
//import org.y2k2.globa.infrastructure.persistence.highlight.repository.HighlightJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.quizattemp.repository.QuizAttemptJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
//import org.y2k2.globa.infrastructure.persistence.record.repository.RecordJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
//import org.y2k2.globa.infrastructure.persistence.section.repository.SectionJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
//import org.y2k2.globa.infrastructure.persistence.study.repository.StudyJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;
//import org.y2k2.globa.infrastructure.persistence.summary.repository.SummaryJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.survey.repository.SurveyJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
//import org.y2k2.globa.application.common.dto.file.FileDto;
//import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
//import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
//import org.y2k2.globa.infrastructure.persistence.quiz.projection.QuizGradeProjection;
//import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjection;
//import org.y2k2.globa.common.annotation.FileCleanup;
//import org.y2k2.globa.application.record.dto.request.RequestRecordMoveDto;
//import org.y2k2.globa.application.record.dto.request.RequestRecordNameDto;
//import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
//import org.y2k2.globa.application.analysis.dto.response.ResponseRecordAnalysisDto;
//import org.y2k2.globa.application.folder.dto.response.ResponseDetailFolderDto;
//import org.y2k2.globa.application.hightlight.dto.response.ResponseDetailHighlightDto;
//import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;
//import org.y2k2.globa.application.quiz.dto.response.ResponseQuizGradeDto;
//import org.y2k2.globa.application.record.dto.response.ResponseRecordDetailDto;
//import org.y2k2.globa.application.record.dto.response.ResponseRecordSearchDto;
//import org.y2k2.globa.application.record.dto.response.ResponseRecordsByFolderDto;
//import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
//import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
//import org.y2k2.globa.application.studytime.dto.request.RequestStudyDto;
//import org.y2k2.globa.application.studytime.dto.response.ResponseStudyTimesDto;
//import org.y2k2.globa.application.summary.dto.response.ResponseDetailSummaryDto;
//import org.y2k2.globa.application.user.dto.common.UserIntroDto;
//import org.y2k2.globa.common.exception.CustomException;
//import org.y2k2.globa.common.exception.ErrorCode;
//import org.y2k2.globa.common.exception.FileUploadException;
//import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
//import org.y2k2.globa.common.type.FolderRole;
//import org.y2k2.globa.common.util.file.FileStore;
//import org.y2k2.globa.infrastructure.persistence.user.repository.UserJpaRepository;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class RecordService {
//    private final FileStore fileStore;
//
//    public final UserJpaRepository userJpaRepository;;
//    public final StudyJpaRepository studyJpaRepository;
//    public final SurveyJpaRepository surveyJpaRepository;
//    public final FolderJpaRepository folderJpaRepository;
//    public final RecordJpaRepository recordJpaRepository;
//    public final FolderShareJpaRepository folderShareJpaRepository;
//    public final FolderRoleJpaRepository folderRoleJpaRepository;
//    public final SectionJpaRepository sectionJpaRepository;
//    public final AnalysisRepository analysisRepository;
//    public final HighlightJpaRepository highlightJpaRepository;
//    public final SummaryJpaRepository summaryJpaRepository;
//    public final QuizJpaRepository quizJpaRepository;
//    public final QuizAttemptJpaRepository quizAttemptJpaRepository;
//    public final KeywordJpaRepository keywordJpaRepository;
//
//    public ResponseRecordsByFolderDto getRecords(Long folderId, int page, int count, UserEntity user) {
//        FolderEntity folder = folderJpaRepository.findFirstByFolderId(folderId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));
//
//        boolean hasAccess = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folder.getFolderId(), InvitationStatus.ACCEPT);
//        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//
//        Pageable pageable = PageRequest.of(page - 1, count);
//        Page<RecordEntity> records = recordJpaRepository.findAllByFolderFolderId(folderId, pageable);
//
//        boolean isOwner = folder.getUser().getUserId().equals(user.getUserId());
//
//        return new ResponseRecordsByFolderDto(
//                records.stream().map(RecordMapper.INSTANCE::toRequestRecordDto).toList(),
//                isOwner,
//                records.getTotalElements()
//        );
//    }
//
//    public ResponseRecordsDto getRecentRecords(int page, int count, UserEntity user) {
//        Pageable pageable = PageRequest.of(page - 1, count);
//        Page<RecordEntity> recordPages = recordJpaRepository.findAllByAccessibleRecord(user, pageable);
//
//        if (recordPages == null || recordPages.getContent().isEmpty()) {
//            return new ResponseRecordsDto(new ArrayList<>(), 0L);
//        }
//
//        List<RecordEntity> records = recordPages.getContent();
//        List<KeywordProjection> keywords = keywordJpaRepository.findAllByRecordInOrderByImportanceDesc(records);
//
//        return new ResponseRecordsDto(
//                records.stream()
//                        .map(record -> {
//                            List<ResponseKeywordDto> responseKeywords = keywords.stream()
//                                    .filter(keyword -> keyword.getRecordId().equals(record.getRecordId()))
//                                    .map(keyword -> ResponseKeywordDto.builder()
//                                            .word(keyword.getWord())
//                                            .importance(keyword.getImportance())
//                                            .build()
//                                    ).toList();
//
//                            return RecordMapper.INSTANCE.toResponseRecordDto(record, record.getFolder().getFolderId(), responseKeywords);
//                        }).toList(),
//                recordPages.getTotalElements());
//    }
//
//    public ResponseRecordDetailDto getRecordDetail(Long folderId, Long recordId, UserEntity user){
//        RecordEntity record = recordJpaRepository.findByRecordId(recordId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
//
//        if (!record.getIsShare()) {
//            boolean hasAccess = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
//            if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//        }
//
//        /*
//         * 1. 문서 내 섹션 찾기
//         * 2. 섹션 내 분석 찾기
//         * 3. 섹션 내 하이라이트 찾기
//         * 4. 섹션 내 요약 찾기
//         * */
//        List<SectionEntity> sections = sectionJpaRepository.findAllByRecordOrderByStartTimeAsc(record);
//        List<AnalysisEntity> analyses = analysisRepository.findALlBySectionIn(sections);
//        List<HighlightEntity> highlights = highlightJpaRepository.findAllBySectionIn(sections);
//        List<SummaryEntity> summaries = summaryJpaRepository.findAllBySectionIn(sections);
//        List<ResponseSectionDto> responseSections = new ArrayList<>();
//
//        for (SectionEntity section : sections) {
//            AnalysisEntity analysisToSection = analyses.stream()
//                    .filter(analysis -> analysis.getSection().getSectionId().equals(section.getSectionId()))
//                    .findFirst()
//                    .orElse(null);
//            List<HighlightEntity> highlightToSection = highlights.stream()
//                    .filter(highlight -> highlight.getSection().getSectionId().equals(section.getSectionId()))
//                    .toList();
//            List<SummaryEntity> summaryToSection = summaries.stream()
//                    .filter(summary -> summary.getSection().getSectionId().equals(section.getSectionId()))
//                    .toList();
//
//            List<ResponseDetailHighlightDto> responseHighlights = highlightToSection.stream()
//                    .map(HighlightMapper.INSTANCE::toResponseDetailHighlightDto)
//                    .toList();
//            ResponseRecordAnalysisDto responseAnalysis = AnalysisMapper.INSTANCE.toResponseRecordAnalysisDto(analysisToSection, responseHighlights);
//            List<ResponseDetailSummaryDto> responseSummaries = summaryToSection.stream()
//                    .map(SummaryMapper.INSTANCE::toResponseDetailSummaryDto)
//                    .toList();
//
//            responseSections.add(SectionMapper.INSTANCE.toResponseDetailSummaryDto(section, responseAnalysis, responseSummaries));
//        }
//
//        ResponseDetailFolderDto folder = FolderMapper.INSTANCE.toResponseDetailFolderDto(record.getFolder());
//        return RecordMapper.INSTANCE.toResponseRecordDetailDto(record, folder, responseSections);
//    }
//
//    public ResponseAnalysisDto getAnalysis(Long recordId, Long folderId, UserEntity user) {
//        // TODO : 모든 정보를 가져오는 무거운 작업이 많기 때문에 캐싱이 필요
//        boolean hasAccess = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
//        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//
//        List<StudyEntity> studies = studyJpaRepository.findAllByUserAndRecordRecordId(user, recordId);
//        List<QuizGradeProjection> quizzes = quizJpaRepository.findQuizGradeByUserAndRecordRecordId(user, recordId);
//        List<KeywordProjection> keywords = keywordJpaRepository.findAllByRecordId(recordId);
//
//        List<ResponseStudyTimesDto> responseStudyTimes = studies.stream()
//                .map(StudyTimeMapper.INSTANCE::toResponseStudyTimesDto)
//                .toList();
//        List<ResponseQuizGradeDto> responseQuizGrades = quizzes.stream()
//                .map(QuizMapper.INSTANCE::toResponseQuizGradeDto)
//                .toList();
//        List<ResponseKeywordDto> responseKeywords = keywords.stream()
//                .map(KeywordMapper.INSTANCE::toResponseKeywordDto)
//                .toList();
//
//        return new ResponseAnalysisDto(responseKeywords, responseStudyTimes, responseQuizGrades);
//    }
//
//    public ResponseRecordSearchDto searchRecord(String keyword, int page, int count, UserEntity user) {
//        Page<RecordSearchProjection> recordEntities = recordJpaRepository.findAllSharedOrOwnedRecords(
//                user,
//                keyword,
//                PageRequest.of(page - 1, count)
//        );
//
//        return new ResponseRecordSearchDto(recordEntities.stream()
//                .map(record -> {
//                    UserIntroDto uploader = new UserIntroDto(record.getUserId(), record.getProfilePath(), record.getName());
//                    return RecordMapper.INSTANCE.toResponseRecordSearch(record.getFolderId(), record, uploader);
//                }).toList(),
//                recordEntities.getTotalElements());
//    }
//
//    public ResponseRecordsDto getReceivingRecords(int page, int count, UserEntity user) {
//        Pageable pageable = PageRequest.of(page - 1, count);
//        Page<RecordEntity> records = recordJpaRepository.findReceivingRecordsByUserOrderByCreatedTimeDesc(user, pageable);
//        return getResponseRecordsDto(records.getContent(), records.getTotalElements());
//    }
//
//    public ResponseRecordsDto getSharingRecords(int page, int count, UserEntity user) {
//        Pageable pageable = PageRequest.of(page - 1, count);
//        Page<RecordEntity> records = recordJpaRepository.findSharingRecordsByUserOrderByCreatedTimeDesc(user, pageable);
//        return getResponseRecordsDto(records.getContent(), records.getTotalElements());
//    }
//
//    private ResponseRecordsDto getResponseRecordsDto(List<RecordEntity> records, Long total) {
//        if (records.isEmpty()) {
//            return new ResponseRecordsDto(new ArrayList<>(), 0L);
//        }
//
//        List<KeywordProjection> keywords = keywordJpaRepository.findAllByRecordInOrderByImportanceDesc(records);
//
//        return new ResponseRecordsDto(
//                records.stream()
//                        .map(record -> {
//                            List<ResponseKeywordDto> responseKeywords = keywords.stream()
//                                    .filter(keyword -> keyword.getRecordId().equals(record.getRecordId()))
//                                    .map(KeywordMapper.INSTANCE::toResponseKeywordDto)
//                                    .toList();
//
//                            return RecordMapper.INSTANCE.toResponseRecordDto(
//                                    record,
//                                    record.getFolder().getFolderId(),
//                                    responseKeywords
//                            );
//                        }).toList(),
//                total
//        );
//    }
//
//    @Transactional
//    public void changeLinkShare(Long folderId, Long recordId, boolean isShare, UserEntity user){
//        FolderEntity folder = folderJpaRepository.findFirstByFolderId(folderId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));
//        RecordEntity record = recordJpaRepository.findByRecordId(recordId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
//
//        if (!folder.getUser().getUserId().equals(user.getUserId())) throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
//        if (!record.getUser().getUserId().equals(user.getUserId())) throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
//
//        record.setIsShare(isShare);
//        recordJpaRepository.save(record);
//    }
//
//    @Transactional
//    public void createdRecord(Long folderId, RequestPostRecordDto dto, UserEntity user){
//        FolderEntity folderEntity = folderJpaRepository.findFirstByFolderId(folderId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));;
//
//        boolean hasAccess = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderEntity.getFolderId(), InvitationStatus.ACCEPT);
//        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//
//        FileDto file = fileStore.getFile(dto.path())
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD_FIREBASE));
//        RecordEntity record = RecordMapper.INSTANCE.toEntity(dto, folderEntity, user, file.size());
//
//        recordJpaRepository.save(record);
//
//        // TODO : Kafka를 통해 분석 요청 (Front한테 넘길 떄 주석 해제)
//        // kafkaProducer.send(topic, topicKey, new RequestKafkaDto(recordEntity.getRecordId(), user.getUserId()));
//    }
//
//    @Transactional
//    public void modifyRecordName(Long folderId, Long recordId, RequestRecordNameDto dto, UserEntity user) {
//        RecordEntity record = recordJpaRepository.findByRecordId(recordId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
//
//        boolean hasAccess = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
//        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//
//        if (!user.getUserId().equals(record.getUser().getUserId())) {
//            throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
//        }
//
//        record.setTitle(dto.title());
//        recordJpaRepository.save(record);
//    }
//
//    @Transactional
//    @FileCleanup
//    public void moveRecord(Long folderId, Long recordId, RequestRecordMoveDto dto, UserEntity user) {
//        RecordEntity record = recordJpaRepository.findByRecordId(recordId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
//        FolderEntity target = folderJpaRepository.findFirstByFolderId(dto.targetId())
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_TARGET_FOLDER));
//
//        boolean hasAccessFromFolder = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
//        if (!hasAccessFromFolder) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//
//        boolean hasAccessToFolder = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, dto.targetId(), InvitationStatus.ACCEPT);
//        if (!hasAccessToFolder) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//
//        if (!record.getUser().getUserId().equals(user.getUserId())) {
//            throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
//        } else if (!record.getFolder().getFolderId().equals(folderId)) {
//            throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
//        }
//
//        String oldPath = record.getPath();
//        String newPath = "folders/" + target.getFolderId() + oldPath.substring(oldPath.lastIndexOf("/"));
//        fileStore.moveFile(oldPath, newPath);
//
//        try {
//            record.setFolder(target);
//            record.setPath(newPath);
//            recordJpaRepository.save(record);
//        } catch (Exception e) {
//            throw new FileUploadException(newPath);
//        }
//
//        fileStore.deleteFile(oldPath);
//    }
//
//    @Transactional
//    public void modifyStudyTime(Long folderId, Long recordId, RequestStudyDto dto, UserEntity user) {
//        RecordEntity record = recordJpaRepository.findByRecordId(recordId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
//        if (!record.getFolder().getFolderId().equals(folderId)) throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
//
//        Boolean existsByFolderShare = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
//        if (!existsByFolderShare) throw new CustomException(ErrorCode.MISMATCH_RECORD_OWNER);
//
//        StudyEntity study = studyJpaRepository.findByCreatedTime(user, record)
//                .orElse(
//                        StudyEntity.builder()
//                            .user(user)
//                            .record(record)
//                            .build()
//                );
//
//        if (study.getStudyTime() != null) {
//            study.setStudyTime(study.getStudyTime() + dto.studyTime());
//        } else {
//            study.setStudyTime(dto.studyTime());
//        }
//
//        studyJpaRepository.save(study);
//    }
//
//    @Transactional
//    public void deleteRecord(Long folderId, Long recordId, UserEntity user){
//        RecordEntity record = recordJpaRepository.findByRecordId(recordId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));
//
//        boolean hasAccess = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatusAndRole_RoleName(
//                user,
//                folderId,
//                InvitationStatus.ACCEPT,
//                FolderRole.OWNER.getRoleName()
//        );
//        if (!hasAccess) {
//            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//        }
//        if(!record.getFolder().getFolderId().equals(folderId)) {
//            throw new CustomException(ErrorCode.MISMATCH_RECORD_FOLDER);
//        }
//
//        recordJpaRepository.delete(record);
//        fileStore.deleteFile(record.getPath());
//    }
//}
