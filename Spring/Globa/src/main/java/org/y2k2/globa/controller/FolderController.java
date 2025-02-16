package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import org.y2k2.globa.dto.common.auth.CustomUserDetails;
import org.y2k2.globa.dto.common.folder.FolderDto;
import org.y2k2.globa.dto.request.folder.RequestFolderPostDto;
import org.y2k2.globa.dto.response.folder.ResponseFolderDto;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.FolderService;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/folder")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Folder", description = "폴더 관련 API입니다.")
public class FolderController {

    public final FolderService folderService;

    @Operation(
            summary = "폴더 목록 조회",
            description = "폴더 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "폴더 목록 조회",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseFolderDto.class)))
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
    @GetMapping
    public ResponseEntity<?> getFolders(
            @RequestParam(required = false, defaultValue = "1", value = "page") int page,
            @RequestParam(required = false, defaultValue = "100", value = "count") int count,
            @AuthenticationPrincipal CustomUserDetails details
            ) { return ResponseEntity.status(HttpStatus.OK).body(folderService.getFolders(page, count, details.getUser())); }

    @Operation(
            summary = "폴더 추가",
            description = "폴더를 추가합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "폴더 추가 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = FolderDto.class))
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_TARGET_USER, ref = SwaggerErrorCode.NOT_FOUND_TARGET_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.FAILED_FOLDER_CREATE, ref = SwaggerErrorCode.FAILED_FOLDER_CREATE_VALUE),
                    }))
            }
    )
    @PostMapping
    public ResponseEntity<?> createFolder(
            @Valid @RequestBody RequestFolderPostDto request,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        if (request.getShareTargets() == null) {
            folderService.createFolder(request.getTitle(), details.getUser());
        } else {
            folderService.createFolder(request.getTitle(), request.getShareTargets(), details.getUser());
        }

        return ResponseEntity.created(URI.create("/folder")).build();
    }

    @Operation(
            summary = "폴더 이름 수정",
            description = "폴더 이름을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "폴더 추가 완료",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref="500")
            }
    )
    @PatchMapping("/{folder_id}/name")
    public ResponseEntity<?> patchFolder(@Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
                                        @PathVariable(value = "folder_id") Long folderId,
                                        @RequestBody Map<String, String> titleMap){
        if ( folderId == null)
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);

        return ResponseEntity.status(folderService.patchFolderName(accessToken, folderId, titleMap.get("title"))).body("");
    }

    @Operation(
            summary = "폴더 삭제",
            description = "폴더를 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "폴더 삭제 완료",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.FOLDER_DELETE_BAD_REQUEST, ref = SwaggerErrorCode.FOLDER_DELETE_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER_FIREBASE, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_FIREBASE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref="500")
            }
    )
    @DeleteMapping("/{folder_id}")
    public ResponseEntity<?> deleteFolder(@Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
                                        @PathVariable(value = "folder_id") Long folderId){
        if ( folderId == null)
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);

        return ResponseEntity.status(folderService.deleteFolderName(accessToken, folderId)).body("");
    }
}
