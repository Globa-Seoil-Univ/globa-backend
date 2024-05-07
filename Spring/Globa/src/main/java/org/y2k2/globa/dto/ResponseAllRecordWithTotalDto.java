package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ResponseAllRecordWithTotalDto {
    private List<ResponseAllRecordDto> records;
    private int total;
}
