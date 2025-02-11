package org.y2k2.globa.dto.response.study;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
public class ResponseStudyTimesDto {
    private Long studyTime;
    private String createdTime;
}