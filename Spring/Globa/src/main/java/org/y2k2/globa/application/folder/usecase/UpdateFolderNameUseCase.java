package org.y2k2.globa.application.folder.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.folder.command.UpdateFolderNameCommand;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

@RequiredArgsConstructor
@Component
public class UpdateFolderNameUseCase implements VoidUseCase<UpdateFolderNameCommand> {
    private final FolderRepository folderRepository;

    @Override
    public void execute(UpdateFolderNameCommand command) {
        FolderEntity folder = command.folder();
        folder.setTitle(command.title());
        folderRepository.save(folder);
    }
}
