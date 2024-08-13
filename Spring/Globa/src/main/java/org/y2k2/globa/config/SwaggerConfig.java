package org.y2k2.globa.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.SwaggerErrorCode;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI SwaggerConfig() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );

        addResponse(components);

        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("Globa API")
                .description("Globa API Documents")
                .version("1.0.0");
    }

    private void addResponse(Components components) {
        Schema exceptionSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(CustomException.class)).schema;

        // 400 Errors
        components.addResponses("400", createApiResponse(exceptionSchema, "Bad Request", createExample("400", "~~~ 인자가 필요 또는 잘못되었습니다.")));
        components.addExamples(SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE, createExample(SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE, "만료된 인증 토큰입니다."));
        components.addExamples(SwaggerErrorCode.ACTIVE_REFRESH_TOKEN_VALUE, createExample(SwaggerErrorCode.ACTIVE_REFRESH_TOKEN_VALUE, "갱신 토큰이 아직 만료되지 않았습니다."));
        components.addExamples(SwaggerErrorCode.NOT_MATCH_REFRESH_TOKEN_VALUE, createExample(SwaggerErrorCode.NOT_MATCH_REFRESH_TOKEN_VALUE, "갱신 토큰이 일치하지 않습니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE, createExample(SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE, "액세스 토큰은 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_REQUEST_TOKEN_VALUE, createExample(SwaggerErrorCode.REQUIRED_REQUEST_TOKEN_VALUE, "요청 토큰은 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_USER_CODE_VALUE, createExample(SwaggerErrorCode.REQUIRED_USER_CODE_VALUE, "사용자 코드는 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_USER_ID_VALUE, createExample(SwaggerErrorCode.REQUIRED_USER_ID_VALUE, "사용자 ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.FOLDER_DELETE_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.FOLDER_DELETE_BAD_REQUEST_VALUE, "기본 폴더는 삭제할 수 없습니다."));
        components.addExamples(SwaggerErrorCode.DELETED_USER_VALUE, createExample(SwaggerErrorCode.DELETED_USER_VALUE, "삭제된 사용자입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_FOLDER_TITLE_VALUE, createExample(SwaggerErrorCode.REQUIRED_FOLDER_TITLE_VALUE, "폴더 제목이 필요합니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE, createExample(SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE, "폴더 ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_QUIZ_ID_VALUE, createExample(SwaggerErrorCode.REQUIRED_QUIZ_ID_VALUE, "퀴즈 ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE, createExample(SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE, "레코드 ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_QUIZ_VALUE, createExample(SwaggerErrorCode.REQUIRED_QUIZ_VALUE, "퀴즈 ID와 정답 여부는 필수입니다."));
        components.addExamples(SwaggerErrorCode.RECORD_POST_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.RECORD_POST_BAD_REQUEST_VALUE, "음성 파일에 대한 정보(경로, 제목, 크기)들은 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_RECORD_TITLE_VALUE, createExample(SwaggerErrorCode.REQUIRED_RECORD_TITLE_VALUE, "음성에 대한 제목은 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_MOVE_ARRIVED_ID_VALUE, createExample(SwaggerErrorCode.REQUIRED_MOVE_ARRIVED_ID_VALUE, "이동할 폴더 ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.INVITE_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.INVITE_BAD_REQUEST_VALUE, "자신을 초대할 수 없습니다."));
        components.addExamples(SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST_VALUE, "이미 초대된 사용자입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_NOTICE_ID_VALUE, createExample(SwaggerErrorCode.REQUIRED_NOTICE_ID_VALUE, "공지 ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.NOFI_POST_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.NOFI_POST_BAD_REQUEST_VALUE, "알림 정보는 모두 필수입니다."));
        components.addExamples(SwaggerErrorCode.SURVEY_POST_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.SURVEY_POST_BAD_REQUEST_VALUE, "설문 정보는 모두 필수입니다."));
        components.addExamples(SwaggerErrorCode.NOT_PARENT_COMMENT_VALUE, createExample(SwaggerErrorCode.NOT_PARENT_COMMENT_VALUE, "부모 댓글 ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_IMAGE_VALUE, createExample(SwaggerErrorCode.REQUIRED_IMAGE_VALUE, "이미지는 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_ROLE_VALUE, createExample(SwaggerErrorCode.REQUIRED_ROLE_VALUE, "초대하고자 하는 사용자의 권한 설정은 필수입니다."));
        components.addExamples(SwaggerErrorCode.ROLE_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.ROLE_BAD_REQUEST_VALUE, "role의 값은 r 또는 w 값이어야만 합니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_SNS_KIND_VALUE, createExample(SwaggerErrorCode.REQUIRED_SNS_KIND_VALUE, "SNS 분류 ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_SNS_ID_VALUE, createExample(SwaggerErrorCode.REQUIRED_SNS_ID_VALUE, "SNS ID는 필수입니다."));
        components.addExamples(SwaggerErrorCode.REQUIRED_NAME_VALUE, createExample(SwaggerErrorCode.REQUIRED_NAME_VALUE, "사용자 이름은 필수입니다."));
        components.addExamples(SwaggerErrorCode.SNS_KIND_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.SNS_KIND_BAD_REQUEST_VALUE, "SNS 종류 ID는 1001 ~ 1004이어야 합니다."));
        components.addExamples(SwaggerErrorCode.NAME_BAD_REQUEST_VALUE, createExample(SwaggerErrorCode.NAME_BAD_REQUEST_VALUE, "이름은 32글자 이하이어야 합니다."));

        // 401 Errors
        components.addExamples(SwaggerErrorCode.INVALID_TOKEN_VALUE, createExample(SwaggerErrorCode.INVALID_TOKEN_VALUE, "유효하지 않은 토큰입니다."));
        components.addExamples(SwaggerErrorCode.SIGNATURE_VALUE, createExample(SwaggerErrorCode.SIGNATURE_VALUE, "토큰 파싱에 실패하였습니다."));
        components.addExamples(SwaggerErrorCode.NOT_DESERVE_FOLDER_VALUE, createExample(SwaggerErrorCode.NOT_DESERVE_FOLDER_VALUE, "폴더 접근 권한이 없습니다."));
        components.addExamples(SwaggerErrorCode.EXPIRED_REFRESH_TOKEN_VALUE, createExample(SwaggerErrorCode.EXPIRED_REFRESH_TOKEN_VALUE, "만료된 갱신 토큰입니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_INQUIRY_OWNER_VALUE, createExample(SwaggerErrorCode.MISMATCH_INQUIRY_OWNER_VALUE, "문의 작성자만 요청할 수 있습니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE, createExample(SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE, "폴더 소유자 요청할 수 있습니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_COMMENT_OWNER_VALUE, createExample(SwaggerErrorCode.MISMATCH_COMMENT_OWNER_VALUE, "댓글 작성자만 요청할 수 있습니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_NOFI_OWNER_VALUE, createExample(SwaggerErrorCode.MISMATCH_NOFI_OWNER_VALUE, "요청한 사용자와 토큰 정보가 일치하지 않습니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_ANALYSIS_OWNER_VALUE, createExample(SwaggerErrorCode.MISMATCH_ANALYSIS_OWNER_VALUE, "요청한 사용자와 토큰 정보가 일치하지 않습니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_RENAME_OWNER_VALUE, createExample(SwaggerErrorCode.MISMATCH_RENAME_OWNER_VALUE, "이름 변경을 위한 사용자와 토큰 정보가 일치하지 않습니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_QUIZ_RECORD_ID_VALUE, createExample(SwaggerErrorCode.MISMATCH_QUIZ_RECORD_ID_VALUE, "퀴즈의 음성 ID와 요청한 음성 ID 정보가 일치하지 않습니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE, createExample(SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE, "음성 소유자만 요청할 수 있습니다."));
        components.addExamples(SwaggerErrorCode.MISMATCH_RECORD_FOLDER_VALUE, createExample(SwaggerErrorCode.MISMATCH_RECORD_FOLDER_VALUE, "요청한 음성 파일은 해당 폴더에 속해있지 않습니다."));

        // 403 Errors
        components.addExamples(SwaggerErrorCode.NOT_NULL_ROLE_VALUE, createExample(SwaggerErrorCode.NOT_NULL_ROLE_VALUE, "사용자 권한이 존재하지 않습니다."));
        components.addExamples(SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE_VALUE, createExample(SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE_VALUE, "공지 추가 권한이 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE, createExample(SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE, "폴더 접근 권한이 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_DESERVE_MODIFY_INVITATION_VALUE, createExample(SwaggerErrorCode.NOT_DESERVE_MODIFY_INVITATION_VALUE, "초대 수정 권한이 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE, createExample(SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE, "댓글 작성 권한이 없습니다."));
        components.addExamples(SwaggerErrorCode.INVALID_TOKEN_USER_VALUE, createExample(SwaggerErrorCode.INVALID_TOKEN_USER_VALUE, "잘못된 토큰 사용자입니다."));

        // 404 Errors
        components.addExamples(SwaggerErrorCode.NOT_FOUND_USER_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_USER_VALUE, "사용자를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_DEFAULT_FOLDER_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_DEFAULT_FOLDER_VALUE, "기본 폴더를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_TARGET_USER_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_TARGET_USER_VALUE, "대상 사용자를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_INQUIRY_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_INQUIRY_VALUE, "문의 사항을 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_NOTICE_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_NOTICE_VALUE, "공지를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_ANSWER_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_ANSWER_VALUE, "답변을 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE, "폴더를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER_VALUE, "접근 가능한 폴더를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_ORIGIN_FOLDER_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_ORIGIN_FOLDER_VALUE, "원본 폴더를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_TARGET_FOLDER_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_TARGET_FOLDER_VALUE, "대상 폴더를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_SHARE_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_SHARE_VALUE, "공유 초대를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_FOLDER_FIREBASE_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_FOLDER_FIREBASE_VALUE, "폴더를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE, "하이라이트를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT_VALUE, "부모 댓글을 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_RECORD_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_RECORD_VALUE, "레코드를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_ANALYSIS_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_ANALYSIS_VALUE, "분석을 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_QUIZ_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_QUIZ_VALUE, "퀴즈를 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE_VALUE, "음성을 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_SECTION_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_SECTION_VALUE, "섹션을 찾을 수 없습니다."));
        components.addExamples(SwaggerErrorCode.NOT_FOUND_COMMENT_VALUE, createExample(SwaggerErrorCode.NOT_FOUND_COMMENT_VALUE, "댓글을 찾을 수 없습니다."));

        // 409 Errors
        components.addExamples(SwaggerErrorCode.HIGHLIGHT_DUPLICATED_VALUE, createExample(SwaggerErrorCode.HIGHLIGHT_DUPLICATED_VALUE, "댓글 하이라이트 중복입니다."));
        components.addExamples(SwaggerErrorCode.FOLDER_NAME_DUPLICATED_VALUE, createExample(SwaggerErrorCode.FOLDER_NAME_DUPLICATED_VALUE, "폴더 이름이 이미 존재합니다."));
        components.addExamples(SwaggerErrorCode.INQUIRY_ANSWER_DUPLICATED_VALUE, createExample(SwaggerErrorCode.INQUIRY_ANSWER_DUPLICATED_VALUE, "문의 답변이 이미 존재합니다."));
        components.addExamples(SwaggerErrorCode.SHARE_USER_DUPLICATED_VALUE, createExample(SwaggerErrorCode.SHARE_USER_DUPLICATED_VALUE, "이미 초대한 사용자입니다."));

        // 500 Errors
        components.addResponses("500", createApiResponse(exceptionSchema, "Internal Server Error", createExample(SwaggerErrorCode.INTERNAL_SERVER_ERROR_VALUE, "서버 내부 오류가 발생하였습니다.")));
        components.addExamples(SwaggerErrorCode.FAILED_FILE_UPLOAD_VALUE, createExample(SwaggerErrorCode.FAILED_FILE_UPLOAD_VALUE, "파이어베이스 파일 업로드 오류가 발생하였습니다."));
        components.addExamples(SwaggerErrorCode.FAILED_FCM_SEND_VALUE, createExample(SwaggerErrorCode.FAILED_FCM_SEND_VALUE, "FCM 오류가 발생하였습니다."));
        components.addExamples(SwaggerErrorCode.REDIS_TIMEOUT_VALUE, createExample(SwaggerErrorCode.REDIS_TIMEOUT_VALUE, "레디스에 연결하지 못했습니다."));
        components.addExamples(SwaggerErrorCode.FAILED_FOLDER_CREATE_VALUE, createExample(SwaggerErrorCode.FAILED_FOLDER_CREATE_VALUE, "파이어베이스 폴더 생성 오류가 발생하였습니다."));
        components.addExamples(SwaggerErrorCode.FAILED_FOLDER_DELETE_VALUE, createExample(SwaggerErrorCode.FAILED_FOLDER_DELETE_VALUE, "파이어베이스 폴더 삭제 오류가 발생하였습니다."));
        components.addExamples(SwaggerErrorCode.FAILED_FIREBASE_VALUE, createExample(SwaggerErrorCode.FAILED_FIREBASE_VALUE, "예기치 못한 파이어베이스 오류가 발생하였습니다."));
    }

    private ApiResponse createApiResponse(Schema schema, String description, Example example) {
        return new ApiResponse().content(
                new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType()
                                .schema(schema)
                                .addExamples("default", example)
                )
        ).description(description);
    }

    private Example createExample(String errorCode, String message) {
        return new Example().value(String.format("{\"errorCode\":%s,\"message\":\"%s\",\"timestamp\":\"2024-05-30 15:00:00\"}", errorCode, message));
    }
}
