package org.y2k2.globa.fixture.highlight;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.highlight.HighlightFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

@Component
public class HighlightFixture extends AbstractFixture<HighlightEntity> {
    @Autowired
    private HighlightFactory highlightFactory;

    @Override
    protected HighlightEntity build() {
        return highlightFactory.createAndSave();
    }

    public HighlightFixture withStartIndex(Long startIndex) {
        highlightFactory.setStartIndex(startIndex);
        return this;
    }

    public HighlightFixture withEndIndex(Long endIndex) {
        highlightFactory.setEndIndex(endIndex);
        return this;
    }

    public HighlightFixture withSection(SectionEntity section) {
        highlightFactory.setSection(section);
        return this;
    }
}
