package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.ResponseDictionaryDto;
import org.y2k2.globa.dto.ResponseInquiryDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.DictionaryService;

import java.net.URI;

@RestController
@RequestMapping("/dictionary")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Dictionary", description = "단어 관련 API입니다.")
public class DictionaryController {
    private final DictionaryService dictionaryService;

    @Operation(
            summary = "단어 조회",
            description = """
                    요청한 단어를 조회합니다. <br />
                    단, 단어가 많은 경우 10개만 가져옵니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "단어 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDictionaryDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping
    public ResponseEntity<?> getDictionary(@Parameter(hidden = true) @RequestHeader(value = "Authorization") String accessToken, @RequestParam(value = "keyword") String keyword) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }

        return ResponseEntity.ok().body(dictionaryService.getDictionary(accessToken, keyword));
    }

    @Operation(
            summary = "단어 추가",
            description = """
                    단어를 추가합니다. <br />
                    관리자 또는 편집자만 요청할 수 있으며, DB에 저장된 모든 단어를 제거하고, 엑셀 파일에 있는 단어를 DB에 저장합니다. <br />
                    단어 양에 따라 최소 1 ~ 5분 정도 소요됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "단어 추가 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_DICTIONARY, ref = SwaggerErrorCode.NOT_DESERVE_DICTIONARY_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping
    public ResponseEntity<?> addDictionary(@Parameter(hidden = true) @RequestHeader(value = "Authorization") String accessToken) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }

        dictionaryService.saveDictionary(accessToken);
        return ResponseEntity.created(URI.create("/dictionary?keyword=테스트")).build();
    }
}
