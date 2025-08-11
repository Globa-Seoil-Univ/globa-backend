package org.y2k2.globa.infrastructure.persistence.keyword.projection;

public class KeywordProjectionImpl implements KeywordProjection {
    private final Long recordId;
    private final String word;
    private final Double importance;

    public KeywordProjectionImpl(Long recordId, String word, Double importance) {
        this.recordId = recordId;
        this.word = word;
        this.importance = importance;
    }

    @Override
    public Long getRecordId() {
        return recordId;
    }

    @Override
    public String getWord() {
        return word;
    }

    @Override
    public Double getImportance() {
        return importance;
    }
}
