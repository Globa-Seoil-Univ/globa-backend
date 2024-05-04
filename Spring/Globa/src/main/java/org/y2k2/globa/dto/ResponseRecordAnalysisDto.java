package org.y2k2.globa.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.entity.HighlightEntity;
import org.y2k2.globa.entity.SectionEntity;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ResponseRecordAnalysisDto {
    private Long analysisId;
    private String content;
    private List<HighlightEntity> highlights;
}
