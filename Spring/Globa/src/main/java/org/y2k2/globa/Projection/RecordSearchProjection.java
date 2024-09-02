package org.y2k2.globa.Projection;

import org.y2k2.globa.dto.UserIntroDto;

import java.time.LocalDateTime;

public interface RecordSearchProjection {
    Long getUserId();
    String getName();
    String getProfilePath();
    Long getRecordId();
    Long getFolderId();
    String getTitle();
    LocalDateTime getCreatedTime();
}
