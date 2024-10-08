package org.y2k2.globa.service;

import com.google.cloud.storage.*;
import com.google.cloud.storage.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.FolderDto;
import org.y2k2.globa.dto.ResponseFolderDto;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.Role;
import org.y2k2.globa.dto.ShareTarget;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.mapper.FolderMapper;
import org.y2k2.globa.util.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final JwtTokenProvider jwtTokenProvider;
    private final FolderShareService folderShareService;

    public final UserRepository userRepository;;
    public final StudyRepository studyRepository;
    public final SurveyRepository surveyRepository;
    public final FolderRepository folderRepository;
    public final FolderShareRepository folderShareRepository;
    public final FolderRoleRepository folderRoleRepository;

    @Autowired
    private Bucket bucket;

    @Value("${firebase.bucket-path}")
    private String firebaseBucketPath;


    public ResponseFolderDto getFolders(String accessToken, int page, int count){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);


        Pageable pageable = PageRequest.of(page-1, count);

        Page<FolderShareEntity> folderShareEntities = folderShareRepository.findFolderShareEntitiesAndShareUserByTargetUserAndInvitationStatus(userEntity, "ACCEPT", pageable);

        if(folderShareEntities == null)
            throw new CustomException(ErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER);

        List<FolderEntity> folders = folderShareEntities.getContent().stream().map(FolderShareEntity::getFolder).toList();
        List<ResponseFolderDto.FolderDto> dtos = folders.stream()
                .map(FolderMapper.INSTANCE::toResponseInFolderDto)
                .toList();

        return new ResponseFolderDto(dtos, folderShareEntities.getTotalElements());
    }

    public FolderDto postDefaultFolder(UserEntity userEntity){
        FolderEntity saveFolderEntity = new FolderEntity();
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);
        saveFolderEntity.setUser(userEntity);
        saveFolderEntity.setTitle(userEntity.getName() + "의 기본 폴더");
        saveFolderEntity.setCreatedTime(LocalDateTime.now());

        FolderEntity savedEntity = folderRepository.save(saveFolderEntity);


        FolderShareEntity saveShareEntity = new FolderShareEntity();
        saveShareEntity.setFolder(savedEntity);
        saveShareEntity.setRoleId(folderRoleRepository.findByRoleName("소유자"));
        saveShareEntity.setOwnerUser(userEntity);
        saveShareEntity.setTargetUser(userEntity);
        saveShareEntity.setInvitationStatus("ACCEPT");

        folderShareRepository.save(saveShareEntity);

        try {
            String folderPath = "folders/" + savedEntity.getFolderId() + "/";
            String placeholderPath = folderPath + "placeholder.txt";

            BlobInfo blobInfo = BlobInfo.newBuilder(firebaseBucketPath, placeholderPath).build();
            byte[] content = new byte[0];
            bucket.create(blobInfo.getName(), content);
        } catch (StorageException e) {
            folderRepository.delete(savedEntity);
            folderShareRepository.delete(saveShareEntity);
            throw new CustomException(ErrorCode.FAILED_FOLDER_CREATE);
        }

        return FolderMapper.INSTANCE.toFolderDto(savedEntity);

    }
    @Transactional
    public FolderDto postFolder(String accessToken, String title){

        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity saveFolderEntity = new FolderEntity();
        saveFolderEntity.setUser(userEntity);
        saveFolderEntity.setTitle(title);
        saveFolderEntity.setCreatedTime(LocalDateTime.now());


        FolderEntity savedEntity = folderRepository.save(saveFolderEntity);


        FolderShareEntity saveShareEntity = new FolderShareEntity();
        saveShareEntity.setFolder(savedEntity);
        saveShareEntity.setInvitationStatus(String.valueOf(InvitationStatus.ACCEPT));
        saveShareEntity.setRoleId(folderRoleRepository.findByRoleName("소유자"));
        saveShareEntity.setOwnerUser(userEntity);
        saveShareEntity.setTargetUser(userEntity);

        FolderShareEntity savedShareEntity = folderShareRepository.save(saveShareEntity);

        try {
            String folderPath = "folders/" + savedEntity.getFolderId() + "/";
            String placeholderPath = folderPath + "placeholder.txt";

            BlobInfo blobInfo = BlobInfo.newBuilder(firebaseBucketPath, placeholderPath).build();
            byte[] content = new byte[0];
            bucket.create(blobInfo.getName(), content);
        } catch (StorageException e) {
            folderRepository.delete(savedEntity);
            folderShareRepository.delete(savedShareEntity);
            throw new CustomException(ErrorCode.FAILED_FOLDER_CREATE);
        }

        return FolderMapper.INSTANCE.toFolderDto(savedEntity);

    }

    @Transactional
    public FolderDto postFolder(String accessToken, String title, List<ShareTarget> shareTargets){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity saveFolderEntity = new FolderEntity();
        saveFolderEntity.setUser(userEntity);
        saveFolderEntity.setTitle(title);
        saveFolderEntity.setCreatedTime(LocalDateTime.now());


        FolderEntity savedEntity = folderRepository.save(saveFolderEntity);

        FolderShareEntity saveOwnerShareEntity = new FolderShareEntity();
        saveOwnerShareEntity.setFolder(savedEntity);
        saveOwnerShareEntity.setInvitationStatus(String.valueOf(InvitationStatus.ACCEPT));
        saveOwnerShareEntity.setRoleId(folderRoleRepository.findByRoleName("소유자"));
        saveOwnerShareEntity.setOwnerUser(userEntity);
        saveOwnerShareEntity.setTargetUser(userEntity);

        FolderShareEntity savedOwnerShareEntity = folderShareRepository.save(saveOwnerShareEntity);

        // 리스트를 순회하며 각 공유 타겟 수행
        for (ShareTarget target : shareTargets) {
            UserEntity targetEntity = userRepository.findOneByCode(target.getCode());
            if(targetEntity == null)
                throw new CustomException(ErrorCode.NOT_FOUND_TARGET_USER);

            if (target.getRole().isEmpty())
                throw new CustomException(ErrorCode.NOT_NULL_ROLE);
            if (!target.getRole().toUpperCase().equals(Role.R.toString())
                    && !target.getRole().toUpperCase().equals(Role.W.toString())) {
                throw new CustomException(ErrorCode.ROLE_BAD_REQUEST);
            }

            folderShareService.inviteShare(savedEntity.getFolderId(), userEntity.getUserId(), targetEntity.getUserId(), Role.valueOf(target.getRole().toUpperCase()));
        }

        try {
            String placeholderPath = "folders/" + savedEntity.getFolderId() + "/placeholder.txt";
            BlobInfo blobInfo = BlobInfo.newBuilder(firebaseBucketPath, placeholderPath).build();
            byte[] content = new byte[0]; // 빈 콘텐츠라도 넣어줘야 폴더가 생김
            bucket.create(blobInfo.getName(), content);
        } catch (Exception e) {
            folderRepository.delete(savedEntity);
            folderShareRepository.delete(savedOwnerShareEntity);
            throw new CustomException(ErrorCode.FAILED_FOLDER_CREATE);
        }

        return FolderMapper.INSTANCE.toFolderDto(savedEntity);

    }

    public HttpStatus patchFolderName(String accessToken, Long folderId, String title){
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

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
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

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

        Iterable<Blob> blobs = bucket.list(Storage.BlobListOption.prefix("folders/" + folderId)).iterateAll();
        if(blobs == null) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER_FIREBASE);

        try {
            for (Blob blob : blobs) {
                blob.delete();
            }
            folderRepository.delete(folderEntity);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FAILED_FOLDER_DELETE);
        }

        return HttpStatus.OK;
    }

    public HttpStatus deleteDefaultFolder(UserEntity userEntity){
        if (userEntity == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (userEntity.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        FolderEntity folderEntity = folderRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(userEntity.getUserId());

        if(folderEntity == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_FOLDER);
        }

        if (!Objects.equals(userEntity.getUserId(), folderEntity.getUser().getUserId())){
            throw new CustomException(ErrorCode.MISMATCH_FOLDER_OWNER);
        }


        Iterable<Blob> blobs = bucket.list(Storage.BlobListOption.prefix("folders/" + folderEntity.getFolderId())).iterateAll();

        if(blobs == null ) throw new CustomException(ErrorCode.NOT_FOUND_FOLDER_FIREBASE);

        try{
            for (Blob blob : blobs) {
                blob.delete();
            }
            folderRepository.delete(folderEntity);
        }catch (Exception e) {
            throw new CustomException(ErrorCode.FAILED_FOLDER_DELETE);
        }

        return HttpStatus.OK;
    }

}

/* 파베 연결 코드
List<String> folders = new ArrayList<>();

        for (Blob blob : bucket.list(Storage.BlobListOption.prefix("folders/"), Storage.BlobListOption.currentDirectory()).iterateAll()) {
            if (blob.isDirectory()) {
                folders.add(blob.getName());
            }
        }
 */