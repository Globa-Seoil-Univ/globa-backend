package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.y2k2.globa.dto.FolderShareUserResponseDto;
import org.y2k2.globa.dto.Role;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.service.FolderShareService;

import java.net.URI;
import java.util.Map;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class FolderShareController {
    private final String PRE_FIX = "/folder/{folderId}/share";
    private final FolderShareService folderShareService;

    @GetMapping(PRE_FIX + "/user")
    public ResponseEntity<?> getShareUsers(
            @PathVariable(value = "folderId") Long folderId,
            @RequestParam(required = false, defaultValue = "1", value = "page") int page,
            @RequestParam(required = false, defaultValue = "10", value = "count") int count) {
        // token 체크
        FolderShareUserResponseDto folderShareUserResponseDto = folderShareService.getShares(folderId, 1L, page, count);
        return ResponseEntity.ok().body(folderShareUserResponseDto);
    }

    @PostMapping(PRE_FIX + "/user/{userId}")
    public ResponseEntity<?> inviteShare(
            @RequestBody Map<String, String> body,
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId) {
        // token 체크
        String role = body.get("role");
        checkRole(role);

        folderShareService.inviteShare(folderId, 1L, targetId, Role.valueOf(role.toUpperCase()));
        return ResponseEntity.created(URI.create("/folder/" + folderId + "/share/user")).build();
    }

    @PatchMapping(PRE_FIX + "/user/{userId}")
    public ResponseEntity<?> editShare(
            @RequestBody Map<String, String> body,
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId) {
        // token 체크
        String role = body.get("role");
        checkRole(role);

        folderShareService.editInviteShare(folderId, 1L, targetId, Role.valueOf(role.toUpperCase()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(PRE_FIX + "/user/{userId}")
    public ResponseEntity<?> deleteShare(
            @PathVariable(value = "folderId") Long folderId,
            @PathVariable(value = "userId") Long targetId) {
            // token 체크
            folderShareService.deleteInviteShare(folderId, 1L, targetId);

            return ResponseEntity.noContent().build();
    }

    private void checkRole(String role) {
        if (role.isEmpty()) throw new BadRequestException("You must be request role field");
        if (!role.toUpperCase().equals(Role.R.toString())
                && !role.toUpperCase().equals(Role.W.toString())) {
            throw new BadRequestException("Role field must be only 'r' or 'w'");
        }
    }

}
