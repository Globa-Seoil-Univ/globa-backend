package org.y2k2.globa.dto.request.folder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RequestFolderPostDto {
    @Getter
    @AllArgsConstructor
    public static class ShareTarget {
        private String code;
        private String role;  // "r" for read, "w" for write
    }

    private String title;
    private List<ShareTarget> shareTarget;
}
