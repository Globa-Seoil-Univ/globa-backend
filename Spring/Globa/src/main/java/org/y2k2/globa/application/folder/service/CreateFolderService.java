package org.y2k2.globa.application.folder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.folder.dto.request.RequestFolderPostDto;
import org.y2k2.globa.application.folder.mapper.FolderMapper;
import org.y2k2.globa.application.folderrole.command.GetFolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.GetFolderRoleUseCase;
import org.y2k2.globa.application.foldershare.command.CreateFolderSharesCommand;
import org.y2k2.globa.application.foldershare.usecase.CreateFolderSharesUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateFolderService {
    private final FindUserUseCase findUserUseCase;
    private final GetFolderRoleUseCase getFolderRoleUseCase;
    private final CreateFolderSharesUseCase createFolderSharesUsecase;

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Transactional
    public FolderShareEntity create(String title, Long userId) {
        UserEntity ownerUser = findUserUseCase.execute(userId);
        FolderRoleEntity role = getFolderRoleUseCase.execute(
                GetFolderRoleCommand.of(FolderRole.OWNER)
        );

        FolderEntity folder = FolderMapper.INSTANCE.toEntity(ownerUser, title);
        FolderEntity createdFolder = folderRepository.save(folder);

        List<FolderShareEntity> folderShares = createFolderSharesUsecase.execute(
                CreateFolderSharesCommand.of(
                        createdFolder,
                        role,
                        ownerUser,
                        List.of(ownerUser),
                        InvitationStatus.ACCEPT
                )
        );

        return folderShares.get(0);
    }

    @Transactional
    public void create(String title, List<RequestFolderPostDto.ShareTarget> shareTargets, Long userId){
        FolderShareEntity share = create(title, userId);

        List<UserEntity> targets = userRepository.getAllUsersByCodes(
                shareTargets.stream().map(RequestFolderPostDto.ShareTarget::code).toList()
        );

        if (targets.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);
        }

        createFolderSharesUsecase.execute(
                CreateFolderSharesCommand.of(
                        share.getFolder(),
                        share.getRole(),
                        share.getOwnerUser(),
                        targets,
                        InvitationStatus.PENDING
                )
        );
    }
}
