package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.ForbiddenException;
import org.y2k2.globa.service.UserService;
import org.y2k2.globa.util.JwtToken;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.ValidValues;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("user")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 알림을 보내는 API입니다.")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "회원 가입과 로그인",
            description = """
                    요청한 snsKind, snsId, name이 없다면 회원 가입을 진행하고 있다면 로그인을 시도합니다. <br>
                    snsId는 1001 ~ 1004 사이의 값만 허용합니다. <br>
                    name은 30자 이하로 제한합니다. <br>
                    회원 가입 및 로그인에 성공하면 Access Token을 반환합니다.""",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 가입 또는 로그인 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtToken.class))
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping
    public ResponseEntity<?> postUser(@RequestBody RequestUserPostDTO requestUserPostDTO) {

        if ( requestUserPostDTO.getSnsKind() == null )
            throw new CustomException(ErrorCode.REQUIRED_SNS_KIND);
        if ( requestUserPostDTO.getSnsId() == null )
            throw new CustomException(ErrorCode.REQUIRED_SNS_ID);
        if ( requestUserPostDTO.getName() == null )
            throw new CustomException(ErrorCode.REQUIRED_NAME);

        if ( ValidValues.validSnsKinds.contains(requestUserPostDTO.getSnsKind()) )
            throw new CustomException(ErrorCode.SNS_KIND_BAD_REQUEST);
        if ( requestUserPostDTO.getName().length() > 32 )
            throw new CustomException(ErrorCode.NAME_BAD_REQUEST);

        if ( requestUserPostDTO.getNotification() == null )
            requestUserPostDTO.setNotification(true);

        JwtToken jwtToken = userService.postUser(requestUserPostDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(jwtToken);
    }

    @Operation(
            summary = "Access Token 갱신",
            description = "Refresh Token을 사용하여 Access Token을 갱신합니다.\nRTR 기법을 사용하기 때문에 사용한 Refresh Token은 폐기 처리됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Access Token 갱신 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtToken.class))
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/auth")
    public ResponseEntity<?> authUser(@RequestBody Map<String, String> requestTokenMap,
                                      @Parameter(hidden = true)
                                      @RequestHeader(value = "Authorization", required = false) String accessToken) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        JwtToken jwtToken;

        try {
            if ( requestTokenMap.get("requestToken") == null )
                throw new CustomException(ErrorCode.REQUIRED_REQUEST_TOKEN);
            jwtToken = userService.reloadRefreshToken(requestTokenMap.get("requestToken"), accessToken);

        } catch (Exception e) {
            throw e;
        }

        return ResponseEntity.status(HttpStatus.OK).body(jwtToken);
    }

    @Operation(
            summary = "내 정보 가져오기",
            description = "Access Token을 사용하여 내 정보를 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 정보 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseUserDTO.class))
                    ),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "404", ref = "404"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping
    public ResponseEntity<?> getUser(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String accessToken) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        ResponseUserDTO result = userService.getUser(accessToken);

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "상대 정보 가져오기",
            description = "유저의 고유한 코드를 사용해 해당 유저의 간단한 정보를 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상대 정보 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseUserSearchDto.class))
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "404", ref = "404"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<?> getUserSearch(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(value = "code", required = false) String code) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( code == null )
            throw new CustomException(ErrorCode.REQUIRED_USER_CODE);

        ResponseUserSearchDto result = userService.getUser(accessToken,code);

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "내 알림 정보 가져오기",
            description = "Access Token을 사용하여 내 알림 정보를 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 알림 정보 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationSettingDto.class))
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "404", ref = "404"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/{user_id}/notification")
    public ResponseEntity<?> getUserNotification(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable(value = "user_id", required = false) Long userId) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( userId == null )
            throw new CustomException(ErrorCode.REQUIRED_USER_ID);

        NotificationSettingDto result = userService.getNotification(accessToken,userId);

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "내 분석 정보 가져오기",
            description = "Access Token을 사용하여 내 분석 정보를 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 분석 정보 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAnalysisDto.class))
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "404", ref = "404"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/{user_id}/analysis")
    public ResponseEntity<?> getAnalysis(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable(value = "user_id", required = false) Long userId) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( userId == null )
            throw new CustomException(ErrorCode.REQUIRED_USER_ID);

        ResponseAnalysisDto result = userService.getAnalysis(accessToken,userId);

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "알림 정보 수정",
            description = "Access Token을 사용하여 내 알림 정보를 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "알림 정보 수정 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationSettingDto.class))
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PutMapping("/{user_id}/notification")
    public ResponseEntity<?> putNotification(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable(value = "user_id", required = false) Long userId,
            @RequestBody NotificationSettingDto NotificationSettingDto) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( userId == null )
            throw new CustomException(ErrorCode.REQUIRED_USER_ID);
        if ( NotificationSettingDto.getEventNofi() == null || NotificationSettingDto.getUploadNofi() == null  || NotificationSettingDto.getShareNofi() == null  )
            throw new CustomException(ErrorCode.NOFI_POST_BAD_REQUEST);

        NotificationSettingDto result = userService.putNotification(accessToken,userId, NotificationSettingDto);

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "이름 수정",
            description = "Access Token을 사용하여 내 이름을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이름 수정 완료",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PatchMapping("/{user_id}/name")
    public ResponseEntity<?> patchUserName(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable(value = "user_id", required = false) Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "이름",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"name\": \"string\"}")
                    )
            )
            @RequestBody Map<String, String> nameMap) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( userId == null )
            throw new CustomException(ErrorCode.REQUIRED_USER_ID);
        if ( nameMap.get("name") == null  )
            throw new CustomException(ErrorCode.REQUIRED_NAME);

        HttpStatus result = userService.patchUserName(accessToken,userId, nameMap.get("name"));

        return ResponseEntity.status(result).body("");
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "Access Token을 사용하여 회원을 탈퇴합니다. (Soft Delete)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 탈퇴 완료",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping
    public ResponseEntity<?> deleteUser(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestBody RequestSurveyDto requestSurveyDto) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if( requestSurveyDto.getSurveyType() == null || requestSurveyDto.getContent() == null)
            throw new CustomException(ErrorCode.SURVEY_POST_BAD_REQUEST);

        HttpStatus result = userService.deleteUser(accessToken, requestSurveyDto);

        return ResponseEntity.status(result).body("");
    }

    @Operation(
            summary = "FCM 알림 토큰 수정",
            description = "Access Token을 사용하여 FCM 알림 토큰을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "알림 토큰 수정 완료",
                            headers = @Header(
                                    name = "Location",
                                    description = "내 정보 가져오기",
                                    schema = @Schema(type = "string")
                            ),
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PutMapping("/{userId}/notification/token")
    public ResponseEntity<?> updateNotificationToken(
            @RequestHeader(value = "Authorization") String accessToken,
            @Valid @RequestBody RequestNotificationTokenDto dto,
            @PathVariable(value = "userId", required = false) long userId) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long accessUserId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        if (accessUserId != userId)
            throw new CustomException(ErrorCode.INVALID_TOKEN_USER);

        userService.addAndUpdateNotificationToken(dto, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "FCM 알림 토큰 등록",
            description = "Access Token을 사용하여 FCM 알림 토큰을 등록합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "알림 토큰 등록 완료",
                            headers = @Header(
                                    name = "Location",
                                    description = "내 정보 가져오기",
                                    schema = @Schema(type = "string")
                            ),
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "403", ref = "403"),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/{userId}/notification/token")
    public ResponseEntity<?> postNotificationToken(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization") String accessToken,
            @Valid @RequestBody RequestNotificationTokenDto dto,
            @PathVariable(value = "userId", required = false) long userId) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long accessUserId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        if (accessUserId != userId)
            throw new CustomException(ErrorCode.INVALID_TOKEN_USER);

        userService.addAndUpdateNotificationToken(dto, userId);
        return ResponseEntity.created(URI.create("/user")).build();
    }

    @Operation(
            summary = "프로필 사진 수정",
            description = "Access Token을 사용하여 프로필 사진을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "프로필 사진 수정 완료",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "400", ref = "400"),
                    @ApiResponse(responseCode = "401", ref = "401"),
                    @ApiResponse(responseCode = "403", ref = "403"),
                    @ApiResponse(responseCode = "500",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "default",
                                                    value = "{\"errorCode\":500,\"message\":\"Internal Server Error\",\"timestamp\":\"2024-05-30 15:00:00\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "firebase upload failed",
                                                    value = "{\"errorCode\":50010,\"message\":\"Failed to file upload to Firebase\",\"timestamp\":\"2024-05-30 15:00:00\"}"
                                            )
                                    }
                            )
                    )
            }
    )
    @PatchMapping(value = "/{userId}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestParam("profile") MultipartFile file,
            @PathVariable(value = "userId", required = false) long userId) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if (file.isEmpty()) throw new CustomException(ErrorCode.REQUIRED_IMAGE);

        long accessUserId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        if (accessUserId != userId)
            throw new CustomException(ErrorCode.INVALID_TOKEN_USER);

        userService.updateProfile(file, userId);
        return ResponseEntity.noContent().build();
    }
}


