package org.y2k2.globa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.StudyEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.AuthorizedException;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.repository.StudyRepository;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.util.JwtToken;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.JwtUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;

    public final UserRepository userRepository;;
    public final StudyRepository studyRepository;

    public JwtToken reloadRefreshToken(String refreshToken, String accessToken){
        try {
            Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
            Date expiredTime = jwtTokenProvider.getExpiredTimeByAccessTokenWithoutCheck(accessToken);
            String redisRefreshToken = jwtUtil.getRefreshToken(userId);

            if(new Date().before(expiredTime)) {
                jwtUtil.deleteValue(String.valueOf(userId));
                throw new BadRequestException("우애우애우ㅐ우앵");
            }

            if (!redisRefreshToken.equals(refreshToken)) {
                jwtUtil.deleteValue(String.valueOf(userId));
                throw new BadRequestException("Not equal RefreshToken");
            }

            jwtTokenProvider.getExpirationDateFromToken(redisRefreshToken);


            JwtToken jwtToken = jwtTokenProvider.generateToken(userId);

            jwtUtil.insertRedisRefreshToken(userId, jwtToken.getRefreshToken());

            return jwtToken;
        }
        catch(Exception e){
            throw e;
        }
    }

    public JwtToken postUser(RequestUserPostDTO requestUserPostDTO){

        UserEntity postUserEntity = userRepository.findBySnsId(requestUserPostDTO.getSnsId());

        if(postUserEntity == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setSnsKind(requestUserPostDTO.getSnsKind());
            userEntity.setSnsId(requestUserPostDTO.getSnsId());
            userEntity.setCode(generateRandomCode(6));
            userEntity.setName(requestUserPostDTO.getName());
            userEntity.setProfilePath(requestUserPostDTO.getProfile());
            userEntity.setPrimaryNofi(requestUserPostDTO.getNotification());
            userEntity.setShareNofi(requestUserPostDTO.getNotification());
            userEntity.setUploadNofi(requestUserPostDTO.getNotification());
            userEntity.setEventNofi(requestUserPostDTO.getNotification());
            userEntity.setCreatedTime(LocalDateTime.now());
            userEntity.setDeleted(false);

            postUserEntity = userRepository.save(userEntity);
        }

        JwtToken jwtToken = jwtTokenProvider.generateToken(postUserEntity.getUserId());

        jwtUtil.insertRedisRefreshToken(postUserEntity.getUserId(), jwtToken.getRefreshToken());


        return jwtToken;

    }

    public ResponseUserDTO getUser(String accessToken){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        ResponseUserDTO responseUserDTO = new ResponseUserDTO();

        responseUserDTO.setProfile(userEntity.getProfilePath());
        responseUserDTO.setName(userEntity.getName());
        responseUserDTO.setCode(userEntity.getCode());
        responseUserDTO.setPublicFolderId(9999);

        return responseUserDTO;

    }

    public ResponseUserSearchDto getUser(String accessToken, String code){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByCode(code);

        ResponseUserSearchDto responseUserSearchDto = new ResponseUserSearchDto();

        responseUserSearchDto.setProfile(userEntity.getProfilePath());
        responseUserSearchDto.setName(userEntity.getName());
        responseUserSearchDto.setCode(userEntity.getCode());

        return responseUserSearchDto;

    }

    public ResponseUserNotificationDto getNotification(String accessToken, Long pathUserId){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        if (!Objects.equals(userId, pathUserId)){
            throw new AuthorizedException("Not Matched User ! owner : " + userId + ", request : " + pathUserId);
        }

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        ResponseUserNotificationDto responseUserNotificationDto = new ResponseUserNotificationDto();

        responseUserNotificationDto.setUploadNofi(userEntity.getUploadNofi());
        responseUserNotificationDto.setShareNofi(userEntity.getShareNofi());
        responseUserNotificationDto.setEventNofi(userEntity.getEventNofi());

        return responseUserNotificationDto;

    }

    public ResponseAnalysisDto getAnalysis(String accessToken, Long pathUserId){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        if (!Objects.equals(userId, pathUserId)){
            throw new AuthorizedException("Not Matched User ! owner : " + userId + ", request : " + pathUserId);
        }

        List<StudyEntity> studyEntities = studyRepository.findAllByUserUserId(userId);
        List<QuizGradeProjection> quizGradeProjectionList = userRepository.findQuizGradeByUser(userId);
        List<KeywordProjection> keywordProjectionList = userRepository.findKeywordByRecordId(1L);

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












    public String generateRandomCode(int length){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();

        StringBuilder code = new StringBuilder();

        for(int i = 0 ; i < length; ++i ){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));

        }

        return code.toString();
    }



    public ObjectNode getRanking(int page, int count) {
        try {
            Pageable pageable = PageRequest.of(page-1, count);
            Page<UserEntity> rankingEntities =  userRepository.findAll(pageable);


            List<UserEntity> resultValue = rankingEntities.getContent();
            List<ObjectNode> responseList = new ArrayList<>();

            for (UserEntity userEntity : resultValue) {
                ObjectNode rankingNode = createRankingNode(userEntity);
                responseList.add(rankingNode);
            }

            return createResultNode(responseList);

        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void errorTestBadRequest() {
        try {

            throw new BadRequestException("Test BAD");

        }catch (BadRequestException e) {
            throw e;
        }
    }

    private ObjectNode createRankingNode(UserEntity userEntity) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rankingNode = objectMapper.createObjectNode();


        System.out.println(UserDTO.toUserDTO(userEntity));
        rankingNode.put("user", userEntity.getName());

        return rankingNode;
    }

    private ObjectNode createResultNode(List<ObjectNode> responseList) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode codesArrayNode = objectMapper.createArrayNode();

        for (ObjectNode node : responseList) {
            codesArrayNode.add(node);
        }

        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.set("rankings", codesArrayNode);
        resultNode.put("total", responseList.size());

        return resultNode;
    }
}