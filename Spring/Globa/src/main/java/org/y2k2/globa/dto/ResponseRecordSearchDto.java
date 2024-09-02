package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.y2k2.globa.entity.RecordSearchDto;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResponseRecordSearchDto {
    List<RecordSearchDto> records;
    Long total;
}
