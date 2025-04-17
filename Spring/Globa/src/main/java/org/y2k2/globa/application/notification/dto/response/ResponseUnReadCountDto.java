package org.y2k2.globa.application.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ResponseUnReadCountDto {
    Long all;
    Long notice;
    Long share;
    Long document;
    Long inquiry;
}