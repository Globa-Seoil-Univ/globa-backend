package org.y2k2.globa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.dto.UserIntroDto;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class RecordSearchDto {
    private UserIntroDto uploader;
    private Long recordId;
    private Long folderId;
    private String title;
    private String createdTime;
}
