package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.folder.service.FolderService;
import org.y2k2.globa.application.keyword.mapper.KeywordMapper;
import org.y2k2.globa.application.quiz.mapper.QuizMapper;
import org.y2k2.globa.application.studytime.mapper.StudyTimeMapper;
import org.y2k2.globa.application.survey.mapper.SurveyMapper;
import org.y2k2.globa.application.user.mapper.UserMapper;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordJpaRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizJpaRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.record.repository.RecordJpaRepository;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.study.repository.StudyJpaRepository;
import org.y2k2.globa.infrastructure.persistence.survey.repository.SurveyJpaRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderJpaRepository;
import org.y2k2.globa.insfrastructure.persistence.jpa.repository.UserJpaRepository;
import org.y2k2.globa.infrastructure.persistence.role.repository.RoleJpaRepository;
import org.y2k2.globa.infrastructure.persistence.keyword.projection.KeywordProjection;
import org.y2k2.globa.infrastructure.persistence.quiz.projection.QuizGradeProjection;
import org.y2k2.globa.infrastructure.persistence.study.projection.StudyTimeProjection;
import org.y2k2.globa.common.annotation.FileCleanup;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.user.dto.request.RequestNameDto;
import org.y2k2.globa.application.user.dto.request.RequestProfileImageDto;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.keyword.dto.response.ResponseKeywordDto;
import org.y2k2.globa.application.user.dto.request.RequestNotificationSettingDto;
import org.y2k2.globa.application.fcm.dto.request.RequestNotificationTokenDto;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizGradeDto;
import org.y2k2.globa.application.studytime.dto.response.ResponseStudyTimesDto;
import org.y2k2.globa.application.survey.dto.request.RequestSurveyDto;
import org.y2k2.globa.application.user.dto.request.RequestUserPostDTO;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.repository.UserRoleJpaRepository;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.common.util.jwt.JWTProvider;
import org.y2k2.globa.common.util.redis.RedisStore;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final JWTProvider jwtProvider;
    private final RedisStore redisStore;
    private final FileStore fileStore;

    private final UserJpaRepository userJpaRepository;
    private final StudyJpaRepository studyJpaRepository;
    private final SurveyJpaRepository surveyJpaRepository;
    private final FolderJpaRepository folderJpaRepository;
    private final RecordJpaRepository recordJpaRepository;
    private final UserRoleJpaRepository userRoleJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final QuizJpaRepository quizJpaRepository;
    private final KeywordJpaRepository keywordJpaRepository;

    // TODO : 의존성 제거 ?
    public final FolderService folderService;

    public ResponseUserDto getUser(UserEntity user){
        FolderEntity folderEntity = folderJpaRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DEFAULT_FOLDER));

        return UserMapper.INSTANCE.toResponseUserDto(user, folderEntity.getFolderId());
    }

    public UserEntity getUser(Long userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (user.getIsDeleted()) {
            throw new CustomException(ErrorCode.DELETED_USER);
        }

        return user;
    }

    public ResponseUserSearchDto searchUser(String code){
        UserEntity userEntity = userJpaRepository.findOneByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if(userEntity.getIsDeleted())
            throw new CustomException(ErrorCode.DELETED_USER);

        return UserMapper.INSTANCE.toResponseUserSearchDto(userEntity);
    }

    public ResponseNotificationSettingDto getNotification(UserEntity user){
        return UserMapper.INSTANCE.toResponseNotificationSettingDto(user);
    }

    public ResponseAnalysisDto getAnalysis(UserEntity user) {
        List<RecordEntity> records = recordJpaRepository.findAllByUser(user.getUserId());
        if(records.isEmpty())
            return new ResponseAnalysisDto(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        List<Long> recordIds = records.stream().map(RecordEntity::getRecordId).toList();
        List<StudyTimeProjection> studyTimeProjections = studyJpaRepository.findStudyTimeByUserInWeek(user.getUserId());
        List<QuizGradeProjection> quizGradeProjections = quizJpaRepository.findQuizGradeByUserInWeek(user.getUserId());
        List<KeywordProjection> keywordProjections = keywordJpaRepository.findKeywordByRecordIds(recordIds);

        List<ResponseStudyTimesDto> studyTimes = studyTimeProjections.stream().map(
                StudyTimeMapper.INSTANCE::toResponseTotalStudyTimesDto
        ).toList();
        List<ResponseQuizGradeDto> quizGrades = quizGradeProjections.stream().map(
                QuizMapper.INSTANCE::toResponseQuizGradeDto
        ).toList();
        List<ResponseKeywordDto> keywords = keywordProjections.stream().map(
                KeywordMapper.INSTANCE::toResponseKeywordDto
        ).toList();

        return new ResponseAnalysisDto(
                keywords,
                studyTimes,
                quizGrades
        );
    }

    public JWT reloadRefreshToken(String accessToken, String refreshToken) {
        Long userId = jwtProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        String redisRefreshToken = redisStore.getValue(userId);

        if (!jwtProvider.isExpired(accessToken)) {
            redisStore.deleteValue(String.valueOf(userId));
            throw new CustomException(ErrorCode.ACTIVE_ACCESS_TOKEN);
        }

        if (jwtProvider.isExpired(refreshToken)) {
            redisStore.deleteValue(String.valueOf(userId));
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        if (!redisRefreshToken.equals(refreshToken)) {
            redisStore.deleteValue(String.valueOf(userId));
            throw new CustomException(ErrorCode.NOT_MATCH_REFRESH_TOKEN);
        }

        JWT jwt = jwtProvider.generateToken(userId);
        redisStore.setValueExpire(
                userId.toString(),
                jwt.getRefreshToken(),
                jwt.getRefreshTokenExpireTime()
        );

        return jwt;
    }

    @Transactional
    public JWT signup(RequestUserPostDTO requestUserPostDTO){
//        switch (requestUserPostDTO.getSnsKind()) {
//            case "1001" :
//                try {
//                    RestTemplate restTemplate = new RestTemplate();
//
//                    // HTTP 요청 헤더에 Authorization 추가
//                    HttpHeaders headers = new HttpHeaders();
//                    headers.set("Authorization", "Bearer " + requestUserPostDTO.getToken());
//
//                    HttpEntity<String> entity = new HttpEntity<>(headers);
//
//                        // 사용자 정보 요청
//                        ResponseEntity<String> response = restTemplate.exchange(
//                                KAKAO_USER_INFO_URL,
//                                HttpMethod.GET,
//                                entity,
//                                String.class);
//                        // JSON 응답을 JsonNode로 파싱
//                        ObjectMapper objectMapper = new ObjectMapper();
//                        JsonNode responseBody = objectMapper.readTree(response.getBody());
//                        String kakaoUid = String.valueOf(responseBody.get("id"));
//                    if(!requestUserPostDTO.getSnsId().equalsIgnoreCase(kakaoUid))
//                        throw new CustomException(ErrorCode.INVALID_SNS_TOKEN);
//
//                } catch (Exception e) {
//                    log.error("Failed to verify kakao token : " + e);
//                    throw new CustomException(ErrorCode.INVALID_SNS_TOKEN);
//                }
//                break;
//            case "1004" :
//                try {
//                    FirebaseToken token = firebaseAuth.verifyIdToken(requestUserPostDTO.getToken());
//
//                    if(!requestUserPostDTO.getSnsId().equalsIgnoreCase(token.getUid())){
//                        throw new CustomException(ErrorCode.INVALID_SNS_TOKEN);
//                    }
//
//                    System.out.println(token.getUid());
//                    System.out.println(token.getEmail());
//                    System.out.println(token.getName());
//                    System.out.println(token.getPicture());
//                } catch (FirebaseAuthException e) {
//                    log.error("Failed to verify firebase token : " + e);
//                    throw new CustomException(ErrorCode.INVALID_SNS_TOKEN);
//                }
//                break;
//        }

        UserEntity user = userJpaRepository.findBySnsId(requestUserPostDTO.getSnsId())
                .orElseGet(() -> {
                    String code = generateRandomCode();

                    while (userJpaRepository.findOneByCode(code) != null) {
                        code = generateRandomCode();
                    }

                    UserEntity userEntity = UserMapper.INSTANCE.toEntity(requestUserPostDTO.getSnsKind(), code, requestUserPostDTO);
                    UserEntity newUser = userJpaRepository.save(userEntity);

                    UserRoleEntity userRoleEntity = new UserRoleEntity();
                    RoleEntity roleEntity = roleJpaRepository.findByRoleId(4);
                    userRoleEntity.setUser(newUser);
                    userRoleEntity.setRoleId(roleEntity);
                    userRoleJpaRepository.save(userRoleEntity);

                    folderService.createDefaultFolder(newUser);
                    return newUser;
                });

        if (user.getIsDeleted()) {
            throw new CustomException(ErrorCode.DELETED_USER);
        }

        JWT jwt = jwtProvider.generateToken(user.getUserId());
        redisStore.setValueExpire(
                user.getUserId().toString(),
                jwt.getRefreshToken(),
                jwt.getRefreshTokenExpireTime()
        );

        return jwt;
    }

    @Transactional
    public void upsertFcmToken(RequestNotificationTokenDto dto, UserEntity user){
        user.setNotificationToken(dto.token());
        user.setNotificationTokenTime(LocalDateTime.now());
        userJpaRepository.save(user);
    }

    @Transactional
    public void modifyNotification(RequestNotificationSettingDto settingDto, UserEntity user){
        user.setUploadNofi(settingDto.uploadNofi());
        user.setShareNofi(settingDto.shareNofi());
        user.setEventNofi(settingDto.eventNofi());
        userJpaRepository.save(user);
    }

    @Transactional
    public void modifyUsername(RequestNameDto dto, UserEntity user){
        FolderEntity folder = folderJpaRepository.findByDefaultFolder(user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DEFAULT_FOLDER));

        folder.setTitle(dto.name());
        user.setName(dto.name());

        folderJpaRepository.save(folder);
        userJpaRepository.save(user);
    }

    @Transactional
    @FileCleanup
    public void modifyProfileImg(RequestProfileImageDto dto, UserEntity user) {
        String oldProfileImgPath = user.getProfilePath();
        FileDto fileDto = fileStore.storeFile("profiles/", dto.profile());

        try {
            user.setProfilePath(fileDto.storePath());
            user.setProfileType(fileDto.extension());
            user.setProfileSize(fileDto.size());
            userJpaRepository.save(user);
        } catch (Exception e) {
            throw new FileUploadException(fileDto.storePath());
        }

        if (!oldProfileImgPath.isEmpty()) {
            fileStore.deleteFile(oldProfileImgPath);
        }
    }

    @Transactional
    public void deleteUser(RequestSurveyDto dto, UserEntity user){
        user.setIsDeleted(true);
        user.setDeletedTime(new CustomTimestamp().getTimestamp());
        user.setNotificationToken(null);
        user.setNotificationTokenTime(null);

        userJpaRepository.save(user);
        surveyJpaRepository.save(SurveyMapper.INSTANCE.toEntity(dto));
    }

    private String generateRandomCode(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for(int i = 0; i < 6; ++i ){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}