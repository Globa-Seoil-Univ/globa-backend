package org.y2k2.globa.infrastructure.persistence.record.projection;

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
