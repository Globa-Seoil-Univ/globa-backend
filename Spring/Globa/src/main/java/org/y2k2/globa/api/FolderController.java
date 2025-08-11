package org.y2k2.globa.api;

import io.swagger.v3.oas.annotations.Operation;
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
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.folder.dto.common.FolderDto;
import org.y2k2.globa.application.folder.dto.request.RequestFolderNameDto;
import org.y2k2.globa.application.folder.dto.request.RequestFolderPostDto;
import org.y2k2.globa.application.folder.dto.response.ResponseFolderDto;
import org.y2k2.globa.application.folder.service.CreateFolderService;
import org.y2k2.globa.application.folder.service.DeleteFolderService;
import org.y2k2.globa.application.folder.service.GetFoldersService;
import org.y2k2.globa.application.folder.service.UpdateFolderNameService;
import org.y2k2.globa.common.exception.SwaggerErrorCode;

import java.net.URI;

@RestController
@RequestMapping("/folder")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Folder", description = "폴더 관련 API입니다.")
public class FolderController {
    private final GetFoldersService getFoldersService;
    private final CreateFolderService createFolderService;
    private final UpdateFolderNameService updateFolderNameService;
    private final DeleteFolderService deleteFolderService;

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
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE_VALUE),
                    }))
            }
    )
    @GetMapping
    public ResponseEntity<ResponseFolderDto> getFolders(
            @RequestParam(required = false, defaultValue = "1", value = "page") int page,
            @RequestParam(required = false, defaultValue = "10", value = "count") int count,
            @AuthenticationPrincipal CustomUserDetails details
    )
    { return ResponseEntity.status(HttpStatus.OK).body(getFoldersService.getFolders(page, count, details.getUserId())); }

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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE_VALUE),
                    }))
            }
    )
    @PostMapping
    public ResponseEntity<Void> createFolder(
            @Valid @RequestBody RequestFolderPostDto request,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        if (request.getShareTargets() == null) {
            createFolderService.create(request.getTitle(), details.getUserId());
        } else {
            createFolderService.create(request.getTitle(), request.getShareTargets(), details.getUserId());
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
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref="500")
            }
    )
    @PatchMapping("/{folderId}/name")
    public ResponseEntity<Void> updateFolderName(
            @PathVariable(value = "folderId") Long folderId,
            @Valid @RequestBody RequestFolderNameDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        updateFolderNameService.update(folderId, dto.title(), details.getUserId());
        return ResponseEntity.noContent().build();
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
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE)
                    })),
                    @ApiResponse(responseCode = "500", ref="500")
            }
    )
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable(value = "folderId") Long folderId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        deleteFolderService.delete(folderId, details.getUserId());
        return ResponseEntity.noContent().build();
    }
}
