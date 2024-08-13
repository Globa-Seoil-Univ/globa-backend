package org.y2k2.globa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResponseRecordDetailDto {
    private Long recordId;
    private String title;
    private String path;
    private String size;
    private ResponseDetailFolderDto folder;
    private List<ResponseSectionDto> section;
    private LocalDateTime createdTime;
}
