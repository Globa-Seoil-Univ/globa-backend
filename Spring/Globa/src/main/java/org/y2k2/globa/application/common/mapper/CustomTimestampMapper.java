package org.y2k2.globa.application.common.mapper;

import org.y2k2.globa.common.util.CustomTimestamp;

import java.time.LocalDateTime;

@CustomTimestampTranslator
public class CustomTimestampMapper {
    @MapCreatedTime
    public String mapCreatedTime(LocalDateTime createdTime) {
        return new CustomTimestamp(createdTime).toString();
    }
}
