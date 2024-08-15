package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.y2k2.globa.dto.ResponseDummyImageDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.DummyImageService;
import org.y2k2.globa.util.JwtTokenProvider;

@RestController
@RequestMapping("/dummy")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "DummyImage", description = "임시 이미지 관련 API입니다.")
public class DummyImageController {
    private final DummyImageService dummyImageService;

    @Operation(
            summary = "더미 이미지 추가",
            description = """
                    더미 이미지를 추가합니다. <br />
                    임시 저장된 이미지는 1달 뒤에 자동 삭제되며, 게시글 추가 요청을 할 때 사용됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이미지 추가 성공",
                            content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDummyImageDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_IMAGE, ref = SwaggerErrorCode.REQUIRED_IMAGE_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
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
    public ResponseEntity<?> addDummyImage(@Parameter(hidden = true) @RequestHeader(value = "Authorization") String accessToken, @RequestParam("image") MultipartFile file) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        if (file.isEmpty()) throw new CustomException(ErrorCode.REQUIRED_IMAGE);

        ResponseDummyImageDto responseDto = dummyImageService.addDummyImage(accessToken, file);
        return ResponseEntity.ok().body(responseDto);
    }
}
