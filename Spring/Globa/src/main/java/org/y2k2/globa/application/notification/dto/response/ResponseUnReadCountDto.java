package org.y2k2.globa.application.notification.dto.response;

public record ResponseUnReadCountDto(
    Long all,
    Long notice,
    Long share,
    Long document,
    Long inquiry
) {
}