package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class ResponseRecordsByFolderDto {
    private List<RequestRecordDto> records;
    private boolean isOwner;
    private int total;
}
