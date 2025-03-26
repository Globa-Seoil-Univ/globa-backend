package org.y2k2.globa.application.comment.mapper;

import org.mapstruct.Qualifier;
import org.y2k2.globa.common.util.CustomTimestamp;

import java.time.LocalDateTime;

@Qualifier @interface CustomTimestampTranslator { }

@Qualifier @interface MapCreatedTime { }

@CustomTimestampTranslator
public class CustomTimestampMapper {
    @MapCreatedTime
    public String mapCreatedTime(LocalDateTime createdTime) {
        return new CustomTimestamp(createdTime).toString();
    }
}
