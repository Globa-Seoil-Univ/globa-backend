package org.y2k2.globa.application.folderrole.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.mapper.FolderRoleMapper;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.folderrole.repository.FolderRoleRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateFolderRoleUseCase implements UseCase<FolderRoleCommand, FolderRoleEntity> {
    private final FolderRoleRepository folderRoleRepository;

    @Override
    @CachePut(
            value = "folderRole",
            key = "#command.folderRole.name()",
            unless = "#result.getRoleId() == null"
    )
    @Transactional
    public FolderRoleEntity execute(FolderRoleCommand command) {
        log.warn("FolderRole을 찾을 수 없어, 새로 생성합니다. {}", command.folderRole().name());

        FolderRoleEntity folderRole = FolderRoleMapper.INSTANCE.toEntity(command);
        return folderRoleRepository.save(folderRole);
    }
}
