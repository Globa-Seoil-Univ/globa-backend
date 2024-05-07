package org.y2k2.globa.util;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
public class CustomTimestamp {
    private LocalDateTime timestamp;

    public CustomTimestamp() {
        this.timestamp = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    @Override
    public String toString() {
        return this.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
