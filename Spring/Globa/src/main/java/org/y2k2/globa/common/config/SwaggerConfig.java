package org.y2k2.globa.common.config;

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

import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

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
        components.addExamples(ErrorCode.EXPIRED_ACCESS_TOKEN.getErrorCode(), createExample(ErrorCode.EXPIRED_ACCESS_TOKEN.getErrorCode(), "만료된 인증 토큰입니다."));
        components.addExamples(ErrorCode.ACTIVE_ACCESS_TOKEN.getErrorCode(), createExample(ErrorCode.ACTIVE_ACCESS_TOKEN.getErrorCode(), "액세스 토큰이 아직 만료되지 않았습니다."));
        components.addExamples(ErrorCode.NOT_MATCH_REFRESH_TOKEN.getErrorCode(), createExample(ErrorCode.NOT_MATCH_REFRESH_TOKEN.getErrorCode(), "갱신 토큰이 일치하지 않습니다."));
        components.addExamples(ErrorCode.INVITE_BAD_REQUEST.getErrorCode(), createExample(ErrorCode.INVITE_BAD_REQUEST.getErrorCode(), "자신을 초대할 수 없습니다."));
        components.addExamples(ErrorCode.INVITE_ACCEPT_BAD_REQUEST.getErrorCode(), createExample(ErrorCode.INVITE_ACCEPT_BAD_REQUEST.getErrorCode(), "이미 초대된 사용자입니다."));
        components.addExamples(ErrorCode.MISMATCH_FOLDER_ID.getErrorCode(), createExample(ErrorCode.MISMATCH_FOLDER_ID.getErrorCode(), "요청한 Folder Id와 DB의 정보가 일치하지 않습니다."));
        components.addExamples(ErrorCode.INVALID_SNS_KIND.getErrorCode(), createExample(ErrorCode.INVALID_SNS_KIND.getErrorCode(), "유효하지 않은 Sns Kind입니다."));

        // 401 Errors
        components.addExamples(ErrorCode.INVALID_TOKEN.getErrorCode(), createExample(ErrorCode.INVALID_TOKEN.getErrorCode(), "유효하지 않은 토큰입니다."));
        components.addExamples(ErrorCode.SIGNATURE.getErrorCode(), createExample(ErrorCode.SIGNATURE.getErrorCode(), "토큰 파싱에 실패하였습니다."));
        components.addExamples(ErrorCode.EXPIRED_REFRESH_TOKEN.getErrorCode(), createExample(ErrorCode.EXPIRED_REFRESH_TOKEN.getErrorCode(), "만료된 갱신 토큰입니다."));

        // 403 Errors
        components.addExamples(ErrorCode.NOT_DESERVE_ADD_NOTICE.getErrorCode(), createExample(ErrorCode.NOT_DESERVE_ADD_NOTICE.getErrorCode(), "공지 추가 권한이 없습니다."));
        components.addExamples(ErrorCode.NOT_DESERVE_ACCESS_FOLDER.getErrorCode(), createExample(ErrorCode.NOT_DESERVE_ACCESS_FOLDER.getErrorCode(), "폴더 접근 권한이 없습니다."));
        components.addExamples(ErrorCode.NOT_DESERVE_POST_COMMENT.getErrorCode(), createExample(ErrorCode.NOT_DESERVE_POST_COMMENT.getErrorCode(), "댓글 작성 권한이 없습니다."));
        components.addExamples(ErrorCode.NOT_DESERVE_FCM.getErrorCode(), createExample(ErrorCode.NOT_DESERVE_FCM.getErrorCode(), "관리자 또는 편집자만 요청할 수 있습니다."));
        components.addExamples(ErrorCode.NOT_DESERVE_DICTIONARY.getErrorCode(), createExample(ErrorCode.NOT_DESERVE_DICTIONARY.getErrorCode(), "관리자 또는 편집자만 요청할 수 있습니다."));
        components.addExamples(ErrorCode.NOT_DESERVE_DICTIONARY.getErrorCode(), createExample(ErrorCode.NOT_DESERVE_ACCEPT_INVITATION.getErrorCode(), "초대를 수락할 수 있는 권한이 없습니다."));
        components.addExamples(ErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION.getErrorCode(), createExample(ErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION.getErrorCode(), "알림 접근 권한이 없습니다."));

        components.addExamples(ErrorCode.MISMATCH_INQUIRY_OWNER.getErrorCode(), createExample(ErrorCode.MISMATCH_INQUIRY_OWNER.getErrorCode(), "문의 작성자만 요청할 수 있습니다."));
        components.addExamples(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode(), createExample(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode(), "폴더 소유자만 요청할 수 있습니다."));
        components.addExamples(ErrorCode.MISMATCH_ANALYSIS_OWNER.getErrorCode(), createExample(ErrorCode.MISMATCH_ANALYSIS_OWNER.getErrorCode(), "요청한 사용자와 토큰 정보가 일치하지 않습니다."));
        components.addExamples(ErrorCode.MISMATCH_QUIZ_RECORD_ID.getErrorCode(), createExample(ErrorCode.MISMATCH_QUIZ_RECORD_ID.getErrorCode(), "퀴즈의 음성 ID와 요청한 음성 ID 정보가 일치하지 않습니다."));
        components.addExamples(ErrorCode.MISMATCH_RECORD_OWNER.getErrorCode(), createExample(ErrorCode.MISMATCH_RECORD_OWNER.getErrorCode(), "음성 소유자만 요청할 수 있습니다."));
        components.addExamples(ErrorCode.MISMATCH_RECORD_FOLDER.getErrorCode(), createExample(ErrorCode.MISMATCH_RECORD_FOLDER.getErrorCode(), "요청한 음성 파일은 해당 폴더에 속해있지 않습니다."));

        components.addExamples(ErrorCode.DELETED_USER.getErrorCode(), createExample(ErrorCode.DELETED_USER.getErrorCode(), "탈퇴된 사용자입니다."));
        components.addExamples(ErrorCode.NOT_ALLOW_NOTIFICATION_SETTING.getErrorCode(), createExample(ErrorCode.NOT_ALLOW_NOTIFICATION_SETTING.getErrorCode(), "알림 설정이 허용되지 않았습니다."));

        // 404 Errors
        components.addExamples(ErrorCode.NOT_FOUND_USER.getErrorCode(), createExample(ErrorCode.NOT_FOUND_USER.getErrorCode(), "사용자를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_TARGET_USER.getErrorCode(), createExample(ErrorCode.NOT_FOUND_TARGET_USER.getErrorCode(), "대상 사용자를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_ROLE.getErrorCode(), createExample(ErrorCode.NOT_FOUND_ROLE.getErrorCode(), "권한을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_INQUIRY.getErrorCode(), createExample(ErrorCode.NOT_FOUND_INQUIRY.getErrorCode(), "문의 사항을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_NOTICE.getErrorCode(), createExample(ErrorCode.NOT_FOUND_NOTICE.getErrorCode(), "공지를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_NOTIFICATION.getErrorCode(), createExample(ErrorCode.NOT_FOUND_NOTIFICATION.getErrorCode(), "알림을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN.getErrorCode(), createExample(ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN.getErrorCode(), "알림 토큰을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_ANSWER.getErrorCode(), createExample(ErrorCode.NOT_FOUND_ANSWER.getErrorCode(), "답변을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_FOLDER.getErrorCode(), createExample(ErrorCode.NOT_FOUND_FOLDER.getErrorCode(), "폴더를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_TARGET_FOLDER.getErrorCode(), createExample(ErrorCode.NOT_FOUND_TARGET_FOLDER.getErrorCode(), "대상 폴더를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_SHARE.getErrorCode(), createExample(ErrorCode.NOT_FOUND_SHARE.getErrorCode(), "공유 초대를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_FOLDER_FIREBASE.getErrorCode(), createExample(ErrorCode.NOT_FOUND_FOLDER_FIREBASE.getErrorCode(), "Firebase Storage에서 폴더를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_HIGHLIGHT.getErrorCode(), createExample(ErrorCode.NOT_FOUND_HIGHLIGHT.getErrorCode(), "하이라이트를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_PARENT_COMMENT.getErrorCode(), createExample(ErrorCode.NOT_FOUND_PARENT_COMMENT.getErrorCode(), "부모 댓글을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_RECORD.getErrorCode(), createExample(ErrorCode.NOT_FOUND_RECORD.getErrorCode(), "레코드를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_ANALYSIS.getErrorCode(), createExample(ErrorCode.NOT_FOUND_ANALYSIS.getErrorCode(), "분석을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_QUIZ.getErrorCode(), createExample(ErrorCode.NOT_FOUND_QUIZ.getErrorCode(), "퀴즈를 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_RECORD_FIREBASE.getErrorCode(), createExample(ErrorCode.NOT_FOUND_RECORD_FIREBASE.getErrorCode(), "Firebase Storage에서 음성을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_SECTION.getErrorCode(), createExample(ErrorCode.NOT_FOUND_SECTION.getErrorCode(), "섹션을 찾을 수 없습니다."));
        components.addExamples(ErrorCode.NOT_FOUND_COMMENT.getErrorCode(), createExample(ErrorCode.NOT_FOUND_COMMENT.getErrorCode(), "댓글을 찾을 수 없습니다."));

        // 409 Errors
        components.addExamples(ErrorCode.HIGHLIGHT_DUPLICATED.getErrorCode(), createExample(ErrorCode.HIGHLIGHT_DUPLICATED.getErrorCode(), "댓글 하이라이트 중복입니다."));
        components.addExamples(ErrorCode.INQUIRY_ANSWER_DUPLICATED.getErrorCode(), createExample(ErrorCode.INQUIRY_ANSWER_DUPLICATED.getErrorCode(), "문의 답변이 이미 존재합니다."));
        components.addExamples(ErrorCode.SHARE_USER_DUPLICATED.getErrorCode(), createExample(ErrorCode.SHARE_USER_DUPLICATED.getErrorCode(), "이미 초대한 사용자입니다."));
        components.addExamples(ErrorCode.NOTIFICATION_READ_DUPLICATED.getErrorCode() , createExample(ErrorCode.NOTIFICATION_READ_DUPLICATED.getErrorCode() , "이미 읽은 알림입니다."));

        // 500 Errors
        components.addResponses("500", createApiResponse(exceptionSchema, "Internal Server Error", createExample(ErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(), "서버 내부 오류가 발생하였습니다.")));
        components.addExamples(ErrorCode.FAILED_FILE_UPLOAD.getErrorCode(), createExample(ErrorCode.FAILED_FILE_UPLOAD.getErrorCode(), "파이어베이스 파일 업로드 오류가 발생하였습니다."));
        components.addExamples(ErrorCode.REDIS_TIMEOUT.getErrorCode(), createExample(ErrorCode.REDIS_TIMEOUT.getErrorCode(), "레디스에 연결하지 못했습니다."));
        components.addExamples(ErrorCode.FAILED_FOLDER_CREATE.getErrorCode(), createExample(ErrorCode.FAILED_FOLDER_CREATE.getErrorCode(), "파이어베이스 폴더 생성 오류가 발생하였습니다."));
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
