package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.request.folder.RequestFolderPostDto;
import org.y2k2.globa.dto.response.folder.ResponseFolderDto;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.mapper.FolderShareMapper;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.FolderRole;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.mapper.FolderMapper;
import org.y2k2.globa.util.file.FileStore;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {
    private final FolderShareService folderShareService;
    private final FileStore fileStore;

    private final UserRepository userRepository;;
    private final RecordRepository recordRepository;
    private final FolderRepository folderRepository;
    private final FolderShareRepository folderShareRepository;
    private final FolderRoleRepository folderRoleRepository;

    public ResponseFolderDto getFolders(int page, int count, UserEntity user){
        Pageable pageable;
        List<FolderEntity> folders = new ArrayList<>();

        if (page == 1) {
            pageable = PageRequest.of(0, count - 1);

            FolderEntity defaultFolder = folderRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(user.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DEFAULT_FOLDER));

            folders.add(defaultFolder);
        } else {
            pageable = PageRequest.of(page - 1, count);
        }

        Page<FolderShareEntity> folderShareEntities = folderShareRepository.findAllByOwnerUserOrTargetUserAndInvitationStatusOrderByShareIdDesc(
                user,
                user,
                InvitationStatus.ACCEPT,
                pageable
        );

        folders.addAll(folderShareEntities.stream()
                .map(FolderShareEntity::getFolder)
                .toList()
        );

        List<ResponseFolderDto.FolderDto> dtos = folders.stream()
                .map(FolderMapper.INSTANCE::toResponseInFolderDto)
                .toList();

        return new ResponseFolderDto(dtos, folderShareEntities.getTotalElements());
    }

    public void createDefaultFolder(UserEntity user) {
        createFolder(user.getName() + "님의 기본 폴더", user);
    }

    @Transactional
    public FolderShareEntity createFolder(String title, UserEntity user) {
        FolderRoleEntity role = folderRoleRepository.findByRoleName(FolderRole.OWNER.getRoleName());
        FolderEntity folder = FolderMapper.INSTANCE.toEntity(user, title);
        FolderEntity createdFolder = folderRepository.save(folder);

        FolderShareEntity folderShare = FolderShareMapper.INSTANCE.toEntity(
                createdFolder,
                InvitationStatus.ACCEPT,
                role,
                user,
                user
        );
        return folderShareRepository.save(folderShare);
    }

    @Transactional
    public void createFolder(String title, List<RequestFolderPostDto.ShareTarget> shareTargets, UserEntity user){
        FolderShareEntity share = createFolder(title, user);

        List<UserEntity> targets = userRepository.findAllByCodeIn(
                shareTargets.stream().map(RequestFolderPostDto.ShareTarget::code).toList()
        );

        if (targets.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);
        }

        folderShareService.inviteShares(
                share.getFolder(),
                user,
                shareTargets
        );
    }

    @Transactional
    public void modifyFolderName(Long folderId, String title, UserEntity user){
        FolderEntity folder = folderRepository.findFirstByFolderId(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        if (!folder.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }

        folder.setTitle(title);
        folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(Long folderId, UserEntity user){
        FolderEntity folder = folderRepository.findFirstByFolderIdWithoutDefault(folderId, user)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        folderRepository.delete(folder);
        deleteFiles(folder);
    }

    @Async
    public void deleteFiles(FolderEntity folder) {
        List<RecordEntity> records = recordRepository.findAllByFolder(folder);
        fileStore.deleteFiles(records.stream()
                .map(RecordEntity::getPath)
                .toList()
        );
    }
}