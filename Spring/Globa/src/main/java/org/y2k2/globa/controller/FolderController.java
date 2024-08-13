package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.y2k2.globa.dto.FolderPostRequestDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.service.FolderService;

import java.util.Map;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class FolderController {
    private final String PRE_FIX = "/folder";

    public final FolderService folderService;

    @GetMapping(PRE_FIX)
    public ResponseEntity<?> getFolders(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                        @RequestParam(required = false, defaultValue = "1", value = "page") int page,
                                        @RequestParam(required = false, defaultValue = "10", value = "count") int count) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        return ResponseEntity.status(HttpStatus.OK).body(folderService.getFolders(accessToken,page,count));
    }

    @PostMapping(PRE_FIX)
    public ResponseEntity<?> postFolder(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                        @RequestBody FolderPostRequestDto request){


        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( request.getTitle() == null  )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_TITLE);
        if ( request.getShareTarget() == null)
            return ResponseEntity.status(HttpStatus.CREATED).body(folderService.postFolder(accessToken, request.getTitle()));
        else
            return ResponseEntity.status(HttpStatus.CREATED).body(folderService.postFolder(accessToken, request.getTitle(), request.getShareTarget()));
    }

    @PatchMapping(PRE_FIX + "/{folder_id}/name")
    public ResponseEntity<?> patchFolder(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                        @PathVariable(value = "folder_id", required = false) Long folderId,
                                        @RequestBody Map<String, String> titleMap){


        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( titleMap.get("title") == null  )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_TITLE);
        if ( folderId == null)
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);

        return ResponseEntity.status(folderService.patchFolderName(accessToken, folderId, titleMap.get("title"))).body("");
    }

    @DeleteMapping(PRE_FIX + "/{folder_id}")
    public ResponseEntity<?> deleteFolder(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                        @PathVariable(value = "folder_id", required = false) Long folderId){


        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null)
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);

        return ResponseEntity.status(folderService.deleteFolderName(accessToken, folderId)).body("");
    }


}
