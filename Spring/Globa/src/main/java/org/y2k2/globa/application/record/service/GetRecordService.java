package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.analysis.command.GetAnalysisCommand;
import org.y2k2.globa.application.analysis.usecase.GetAnalysisUseCase;
import org.y2k2.globa.application.folder.dto.response.ResponseDetailFolderDto;
import org.y2k2.globa.application.folder.mapper.FolderMapper;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.record.command.AggregateRecordCommand;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDetailDto;
import org.y2k2.globa.application.record.mapper.RecordMapper;
import org.y2k2.globa.application.record.usecase.AggregateRecordUseCase;
import org.y2k2.globa.application.section.command.GetSectionsCommand;
import org.y2k2.globa.application.section.dto.response.ResponseSectionDto;
import org.y2k2.globa.application.section.usecase.GetSectionsUseCase;
import org.y2k2.globa.application.summary.command.GetSummariesCommand;
import org.y2k2.globa.application.summary.usecase.GetSummariesUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.highlight.repository.HighlightRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetRecordService {
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    private final GetSectionsUseCase getSectionsUseCase;
    private final GetAnalysisUseCase getAnalysisUseCase;
    private final GetSummariesUseCase getSummariesUseCase;
    private final AggregateRecordUseCase aggregateRecordUseCase;

    private final RecordRepository recordRepository;
    private final HighlightRepository highlightRepository;

    @Transactional(readOnly = true)
    public ResponseRecordDetailDto get(Long folderId, Long recordId, Long userId) {
        RecordEntity record = recordRepository.getRecord(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        if (!record.getIsShare()) {
            verifyFolderAccessibleUseCase.execute(
                    VerifyFolderCommand.of(
                            userId,
                            folderId
                    )
            );
        }

        List<SectionEntity> sections = getSectionsUseCase.execute(
                GetSectionsCommand.of(
                        record.getRecordId()
                )
        );
        List<AnalysisEntity> analysis = getAnalysisUseCase.execute(
                GetAnalysisCommand.of(
                        sections.stream().map(SectionEntity::getSectionId).toList()
                )
        );
        List<SummaryEntity> summaries = getSummariesUseCase.execute(
                GetSummariesCommand.of(
                        sections.stream().map(SectionEntity::getSectionId).toList()
                )
        );
        List<HighlightEntity> highlights = highlightRepository.getAllHighlights(
                sections.stream().map(SectionEntity::getSectionId).toList()
        );

        List<ResponseSectionDto> combinedSections = aggregateRecordUseCase.execute(
                AggregateRecordCommand.of(
                        sections,
                        analysis,
                        highlights,
                        summaries
                )
        );

        ResponseDetailFolderDto folder = FolderMapper.INSTANCE.toResponseDetailFolderDto(record.getFolder());
        return RecordMapper.INSTANCE.toResponseRecordDetailDto(record, folder, combinedSections);
    }
}
