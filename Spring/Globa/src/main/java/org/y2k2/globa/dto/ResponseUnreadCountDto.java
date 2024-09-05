package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ResponseUnreadCountDto {
    Long all;
    Long notice;
    Long share;
    Long document;
    Long inquiry;
}