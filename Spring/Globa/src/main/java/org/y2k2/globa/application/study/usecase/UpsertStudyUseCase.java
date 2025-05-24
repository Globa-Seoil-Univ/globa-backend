package org.y2k2.globa.application.study.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.study.command.UpsertStudyCommand;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.study.repository.StudyRepository;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;

@Component
@RequiredArgsConstructor
public class UpsertStudyUseCase implements VoidUseCase<UpsertStudyCommand> {
    private final StudyRepository studyRepository;

    @Override
    public void execute(UpsertStudyCommand command) {
        StudyEntity study = studyRepository.getStudy(command.user(), command.record())
                .orElse(
                        StudyEntity.builder()
                                .user(command.user())
                                .record(command.record())
                                .build()
                );

        if (study.getStudyTime() != null) {
            study.setStudyTime(study.getStudyTime() + command.studyTime());
        } else {
            study.setStudyTime(command.studyTime());
        }

        studyRepository.save(study);
    }
}
