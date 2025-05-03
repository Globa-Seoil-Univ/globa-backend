package org.y2k2.globa.api;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.fcm.dto.request.RequestNotificationTokenDto;
import org.y2k2.globa.application.survey.dto.request.RequestSurveyDto;
import org.y2k2.globa.application.user.dto.request.*;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.application.user.service.*;
import org.y2k2.globa.common.exception.SwaggerErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
@ResponseBody
@Tag(name = "User", description = "사용자 관련 API입니다.")
public class UserController {
    private final GetUserService getUserService;
    private final GetSearchUserService getSearchUserService;
    private final GetUserNotificationService getUserNotificationService;
    private final GetUserAnalysisService getUserAnalysisService;

    private final CreateUserService createUserService;
    private final ReissueTokenService reissueTokenService;

    private final UpsertFcmService upsertFcmService;
    private final UpdateUserNameService updateUserNameService;
    private final UpdateUserProfileImgService updateUserProfileImgService;
    private final UpdateUserNotificationService updateUserNotificationService;

    private final DeleteUserService deleteUserService;

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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE)
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE_VALUE),
                    })),
            }
    )
    @GetMapping
    public ResponseEntity<ResponseUserDto> getUser(@AuthenticationPrincipal CustomUserDetails details) {
        return ResponseEntity.ok(getUserService.getUser(details.getUserId()));
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
    public ResponseEntity<ResponseUserSearchDto> getUserSearch(@RequestParam(value = "code", required = false) String code) {
        return ResponseEntity.ok(getSearchUserService.getSearchUser(code));
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
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
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
    @GetMapping("/notification")
    public ResponseEntity<ResponseNotificationSettingDto> getUserNotification(@AuthenticationPrincipal CustomUserDetails details) {
        return ResponseEntity.ok(getUserNotificationService.getUserNotification(details.getUserId()));
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
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_ANALYSIS_OWNER, ref = SwaggerErrorCode.MISMATCH_ANALYSIS_OWNER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/analysis")
    public ResponseEntity<ResponseAnalysisDto> getAnalysis(@AuthenticationPrincipal CustomUserDetails details) {
        return ResponseEntity.ok(getUserAnalysisService.getAnalysis(details.getUserId()));
    }

    @Operation(
            summary = "회원 가입과 로그인",
            description = """
                    요청한 snsKind, snsId, name이 없다면 회원 가입을 진행하고 있다면 로그인을 시도합니다. <br>
                    snsId는 1001 ~ 1004 사이의 값만 허용합니다. <br>
                    name은 30자 이하로 제한합니다. <br>
                    회원 가입 및 로그인에 성공하면 Access Token을 반환합니다.""",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "회원 가입 또는 로그인 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JWT.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.INVALID_SNS_KIND, ref = SwaggerErrorCode.INVALID_SNS_KIND_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.INVALID_SNS_TOKEN, ref = SwaggerErrorCode.INVALID_SNS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER_ROLE, ref = SwaggerErrorCode.NOT_FOUND_USER_ROLE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE_VALUE),
                    })),
            }
    )
    @PostMapping
    public ResponseEntity<JWT> signup(@Valid @RequestBody RequestUserPostDTO dto) {
        JWT jwtToken = createUserService.signupOrLogin(dto);
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
    public ResponseEntity<JWT> reissueToken(@Valid @RequestBody RequestRTRDto dto,
                                                @Parameter(hidden = true)
                                                @RequestHeader(value = "Authorization", required = false) String accessToken) {
        JWT jwtToken = reissueTokenService.reissue(accessToken, dto.refreshToken());
        return ResponseEntity.ok(jwtToken);
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
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @RequestMapping(path = "/notification/token", method = { RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<Void> upsertFcmToken(
            @Valid @RequestBody RequestNotificationTokenDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        upsertFcmService.upsert(dto, details.getUserId());
        return ResponseEntity.noContent().build();
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
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
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
    @PutMapping("/notification")
    public ResponseEntity<Void> modifyNotification(
            @Valid @RequestBody RequestNotificationSettingDto settingDto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        updateUserNotificationService.update(settingDto, details.getUserId());
        return ResponseEntity.noContent().build();
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
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE_VALUE),
                    })),
            }
    )
    @PatchMapping("/name")
    public ResponseEntity<Void> modifyUsername(
            @Valid @RequestBody RequestNameDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        updateUserNameService.update(dto, details.getUserId());
        return ResponseEntity.noContent().build();
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
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE)
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.FAILED_FILE_UPLOAD, ref = SwaggerErrorCode.FAILED_FILE_UPLOAD_VALUE),
                    })),
            }
    )
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> modifyProfileImg(
            @Valid RequestProfileImageDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        updateUserProfileImgService.update(dto, details.getUserId());
        return ResponseEntity.noContent().build();
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
                    })),
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
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            @Valid @RequestBody RequestSurveyDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        deleteUserService.delete(dto, details.getUserId());
        return ResponseEntity.noContent().build();
    }
}


