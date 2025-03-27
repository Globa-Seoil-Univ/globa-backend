package org.y2k2.globa.infrastructure.persistence.keyword.projection;

public interface KeywordProjection {
    Long getRecordId();
    String getWord();
    Double getImportance();
}