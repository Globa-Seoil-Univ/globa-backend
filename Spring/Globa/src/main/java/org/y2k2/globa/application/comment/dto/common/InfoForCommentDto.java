package org.y2k2.globa.application.comment.dto.common;

import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

public record InfoForCommentDto(
        SectionEntity section,
        HighlightEntity highlight,
        FolderShareEntity folderShare
) {
    public static InfoForCommentDto of(
            SectionEntity section,
            HighlightEntity highlight,
            FolderShareEntity folderShare
    ) {
        return new InfoForCommentDto(section, highlight, folderShare);
    }
}
