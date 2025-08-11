package org.y2k2.globa.application.folder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseFolderDto {
    private List<FolderDto> folders;
    private Long total;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FolderDto {
        private Long folderId;
        private String title;
        private String createdTime;
    }
}
