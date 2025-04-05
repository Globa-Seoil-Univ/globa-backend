package org.y2k2.globa.application.summary.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.summary.command.GetSummariesCommand;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.summary.repository.SummaryRepository;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetSummariesUseCase implements UseCase<GetSummariesCommand, List<SummaryEntity>> {
    private final SummaryRepository summaryRepository;

    @Cacheable(
            value = "summaries",
            key = "#command.sectionIds.stream().sorted().collect(" +
                        "T(java.util.stream.Collectors).joining(':')" +
                    ")"
    )
    @Override
    public List<SummaryEntity> execute(GetSummariesCommand command) {
        return summaryRepository.getSummaryInSections(
                command.sectionIds()
        );
    }
}
