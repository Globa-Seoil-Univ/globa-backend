package org.y2k2.globa.application.section.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.section.command.GetSectionsCommand;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.section.repository.SectionRepository;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetSectionsUseCase implements UseCase<GetSectionsCommand, List<SectionEntity>> {
    private final SectionRepository sectionRepository;

    @Override
    public List<SectionEntity> execute(GetSectionsCommand command) {
        return sectionRepository.getAllSortedSections(command.recordId());
    }
}
