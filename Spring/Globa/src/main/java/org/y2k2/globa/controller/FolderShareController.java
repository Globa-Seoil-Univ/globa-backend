package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.FolderShareUserResponseDto;
import org.y2k2.globa.dto.Role;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.service.FolderShareService;
import org.y2k2.globa.util.JwtTokenProvider;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/folder/{folderId}/share")
@ResponseBody
@RequiredArgsConstructor
public class FolderShareController {
    private final FolderShareService folderShareService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/user")
    public ResponseEntity<?> getShareUsers(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable(value = "folderId") Long folderId,
            @RequestParam(required = false, defaultValue = "1", value = "page") int page,
            @RequestParam(required = false, defaultValue = "10", value = "count") int count) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        FolderShareUserResponseDto folderShareUserResponseDto = folderShareService.getShares(folderId, userId, page, count);
        return ResponseEntity.ok().body(folderShareUserResponseDto);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> inviteShare(
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestBody Map<String, String> body,
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        String role = body.get("role");
        checkRole(role);

        folderShareService.inviteShare(folderId, userId, targetId, Role.valueOf(role.toUpperCase()));
        return ResponseEntity.created(URI.create("/folder/" + folderId + "/share/user")).build();
    }

    @PatchMapping("/user/{userId}")
    public ResponseEntity<?> editShare(
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestBody Map<String, String> body,
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        String role = body.get("role");
        checkRole(role);

        folderShareService.editInviteShare(folderId, userId, targetId, Role.valueOf(role.toUpperCase()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteShare(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId) {
            if (accessToken == null) {
                throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
            folderShareService.deleteInviteShare(folderId, userId, targetId);

            return ResponseEntity.noContent().build();
    }

    @PostMapping("/{shareId}")
    public ResponseEntity<?> acceptShare(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "shareId") Long shareId) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        folderShareService.acceptShare(folderId, shareId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> refuseShare(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "shareId") Long shareId) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);
        folderShareService.refuseShare(folderId, shareId, userId);
        return ResponseEntity.noContent().build();
    }


    private void checkRole(String role) {
        if (role == null) throw new CustomException(ErrorCode.NOT_NULL_ROLE);
        if (role.isEmpty()) throw new CustomException(ErrorCode.REQUIRED_ROLE);
        if (!role.toUpperCase().equals(Role.R.toString())
                && !role.toUpperCase().equals(Role.W.toString())) {
            throw new CustomException(ErrorCode.ROLE_BAD_REQUEST);
        }
    }

}
