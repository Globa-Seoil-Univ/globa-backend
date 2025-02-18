package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.common.auth.CustomUserDetails;
import org.y2k2.globa.dto.request.foldershare.RequestInviteDto;
import org.y2k2.globa.dto.response.foldershare.ResponseFolderShareUserDto;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.type.Role;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.FolderShareService;
import org.y2k2.globa.util.jwt.JWTProvider;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/folder/{folderId}/share")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Folder Share", description = "공유 관련 API입니다.")
public class FolderShareController {
    private final FolderShareService folderShareService;
    private final JWTProvider jwtTokenProvider;

    @Operation(
            summary = "공유된 사용자 조회",
            description = "폴더에 공유된 사용자를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공유된 사용자 조회 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseFolderShareUserDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.ROLE_BAD_REQUEST, ref = SwaggerErrorCode.ROLE_BAD_REQUEST_VALUE),
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/user")
    public ResponseEntity<?> getShareUsers(
            @PathVariable(value = "folderId") Long folderId,
            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
            @RequestParam(value = "count", defaultValue = "100", required = false) int count,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        ResponseFolderShareUserDto folderShareUserResponseDto = folderShareService.getShares(folderId, page, count, details.getUser());
        return ResponseEntity.ok().body(folderShareUserResponseDto);
    }

    @Operation(
            summary = "사용자 공유 초대",
            description = "폴더에 사용자를 공유 초대를 보냅니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "공유 초대 요청 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.INVITE_BAD_REQUEST, ref = SwaggerErrorCode.INVITE_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_TARGET_USER, ref = SwaggerErrorCode.NOT_FOUND_TARGET_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "409", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SHARE_USER_DUPLICATED, ref = SwaggerErrorCode.SHARE_USER_DUPLICATED_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> inviteShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId,
            @Valid @RequestBody RequestInviteDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        folderShareService.inviteShare(folderId, targetId, dto, details.getUser());
        return ResponseEntity.created(URI.create("/folder/" + folderId + "/share/user")).build();
    }

    @Operation(
            summary = "사용자 공유 초대 변경",
            description = "폴더에 사용자를 공유 초대를 변경합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "공유 초대 변경 완료"
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_TARGET_USER, ref = SwaggerErrorCode.NOT_FOUND_TARGET_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SHARE, ref = SwaggerErrorCode.NOT_FOUND_SHARE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PatchMapping("/user/{userId}")
    public ResponseEntity<?> editShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId,
            @Valid @RequestBody RequestInviteDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        folderShareService.editInviteShare(folderId, targetId, dto, details.getUser());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "사용자 공유 초대 취소",
            description = "폴더에 사용자를 공유 초대를 취소합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "공유 초대 취소 완료"
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_TARGET_USER, ref = SwaggerErrorCode.NOT_FOUND_TARGET_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SHARE, ref = SwaggerErrorCode.NOT_FOUND_SHARE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        folderShareService.deleteInviteShare(folderId, targetId, details.getUser());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "공유 초대 수락",
            description = "공유 초대를 수락합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공유 초대 수락 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST, ref = SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_ID, ref = SwaggerErrorCode.MISMATCH_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCEPT_INVITATION, ref = SwaggerErrorCode.NOT_DESERVE_ACCEPT_INVITATION_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_TARGET_USER, ref = SwaggerErrorCode.NOT_FOUND_TARGET_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SHARE, ref = SwaggerErrorCode.NOT_FOUND_SHARE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/{shareId}")
    public ResponseEntity<?> acceptShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "shareId") Long shareId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        folderShareService.acceptShare(folderId, shareId, details.getUser());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "공유 초대 거절",
            description = "공유 초대를 거절합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "공유 초대 거절 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST, ref = SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_ID, ref = SwaggerErrorCode.MISMATCH_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCEPT_INVITATION, ref = SwaggerErrorCode.NOT_DESERVE_ACCEPT_INVITATION_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> refuseShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "shareId") Long shareId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        folderShareService.refuseShare(folderId, shareId, details.getUser());
        return ResponseEntity.noContent().build();
    }
}
