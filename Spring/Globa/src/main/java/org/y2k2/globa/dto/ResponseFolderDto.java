package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResponseFolderDto {
    private List<FolderDto> folders;
    private Long total;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FolderDto {
        private Long folderId;
        private String title;
        private String createdTime;
    }
}
