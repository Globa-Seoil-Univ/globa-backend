package org.y2k2.globa.dto.response.record;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.dto.request.record.RequestRecordDto;

import java.util.List;

@AllArgsConstructor
@Getter
public class ResponseRecordsByFolderDto {
    private List<RequestRecordDto> records;
    private boolean isOwner;
    private int total;
}
