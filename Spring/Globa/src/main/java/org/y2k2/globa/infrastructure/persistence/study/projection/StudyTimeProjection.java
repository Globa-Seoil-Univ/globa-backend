package org.y2k2.globa.infrastructure.persistence.study.projection;

import java.time.LocalDateTime;

public interface StudyTimeProjection {
    Long getTotalStudyTime();
    String getCreatedTime();
}