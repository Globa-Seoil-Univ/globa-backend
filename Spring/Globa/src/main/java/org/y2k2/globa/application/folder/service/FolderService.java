//package org.y2k2.globa.application.folder.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.y2k2.globa.common.exception.CustomException;
//import org.y2k2.globa.common.exception.ErrorCode;
//import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
//import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
//import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
//import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
//import org.y2k2.globa.infrastructure.persistence.record.repository.RecordJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
//import org.y2k2.globa.application.folder.dto.request.RequestFolderPostDto;
//import org.y2k2.globa.application.folder.dto.response.ResponseFolderDto;
//import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareJpaRepository;
//import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
//import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
//import org.y2k2.globa.common.type.FolderRole;
//import org.y2k2.globa.application.folder.mapper.FolderMapper;
//import org.y2k2.globa.common.util.file.FileStore;
//import org.y2k2.globa.application.foldershare.service.FolderShareService;
//import org.y2k2.globa.infrastructure.persistence.user.repository.UserJpaRepository;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class FolderService {
//    private final FolderShareService folderShareService;
//    private final FileStore fileStore;
//
//    private final UserJpaRepository userJpaRepository;;
//    private final RecordJpaRepository recordJpaRepository;
//    private final FolderJpaRepository folderJpaRepository;
//    private final FolderShareJpaRepository folderShareJpaRepository;
//    private final FolderRoleJpaRepository folderRoleJpaRepository;
//
//    public ResponseFolderDto getFolders(int page, int count, UserEntity user){
//        Pageable pageable;
//        List<FolderEntity> folders = new ArrayList<>();
//
//        if (page == 1) {
//            pageable = PageRequest.of(0, count - 1);
//
//            FolderEntity defaultFolder = folderJpaRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(user.getUserId())
//                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DEFAULT_FOLDER));
//
//            folders.add(defaultFolder);
//        } else {
//            pageable = PageRequest.of(page - 1, count);
//        }
//
//        Page<FolderShareEntity> folderShareEntities = folderShareJpaRepository.findAllByOwnerUserOrTargetUserAndInvitationStatusOrderByShareIdDesc(
//                user,
//                user,
//                InvitationStatus.ACCEPT,
//                pageable
//        );
//
//        folders.addAll(folderShareEntities.stream()
//                .map(FolderShareEntity::getFolder)
//                .toList()
//        );
//
//        List<ResponseFolderDto.FolderDto> dtos = folders.stream()
//                .map(FolderMapper.INSTANCE::toResponseInFolderDto)
//                .toList();
//
//        return new ResponseFolderDto(dtos, folderShareEntities.getTotalElements());
//    }
//
//    public void createDefaultFolder(UserEntity user) {
//        createFolder(user.getName(), user);
//    }
//
//    @Transactional
//    public FolderShareEntity createFolder(String title, UserEntity user) {
//        FolderRoleEntity role = folderRoleJpaRepository.findByRoleName(FolderRole.OWNER.getRoleName());
//        FolderEntity folder = FolderMapper.INSTANCE.toEntity(user, title);
//        FolderEntity createdFolder = folderJpaRepository.save(folder);
//
//        FolderShareEntity folderShare = FolderShareMapper.INSTANCE.toEntity(
//                createdFolder,
//                InvitationStatus.ACCEPT,
//                role,
//                user,
//                user
//        );
//        return folderShareJpaRepository.save(folderShare);
//    }
//
//    @Transactional
//    public void createFolder(String title, List<RequestFolderPostDto.ShareTarget> shareTargets, UserEntity user){
//        FolderShareEntity share = createFolder(title, user);
//
//        List<UserEntity> targets = userJpaRepository.findAllByCodeIn(
//                shareTargets.stream().map(RequestFolderPostDto.ShareTarget::code).toList()
//        );
//
//        if (targets.isEmpty()) {
//            throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);
//        }
//
//        folderShareService.inviteShares(
//                share.getFolder(),
//                user,
//                shareTargets
//        );
//    }
//
//    @Transactional
//    public void modifyFolderName(Long folderId, String title, UserEntity user){
//        FolderEntity folder = folderJpaRepository.findFirstByFolderId(folderId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));
//
//        if (!folder.getUser().getUserId().equals(user.getUserId())) {
//            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
//        }
//
//        folder.setTitle(title);
//        folderJpaRepository.save(folder);
//    }
//
//    @Transactional
//    public void deleteFolder(Long folderId, UserEntity user){
//        FolderEntity folder = folderJpaRepository.findFirstByFolderIdWithoutDefault(folderId, user)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));
//
//        folderJpaRepository.delete(folder);
//        deleteFiles(folder);
//    }
//
//    @Async
//    public void deleteFiles(FolderEntity folder) {
//        List<RecordEntity> records = recordJpaRepository.findAllByFolder(folder);
//        fileStore.deleteFiles(records.stream()
//                .map(RecordEntity::getPath)
//                .toList()
//        );
//    }
//}