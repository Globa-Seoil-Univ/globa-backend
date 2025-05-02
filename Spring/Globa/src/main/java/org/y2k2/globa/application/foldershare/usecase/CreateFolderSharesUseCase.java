package org.y2k2.globa.application.foldershare.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.foldershare.command.CreateFolderSharesCommand;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateFolderSharesUseCase implements UseCase<CreateFolderSharesCommand, List<FolderShareEntity>> {
    private final FolderShareRepository folderShareRepository;

    @Override
    public List<FolderShareEntity> execute(CreateFolderSharesCommand command) {
        List<FolderShareEntity> folderShares = command.targets().stream().map(
                target -> FolderShareMapper.INSTANCE.toEntity(
                        command.folder(),
                        command.status(),
                        target.role(),
                        command.ownerUser(),
                        target.user()
                )
        ).toList();

        return folderShareRepository.saveAll(folderShares);
    }
}
