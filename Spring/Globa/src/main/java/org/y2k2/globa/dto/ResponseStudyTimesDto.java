package org.y2k2.globa.dto;

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
@ToString
public class ResponseStudyTimesDto implements Serializable {
    private Long studyTime;
    private String createdTime;
}