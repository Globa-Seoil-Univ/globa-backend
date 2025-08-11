package org.y2k2.globa.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.dto.response.ResponseFolderShareUserDto;
import org.y2k2.globa.application.foldershare.service.*;
import org.y2k2.globa.common.exception.SwaggerErrorCode;

import java.net.URI;

@RestController
@RequestMapping("/folder/{folderId}/share")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Folder Share", description = "공유 관련 API입니다.")
public class FolderShareController {
    private final GetFolderSharesService getFolderSharesService;
    private final InviteFolderShareService inviteFolderShareService;
    private final UpdateFolderShareService updateFolderShareService;
    private final DeleteFolderShareService deleteFolderShareService;
    private final AcceptInvitationService acceptInvitationService;
    private final RefuseInvitationService refuseInvitationService;

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
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/user")
    public ResponseEntity<ResponseFolderShareUserDto> getShareUsers(
            @PathVariable(value = "folderId") Long folderId,
            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        return ResponseEntity.ok().body(getFolderSharesService.get(folderId, page, count, details.getUserId()));
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
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "409", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SHARE_USER_DUPLICATED, ref = SwaggerErrorCode.SHARE_USER_DUPLICATED_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE_VALUE),
                    }))
            }
    )
    @PostMapping("/user/{userId}")
    public ResponseEntity<Void> inviteShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId,
            @Valid @RequestBody RequestInviteDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        inviteFolderShareService.invite(folderId, targetId, dto, details.getUserId());
        return ResponseEntity.created(URI.create("/folder/" + folderId + "/share/user")).build();
    }

    @Operation(
            summary = "사용자 접근 권한 변경",
            description = "특정 폴더의 사용자 접근 권한을 변경합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "공유 초대 변경 완료"
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
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SHARE, ref = SwaggerErrorCode.NOT_FOUND_SHARE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_ROLE_VALUE),
                    }))
            }
    )
    @PatchMapping("/user/{userId}")
    public ResponseEntity<Void> editShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId,
            @Valid @RequestBody RequestInviteDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        updateFolderShareService.update(folderId, targetId, dto, details.getUserId());
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
                            @ExampleObject(name = SwaggerErrorCode.INVITE_BAD_REQUEST, ref = SwaggerErrorCode.INVITE_BAD_REQUEST_VALUE),
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SHARE, ref = SwaggerErrorCode.NOT_FOUND_SHARE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        deleteFolderShareService.delete(folderId, targetId, details.getUserId());
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
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_SHARE_ID, ref = SwaggerErrorCode.MISMATCH_SHARE_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST, ref = SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_TARGET_USER, ref = SwaggerErrorCode.NOT_FOUND_TARGET_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SHARE, ref = SwaggerErrorCode.NOT_FOUND_SHARE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/{shareId}")
    public ResponseEntity<Void> acceptShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "shareId") Long shareId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        acceptInvitationService.accept(folderId, shareId, details);
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
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_SHARE_ID, ref = SwaggerErrorCode.MISMATCH_SHARE_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST, ref = SwaggerErrorCode.INVITE_ACCEPT_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SHARE, ref = SwaggerErrorCode.NOT_FOUND_SHARE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> refuseShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "shareId") Long shareId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        refuseInvitationService.refuse(folderId, shareId, details.getUserId());
        return ResponseEntity.noContent().build();
    }
}
