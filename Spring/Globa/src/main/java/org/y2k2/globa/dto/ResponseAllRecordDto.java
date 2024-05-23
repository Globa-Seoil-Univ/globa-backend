package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@Getter
public class ResponseAllRecordDto implements Serializable {
    private Long recordId;
    private Long folderId;
    private String title;
    private String path;
    private List<ResponseKeywordDto> keywords;
    private LocalDateTime createdTime;
}
