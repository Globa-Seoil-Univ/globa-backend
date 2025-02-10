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
import org.y2k2.globa.annotation.VerifyUser;
import org.y2k2.globa.dto.request.user.RequestRTRDto;
import org.y2k2.globa.dto.response.analysis.ResponseAnalysisDto;
import org.y2k2.globa.dto.request.user.RequestNotificationSettingDto;
import org.y2k2.globa.dto.request.fcm.RequestNotificationTokenDto;
import org.y2k2.globa.dto.request.survey.RequestSurveyDto;
import org.y2k2.globa.dto.request.user.RequestUserPostDTO;
import org.y2k2.globa.dto.response.user.ResponseUserDto;
import org.y2k2.globa.dto.response.user.ResponseUserSearchDto;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.UserService;
import org.y2k2.globa.util.jwt.JWT;
import org.y2k2.globa.util.jwt.JWTProvider;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("user")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API입니다.")
public class UserController {
    private final UserService userService;
    private final JWTProvider jwtTokenProvider;

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
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JWT.class))
                    ),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.INVALID_SNS_TOKEN, ref = SwaggerErrorCode.INVALID_SNS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping
    public ResponseEntity<?> signup(@Valid @RequestBody RequestUserPostDTO requestUserPostDTO) {
        JWT jwtToken = userService.signup(requestUserPostDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(jwtToken);
    }

    @Operation(
            summary = "Access Token 갱신",
            description = "Refresh Token을 사용하여 Access Token을 갱신합니다.\nRTR 기법을 사용하기 때문에 사용한 Refresh Token은 폐기 처리됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Access Token 갱신 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JWT.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.ACTIVE_ACCESS_TOKEN, ref = SwaggerErrorCode.ACTIVE_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_MATCH_REFRESH_TOKEN, ref = SwaggerErrorCode.NOT_MATCH_REFRESH_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_REFRESH_TOKEN, ref = SwaggerErrorCode.EXPIRED_REFRESH_TOKEN_VALUE)
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/refresh")
    public ResponseEntity<?> reloadRefreshToken(@Valid @RequestBody RequestRTRDto dto,
                                      @Parameter(hidden = true)
                                      @RequestHeader(value = "Authorization", required = false) String accessToken) {
        JWT jwtToken = userService.reloadRefreshToken(accessToken, dto.refreshToken());
        return ResponseEntity.ok(jwtToken);
    }

    @Operation(
            summary = "내 정보 가져오기",
            description = "Access Token을 사용하여 내 정보를 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 정보 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseUserDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_DEFAULT_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_DEFAULT_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping
    @VerifyUser
    public ResponseEntity<?> getUser(UserEntity user) {
        return ResponseEntity.ok(userService.getUser(user));
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
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE)
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/search")
    @VerifyUser
    public ResponseEntity<?> getUserSearch(@RequestParam(value = "code", required = false) String code) {
        return ResponseEntity.ok(userService.searchUser(code));
    }

    @Operation(
            summary = "내 알림 정보 가져오기",
            description = "Access Token을 사용하여 내 알림 정보를 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "내 알림 정보 가져오기 완료",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RequestNotificationSettingDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_NOFI_OWNER, ref = SwaggerErrorCode.MISMATCH_NOFI_OWNER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/{user_id}/notification")
    public ResponseEntity<?> getUserNotification(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable(value = "user_id", required = false) Long userId) {

        RequestNotificationSettingDto result = userService.getNotification(accessToken,userId);

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
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_ANALYSIS_OWNER, ref = SwaggerErrorCode.MISMATCH_ANALYSIS_OWNER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/{user_id}/analysis")
    public ResponseEntity<?> getAnalysis(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable(value = "user_id", required = false) Long userId) {

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
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RequestNotificationSettingDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOFI_POST_BAD_REQUEST, ref = SwaggerErrorCode.NOFI_POST_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_NOFI_OWNER, ref = SwaggerErrorCode.MISMATCH_NOFI_OWNER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PutMapping("/{user_id}/notification")
    public ResponseEntity<?> putNotification(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable(value = "user_id", required = false) Long userId,
            @RequestBody RequestNotificationSettingDto settingDto) {
        if ( settingDto.getEventNofi() == null || settingDto.getUploadNofi() == null  || settingDto.getShareNofi() == null  )
            throw new CustomException(ErrorCode.NOFI_POST_BAD_REQUEST);

        RequestNotificationSettingDto result = userService.putNotification(accessToken,userId, settingDto);

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
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RENAME_OWNER, ref = SwaggerErrorCode.MISMATCH_RENAME_OWNER_VALUE)
                    })),
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
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.SURVEY_POST_BAD_REQUEST, ref = SwaggerErrorCode.SURVEY_POST_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping
    public ResponseEntity<?> deleteUser(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestBody RequestSurveyDto requestSurveyDto) {
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
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE)
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.INVALID_TOKEN_USER, ref = SwaggerErrorCode.INVALID_TOKEN_USER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PutMapping("/{userId}/notification/token")
    public ResponseEntity<?> updateNotificationToken(
            @RequestHeader(value = "Authorization") String accessToken,
            @Valid @RequestBody RequestNotificationTokenDto dto,
            @PathVariable(value = "userId", required = false) long userId) {
        long accessUserId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
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
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE)
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.INVALID_TOKEN_USER, ref = SwaggerErrorCode.INVALID_TOKEN_USER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/{userId}/notification/token")
    public ResponseEntity<?> postNotificationToken(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization") String accessToken,
            @Valid @RequestBody RequestNotificationTokenDto dto,
            @PathVariable(value = "userId", required = false) long userId) {
        long accessUserId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
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
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_IMAGE, ref = SwaggerErrorCode.REQUIRED_IMAGE_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE)
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.INVALID_TOKEN_USER, ref = SwaggerErrorCode.INVALID_TOKEN_USER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.FAILED_FILE_UPLOAD, ref = SwaggerErrorCode.FAILED_FILE_UPLOAD_VALUE),
                    })),
            }
    )
    @PatchMapping(value = "/{userId}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestParam("profile") MultipartFile file,
            @PathVariable(value = "userId", required = false) long userId) {
        if (file.isEmpty()) throw new CustomException(ErrorCode.REQUIRED_IMAGE);

        long accessUserId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        if (accessUserId != userId)
            throw new CustomException(ErrorCode.INVALID_TOKEN_USER);

        userService.updateProfile(file, userId);
        return ResponseEntity.noContent().build();
    }
}


