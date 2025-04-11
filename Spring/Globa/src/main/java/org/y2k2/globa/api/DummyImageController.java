package org.y2k2.globa.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.dummyimage.dto.request.RequestDummyImageDto;
import org.y2k2.globa.application.dummyimage.dto.response.ResponseDummyImageDto;
import org.y2k2.globa.application.dummyimage.service.CreateDummyImageService;
import org.y2k2.globa.common.exception.SwaggerErrorCode;

@RestController
@RequestMapping("/dummy")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "DummyImage", description = "임시 이미지 관련 API입니다.")
public class DummyImageController {
    private final CreateDummyImageService createDummyImageService;

    @Operation(
            summary = "공지 이미지 추가",
            description = """
                    공지 이미지를 추가합니다. <br />
                    임시 저장된 이미지는 1달 뒤에 자동 삭제되며, 게시글 추가 요청을 할 때 사용됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이미지 추가 성공",
                            content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDummyImageDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE, ref = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.FAILED_FILE_UPLOAD, ref = SwaggerErrorCode.FAILED_FILE_UPLOAD_VALUE),
                    }))
            }
    )
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDummyImageDto> addDummyImage(
            @Valid RequestDummyImageDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        return ResponseEntity.ok().body(createDummyImageService.create(dto, details.getUserId()));
    }
}
