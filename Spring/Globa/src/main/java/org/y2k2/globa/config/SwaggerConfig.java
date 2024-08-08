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
import org.y2k2.globa.exception.*;

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
        Schema badRequestSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(BadRequestException.class)).schema;
        Schema unAuthroizedSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(UnAuthorizedException.class)).schema;
        Schema forbiddenSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ForbiddenException.class)).schema;
        Schema notFoundSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(NotFoundException.class)).schema;

        ApiResponse badRequestApiResponse = new ApiResponse().content(
                new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType()
                                .schema(badRequestSchema.description("필요한 정보가 부족 (Request 정보 부족)"))
                                .addExamples("default", new Example().value("{\"errorCode\":400,\"message\":\"You must be request to data\",\"timestamp\":\"2024-05-30 15:00:00\"}"))
                )
        );
        ApiResponse unAuthroizedApiResponse = new ApiResponse().content(
                new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType()
                                .schema(unAuthroizedSchema.description("Access Token 인증 실패"))
                                .addExamples(
                                        "default", new Example().value("{\"errorCode\":401,\"message\":\"Invalid Access token\",\"timestamp\":\"2024-05-30 15:00:00\"}")
                                )
                                .addExamples("invalid access token", new Example().value("{\"errorCode\":40120,\"message\":\"Invalid Access token\",\"timestamp\":\"2024-05-30 15:00:00\"}"))
                                .addExamples("expired access token", new Example().value("{\"errorCode\":40120,\"message\":\"Expired Access token\",\"timestamp\":\"2024-05-30 15:00:00\"}"))
                )
        );
        ApiResponse forbiddenApiResponse = new ApiResponse().content(
                new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType()
                                .schema(forbiddenSchema.description("잘못된 접근"))
                                .addExamples("default", new Example().value("{\"errorCode\":403,\"message\":\"You can not access the this folder | record | api etc...\",\"timestamp\":\"2024-05-30 15:00:00\"}"))
                )
        );
        ApiResponse notFoudnApiResponse = new ApiResponse().content(
                new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType()
                                .schema(notFoundSchema.description("요청을 통해 찾을 수 없는 정보"))
                                .addExamples("default", new Example().value("{\"errorCode\":403,\"message\":\"Not found folder | record | user etc...\",\"timestamp\":\"2024-05-30 15:00:00\"}"))
                )
        );
        ApiResponse internalApiResponse = new ApiResponse().content(
                new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType()
                                .schema(new Schema().description("서버에서 예기치 못한 오류가 발생"))
                                .addExamples("default", new Example().value("{\"errorCode\":500,\"message\":\"Internal Server Error\",\"timestamp\":\"2024-05-30 15:00:00\"}"))
                )
        );

        components.addResponses("400", badRequestApiResponse);
        components.addResponses("401", unAuthroizedApiResponse);
        components.addResponses("403", forbiddenApiResponse);
        components.addResponses("404", notFoudnApiResponse);
        components.addResponses("500", internalApiResponse);
    }
}
