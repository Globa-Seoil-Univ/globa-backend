package org.y2k2.globa.application.analysis.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.analysis.command.GetAnalysisCommand;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.analysis.repository.AnalysisRepository;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAnalysisUseCase implements UseCase<GetAnalysisCommand, List<AnalysisEntity>> {
    private final AnalysisRepository analysisRepository;

    @Override
    public List<AnalysisEntity> execute(GetAnalysisCommand command) {
        return analysisRepository.getAllSections(
                command.sectionIds()
        );
    }
}
