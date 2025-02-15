package org.y2k2.globa.service;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.common.file.FileDto;
import org.y2k2.globa.dto.common.folder.FolderDto;
import org.y2k2.globa.dto.request.folder.RequestFolderPostDto;
import org.y2k2.globa.dto.response.folder.ResponseFolderDto;
import org.y2k2.globa.entity.FolderRoleEntity;
import org.y2k2.globa.mapper.FolderShareMapper;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.Role;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.mapper.FolderMapper;
import org.y2k2.globa.util.file.FileStore;
import org.y2k2.globa.util.jwt.JWTProvider;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {
    private final JWTProvider jwtTokenProvider;
    private final FolderShareService folderShareService;
    private final FileStore fileStore;

    public final UserRepository userRepository;;
    public final StudyRepository studyRepository;
    public final SurveyRepository surveyRepository;
    public final FolderRepository folderRepository;
    public final FolderShareRepository folderShareRepository;
    public final FolderRoleRepository folderRoleRepository;

    public ResponseFolderDto getFolders(int page, int count, UserEntity user){
        Pageable pageable = PageRequest.of(page-1, count);
        Page<FolderShareEntity> folderShareEntities = folderShareRepository.findAllByOwnerUserOrTargetUserAndInvitationStatus(
                user,
                user,
                InvitationStatus.ACCEPT,
                pageable
        );

        List<FolderEntity> folders = folderShareEntities.getContent().stream().map(FolderShareEntity::getFolder).toList();
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
        FolderRoleEntity role = folderRoleRepository.findByRoleName("소유자");
        FolderEntity folder = FolderMapper.INSTANCE.toEntity(user, title);
        FolderEntity createdFolder = folderRepository.save(folder);

        FolderShareEntity folderShare = FolderShareMapper.INSTANCE.toEntity(
                createdFolder,
                InvitationStatus.ACCEPT,
                role,
                user,
                user
        );
        FolderShareEntity createdFolderShare = folderShareRepository.save(folderShare);

        String filename = "placeholder.txt";
        String folderPath = "folders/" + folder.getFolderId() + "/" + filename;

        fileStore.storeEmptyFile(folderPath);
        return createdFolderShare;
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

        for (RequestFolderPostDto.ShareTarget target : shareTargets) {
            UserEntity targetEntity = targets.stream()
                    .filter(t -> t.getCode().equals(target.code()))
                    .findFirst()
                    .orElse(null);

            if (targetEntity == null) {
                continue;
            }
        }

        folderShareService.inviteShares(
                share.getFolder(),
                user,
                shareTargets
        );
    }

    public HttpStatus patchFolderName(String accessToken, Long folderId, String title){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity folderEntity = folderRepository.findFolderEntityByFolderId(folderId);

        if(folderEntity == null)
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);

        if (!Objects.equals(userId, folderEntity.getUser().getUserId())){
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }
        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUserAndFolderFolderIdAndInvitationStatus(userEntity,folderId,"ACCEPT");

        if(folderShareEntity == null)
            throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        folderEntity.setTitle(title);

        folderRepository.save(folderEntity);


        return HttpStatus.OK;
    }

    @Transactional
    public HttpStatus deleteFolderName(String accessToken, Long folderId){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity folderEntity = folderRepository.findFolderEntityByFolderId(folderId);
        FolderEntity defaultFolderEntity = folderRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(userEntity.getUserId());

        if(folderEntity == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);
        }

        if (!Objects.equals(userId, folderEntity.getUser().getUserId())){
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }

        if(folderEntity == defaultFolderEntity) {
            throw new CustomException(ErrorCode.FOLDER_DELETE_BAD_REQUEST);
        }

//        Iterable<Blob> blobs = bucket.list(Storage.BlobListOption.prefix("folders/" + folderId)).iterateAll();
//        if(blobs == null) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER_FIREBASE);
//
//        try {
//            for (Blob blob : blobs) {
//                blob.delete();
//            }
//            folderRepository.delete(folderEntity);
//        } catch (Exception e) {
//            throw new CustomException(ErrorCode.FAILED_FOLDER_DELETE);
//        }

        return HttpStatus.OK;
    }
}