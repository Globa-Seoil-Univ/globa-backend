package org.y2k2.globa.service;

import com.google.cloud.storage.Bucket;
import com.google.firebase.auth.FirebaseAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.Projection.StudyTimeProjection;
import org.y2k2.globa.dto.response.analysis.ResponseAnalysisDto;
import org.y2k2.globa.dto.response.keyword.ResponseKeywordDto;
import org.y2k2.globa.dto.request.user.RequestNotificationSettingDto;
import org.y2k2.globa.dto.request.fcm.RequestNotificationTokenDto;
import org.y2k2.globa.dto.response.quiz.ResponseQuizGradeDto;
import org.y2k2.globa.dto.response.study.ResponseStudyTimesDto;
import org.y2k2.globa.dto.request.survey.RequestSurveyDto;
import org.y2k2.globa.dto.request.user.RequestUserPostDTO;
import org.y2k2.globa.dto.response.user.ResponseUserDto;
import org.y2k2.globa.dto.response.user.ResponseUserSearchDto;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.KeywordMapper;
import org.y2k2.globa.mapper.QuizMapper;
import org.y2k2.globa.mapper.StudyTimeMapper;
import org.y2k2.globa.mapper.UserMapper;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.util.jwt.JWT;
import org.y2k2.globa.util.jwt.JWTProvider;
import org.y2k2.globa.util.redis.RedisStore;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    private final Bucket bucket;
    private final FirebaseAuth firebaseAuth;

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final JWTProvider jwtProvider;
    private final RedisStore redisStore;

    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final SurveyRepository surveyRepository;
    private final FolderRepository folderRepository;
    private final RecordRepository recordRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final QuizRepository quizRepository;
    private final KeywordRepository keywordRepository;

    public final FolderService folderService;

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

        UserEntity user = userRepository.findBySnsId(requestUserPostDTO.getSnsId())
                .orElseGet(() -> {
                    String code = generateRandomCode();

                    while (userRepository.findOneByCode(code) != null) {
                        code = generateRandomCode();
                    }

                    UserEntity userEntity = UserMapper.INSTANCE.toEntity(requestUserPostDTO.getSnsKind(), code, requestUserPostDTO);
                    UserEntity newUser = userRepository.save(userEntity);

                    UserRoleEntity userRoleEntity = new UserRoleEntity();
                    RoleEntity roleEntity = roleRepository.findByRoleId(4);
                    userRoleEntity.setUser(newUser);
                    userRoleEntity.setRoleId(roleEntity);
                    userRoleRepository.save(userRoleEntity);

                    folderService.postDefaultFolder(newUser);
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

    public ResponseUserDto getUser(UserEntity user){
        FolderEntity folderEntity = folderRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(user.getUserId());
        if(folderEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_DEFAULT_FOLDER);

        return UserMapper.INSTANCE.toResponseUserDto(user, folderEntity.getFolderId());
    }

    public ResponseUserSearchDto searchUser(String code){
        UserEntity userEntity = userRepository.findOneByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if(userEntity.getIsDeleted())
            throw new CustomException(ErrorCode.DELETED_USER);

        return UserMapper.INSTANCE.toResponseUserSearchDto(userEntity);
    }

    public RequestNotificationSettingDto getNotification(UserEntity user){
        return UserMapper.INSTANCE.toResponseNotificationSettingDto(user);
    }

    public ResponseAnalysisDto getAnalysis(UserEntity user) {
        List<RecordEntity> records = recordRepository.findAllByUser(user.getUserId());
        if(records.isEmpty())
            return new ResponseAnalysisDto(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        List<Long> recordIds = records.stream().map(RecordEntity::getRecordId).toList();
        List<StudyTimeProjection> studyTimeProjections = studyRepository.findStudyTimeByUserInWeek(user.getUserId());
        List<QuizGradeProjection> quizGradeProjections = quizRepository.findQuizGradeByUserInWeek(user.getUserId());
        List<KeywordProjection> keywordProjections = keywordRepository.findKeywordByRecordIds(recordIds);

        List<ResponseStudyTimesDto> studyTimes = studyTimeProjections.stream().map(
                StudyTimeMapper.INSTANCE::toResponseStudyTimesDto
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

    @Transactional
    public void updateProfile(MultipartFile file, long userId) {
        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_USER);

        if(userEntity.getIsDeleted())
            throw new CustomException(ErrorCode.DELETED_USER);

        long current = new Date().getTime();
        long size = file.getSize();
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String mimeType = file.getContentType();

        String oldPath = userEntity.getProfilePath();
        String newPath = "users/" + userId + "/profile/" + current + "." + extension;

        try {
            bucket.create(newPath, file.getBytes());

            userEntity.setProfilePath(newPath);
            userEntity.setProfileType(mimeType);
            userEntity.setProfileSize(size);
            userRepository.save(userEntity);

            if(oldPath != null && bucket.get(oldPath) != null) {
                try {
                    bucket.get(oldPath).delete();
                } catch (Exception e) {
                    log.error("Failed to delete old profile : " + e);
                }
            }
        } catch (Exception e) {
            if(bucket.get(newPath) != null) {
                bucket.get(newPath).delete();
            }

            log.error("Failed to update profile : " + e);
            throw new CustomException(ErrorCode.FAILED_FILE_UPLOAD);
        }
    }

    @Transactional
    public RequestNotificationSettingDto putNotification(String accessToken, Long putUserId, RequestNotificationSettingDto settingDto){

        Long userId = jwtProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        if (!Objects.equals(userId, putUserId)){
            throw new CustomException(ErrorCode.MISMATCH_NOFI_OWNER);
        }

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getIsDeleted())
            throw new CustomException(ErrorCode.DELETED_USER);

        userEntity.setUploadNofi(settingDto.getUploadNofi());
        userEntity.setShareNofi(settingDto.getShareNofi());
        userEntity.setEventNofi(settingDto.getEventNofi());

        UserEntity savedEntity = userRepository.save(userEntity);

        RequestNotificationSettingDto responseUserRequestNotificationSettingDto = new RequestNotificationSettingDto();

        responseUserRequestNotificationSettingDto.setUploadNofi(savedEntity.getUploadNofi());
        responseUserRequestNotificationSettingDto.setShareNofi(savedEntity.getShareNofi());
        responseUserRequestNotificationSettingDto.setEventNofi(savedEntity.getEventNofi());

        return responseUserRequestNotificationSettingDto;
    }

    @Transactional
    public HttpStatus patchUserName(String accessToken, Long putUserId, String name){
        Long userId = jwtProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        if (!Objects.equals(userId, putUserId)){
            throw new CustomException(ErrorCode.MISMATCH_RENAME_OWNER);
        }

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getIsDeleted())
            throw new CustomException(ErrorCode.DELETED_USER);

        userEntity.setName(name);

        userRepository.save(userEntity);


        return HttpStatus.OK;
    }

    @Transactional
    public HttpStatus deleteUser(String accessToken, RequestSurveyDto requestSurveyDto){
        Long userId = jwtProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        if (userEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if(userEntity.getIsDeleted())
            throw new CustomException(ErrorCode.DELETED_USER);

        userEntity.setIsDeleted(true);
        userEntity.setDeletedTime(LocalDateTime.now());

        SurveyEntity surveyEntity = new SurveyEntity();
        surveyEntity.setSurveyType(String.valueOf(requestSurveyDto.getSurveyType()).charAt(0));
        surveyEntity.setContent(requestSurveyDto.getContent());
        surveyEntity.setCreatedTime(LocalDateTime.now());

        userRepository.save(userEntity);
        surveyRepository.save(surveyEntity);

//        folderService.deleteDefaultFolder(userEntity);

        return HttpStatus.OK;
    }

    @Transactional
    public void addAndUpdateNotificationToken(RequestNotificationTokenDto requestNotificationTokenDto, Long userId){
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null)
            throw new CustomException(ErrorCode.NOT_FOUND_USER);

        userEntity.setNotificationToken(requestNotificationTokenDto.getToken());
        userEntity.setNotificationTokenTime(LocalDateTime.now());
        userRepository.save(userEntity);
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