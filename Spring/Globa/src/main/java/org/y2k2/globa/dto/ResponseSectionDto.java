package org.y2k2.globa.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.entity.AnalysisEntity;
import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.SummaryEntity;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ResponseSectionDto {
    private Long sectionId;
    private String title;
    private int startTime;
    private int entTime;
    private ResponseRecordAnalysisDto analysis;
    private List<SummaryEntity> summary;
    private LocalDateTime createdTime;
}
