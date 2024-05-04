package org.y2k2.globa.service;

import com.google.api.Http;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.exception.UnAuthorizedException;
import org.y2k2.globa.mapper.FolderMapper;
import org.y2k2.globa.mapper.QuizMapper;
import org.y2k2.globa.mapper.RecordMapper;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final JwtTokenProvider jwtTokenProvider;
    private final FolderShareService folderShareService;
    private final JwtUtil jwtUtil;

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


    public ResponseRecordsByFolderDto getRecords(String accessToken, Long folderId, int page, int count){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByUserId(userId);


        Pageable pageable = PageRequest.of(page-1, count);
        Page<RecordEntity> records = recordRepository.findAllByFolderFolderId(pageable, folderId);


        return new ResponseRecordsByFolderDto(records.stream()
                .map(RecordMapper.INSTANCE::toRequestRecordDto)
                .collect(Collectors.toList()), (int) records.getTotalElements());

    }

    public ResponseAllRecordWithTotalDto getAllRecords(String accessToken, int count){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity userEntity = userRepository.findOneByUserId(userId);



        Pageable pageable = PageRequest.of(0, count);
        Page<RecordEntity> records = recordRepository.findAllByOrderByCreatedTimeDesc(pageable);



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

                   return RecordMapper.INSTANCE.toResponseAllRecordDto(record,keywords);

                })
                .collect(Collectors.toList()), (int) records.getTotalElements());

    }

    public ResponseRecordDetailDto getRecordDetail(String accessToken, Long folderId, Long recordId){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByUserId(userId);


        RecordEntity record = recordRepository.findRecordEntityByRecordId(recordId);

        ResponseRecordDetailDto responseDto = new ResponseRecordDetailDto();

        List<SectionEntity> sections = sectionRepository.findAllByRecordRecordId(recordId);
        List<ResponseSectionDto> responseSections = new ArrayList<>();

        for(SectionEntity section : sections){
            ResponseSectionDto responseSectionDto = new ResponseSectionDto();

            responseSectionDto.setSectionId(section.getSectionId());
            responseSectionDto.setTitle(section.getTitle());
            responseSectionDto.setStartTime(section.getStartTime());
            responseSectionDto.setEntTime(section.getEntTime());
            responseSectionDto.setCreatedTime(section.getCreatedTime());

            System.out.println("==================== sectionId :: " + section.getSectionId() + "::::::::");
            AnalysisEntity analysis = analysisRepository.findAllBySectionSectionId(section.getSectionId());

            if(analysis == null)
                throw new NotFoundException("anlayis not found");

            ResponseRecordAnalysisDto responseRecordAnalysisDto = new ResponseRecordAnalysisDto();
            responseRecordAnalysisDto.setAnalysisId(analysis.getAnalysisId());
            responseRecordAnalysisDto.setContent(analysis.getContent());
            List<HighlightEntity> highlights = highlightRepository.findAllBySectionSectionId(section.getSectionId());
            responseRecordAnalysisDto.setHighlights(highlights);



            responseSectionDto.setAnalysis(responseRecordAnalysisDto);

            responseSectionDto.setSummary(summaryRepository.findAllBySectionSectionId(section.getSectionId()));
            responseSections.add(responseSectionDto);
        }


        responseDto.setRecordId(record.getRecordId());
        responseDto.setTitle(record.getTitle());
        responseDto.setPath(record.getPath());
        responseDto.setSize(record.getSize());
        responseDto.setUser(record.getUser());
        responseDto.setFolder(record.getFolder());
        responseDto.setSection(responseSections);
        responseDto.setCreatedTime(record.getCreatedTime());
        System.out.println(responseDto);

        return responseDto;

    }

    public ResponseAnalysisDto getAnalysis(String accessToken, Long recordId, Long folderId){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity targetEntity = userRepository.findByUserId(userId);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderId(targetEntity,folderId);
        if (folderShareEntity == null) throw new UnAuthorizedException("This user not deserves");

        List<StudyEntity> studyEntities = studyRepository.findAllByUserUserIdAndRecordRecordId(userId,recordId);
        List<QuizGradeProjection> quizGradeProjectionList = analysisRepository.findQuizGradeByUserUserIdAndRecordRecordId(userId, recordId);
        List<KeywordProjection> keywordProjectionList = analysisRepository.findKeywordByRecordId(recordId);

        ResponseAnalysisDto responseAnalysisDto = new ResponseAnalysisDto();
        List<ResponseStudyTimesDto> studyTimes = new ArrayList<>();
        List<ResponseQuizGradeDto> quizGrades = new ArrayList<>();
        List<ResponseKeywordDto> keywords = new ArrayList<>();

        for( StudyEntity studyEntitiy : studyEntities ){
            ResponseStudyTimesDto responseStudyTimesDto = new ResponseStudyTimesDto();
            responseStudyTimesDto.setStudyTime(studyEntitiy.getStudyTime());
            responseStudyTimesDto.setCreatedTime(studyEntitiy.getCreatedTime());
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

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity targetEntity = userRepository.findByUserId(userId);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderId(targetEntity,folderId);
        if (folderShareEntity == null) throw new UnAuthorizedException("This user not deserves");

        List<QuizEntity> quizEntities = quizRepository.findAllByRecordRecordId(recordId);

        return quizEntities.stream()
                .map(QuizMapper.INSTANCE::toQuizDto)
                .collect(Collectors.toList());
    }

    public HttpStatus postQuiz(String accessToken, Long recordId, Long folderId, List<RequestQuizDto> quizs){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity targetEntity = userRepository.findByUserId(userId);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderId(targetEntity,folderId);
        if (folderShareEntity == null) throw new UnAuthorizedException("This user not deserves");

        for(RequestQuizDto requestQuizDto : quizs){

            if( requestQuizDto.getQuizId() == null)
                throw new BadRequestException("Required Quiz Id");

            QuizAttemptEntity quizAttemptEntity = new QuizAttemptEntity();
            quizAttemptEntity.setUser(targetEntity);
            quizAttemptEntity.setQuiz(quizRepository.findQuizEntityByQuizId(requestQuizDto.getQuizId()));
            quizAttemptEntity.setCorrect(requestQuizDto.isCorrect());
            quizAttemptEntity.setCreatedTime(LocalDateTime.now());

            quizAttemptRepository.save(quizAttemptEntity);
        }

        return HttpStatus.CREATED;
    }


}
