package org.y2k2.globa.service;

import com.google.cloud.storage.*;
import jakarta.mail.Folder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.FolderDto;
import org.y2k2.globa.dto.InvitationStatus;
import org.y2k2.globa.dto.Role;
import org.y2k2.globa.dto.ShareTarget;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.BadRequestFolderException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.exception.UnAuthorizedException;
import org.y2k2.globa.mapper.FolderMapper;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final JwtTokenProvider jwtTokenProvider;
    private final FolderShareService folderShareService;
    private final JwtUtil jwtUtil;

    public final UserRepository userRepository;;
    public final StudyRepository studyRepository;
    public final SurveyRepository surveyRepository;
    public final FolderRepository folderRepository;
    public final FolderShareRepository folderShareRepository;
    public final FolderRoleRepository folderRoleRepository;

    @Autowired
    private Bucket bucket;

    public List<FolderDto> getFolders(String accessToken, int page, int count){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        Pageable pageable = PageRequest.of(page-1, count);
        Page<FolderEntity> folders = folderRepository.findAllByUserUserId(pageable, userId);



        return folders.stream()
                .map(FolderMapper.INSTANCE::toFolderDto)
                .collect(Collectors.toList());

    }

    public FolderDto postDefaultFolder(UserEntity userEntity, String Code){
        FolderEntity saveFolderEntity = new FolderEntity();
        saveFolderEntity.setUser(userEntity);
        saveFolderEntity.setTitle(userEntity.getName() + "의 기본 폴더");
        saveFolderEntity.setCreatedTime(LocalDateTime.now());


        FolderEntity savedEntity = folderRepository.save(saveFolderEntity);


        FolderShareEntity saveShareEntity = new FolderShareEntity();
        saveShareEntity.setFolder(savedEntity);
        saveShareEntity.setRoleId(folderRoleRepository.findByRoleName("소유자"));
        saveShareEntity.setOwnerUser(userEntity);
        saveShareEntity.setTargetUser(userEntity);

        folderShareRepository.save(saveShareEntity);


        BlobInfo blobInfo = BlobInfo.newBuilder("${firebase.bucket-path}", "folders/"+ Code + "/placeholder.txt").build();
        byte[] content = new byte[0]; // 빈 콘텐츠라도 넣어줘야 폴더가 생김 ㅠㅠ
        bucket.create(blobInfo.getName(), content);
        System.out.println("Folder created at path: " + Storage.BlobListOption.prefix("folders/"));

        return FolderMapper.INSTANCE.toFolderDto(savedEntity);

    }

    public FolderDto postFolder(String accessToken, String title){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByUserId(userId);

        FolderEntity saveFolderEntity = new FolderEntity();
        saveFolderEntity.setUser(userEntity);
        saveFolderEntity.setTitle(title);
        saveFolderEntity.setCreatedTime(LocalDateTime.now());


        FolderEntity savedEntity = folderRepository.save(saveFolderEntity);


        FolderShareEntity saveShareEntity = new FolderShareEntity();
        saveShareEntity.setFolder(savedEntity);
        saveShareEntity.setRoleId(folderRoleRepository.findByRoleName("소유자"));
        saveShareEntity.setOwnerUser(userEntity);
        saveShareEntity.setTargetUser(userEntity);

        folderShareRepository.save(saveShareEntity);


        BlobInfo blobInfo = BlobInfo.newBuilder("${firebase.bucket-path}", "folders/"+savedEntity.getFolderId() + "/placeholder.txt").build();
        byte[] content = new byte[0]; // 빈 콘텐츠라도 넣어줘야 폴더가 생김 ㅠㅠ
        bucket.create(blobInfo.getName(), content);
        System.out.println("Folder created at path: " + Storage.BlobListOption.prefix("folders/"));

        return FolderMapper.INSTANCE.toFolderDto(savedEntity);

    }

    public FolderDto postFolder(String accessToken, String title, List<ShareTarget> shareTargets){

        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.

        UserEntity userEntity = userRepository.findOneByUserId(userId);

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
        folderShareRepository.save(saveOwnerShareEntity);




        // 리스트를 순회하며 각 공유 타겟 수행
        for (ShareTarget target : shareTargets) {
            System.out.println("Code: " + target.getCode() + ", Role: " + target.getRole());
            UserEntity targetEntity = userRepository.findOneByCode(target.getCode());

            if (target.getRole().isEmpty()) throw new BadRequestException("You must be request role field");
            if (!target.getRole().toUpperCase().equals(Role.R.toString())
                    && !target.getRole().toUpperCase().equals(Role.W.toString())) {
                throw new BadRequestException("Role field must be only 'r' or 'w'");
            }

            folderShareService.inviteShare(savedEntity.getFolderId(), userEntity.getUserId(), targetEntity.getUserId(), Role.valueOf(target.getRole().toUpperCase()));
        }

        BlobInfo blobInfo = BlobInfo.newBuilder("${firebase.bucket-path}", "folders/"+savedEntity.getFolderId() + "/placeholder.txt").build();
        byte[] content = new byte[0]; // 빈 콘텐츠라도 넣어줘야 폴더가 생김 ㅠㅠ
        bucket.create(blobInfo.getName(), content);
        System.out.println("Folder created at path: " + Storage.BlobListOption.prefix("folders/"));

        return FolderMapper.INSTANCE.toFolderDto(savedEntity);

    }

    public HttpStatus patchFolderName(String accessToken, Long folderId, String title){
        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        FolderEntity folderEntity = folderRepository.findFolderEntityByFolderId(folderId);

        if (!Objects.equals(userId, folderEntity.getUser().getUserId())){
            throw new UnAuthorizedException("Not Matched User ");
        }

        if(folderEntity == null)
            throw new NotFoundException("Folder Id not found ! ");

        folderEntity.setTitle(title);

        folderRepository.save(folderEntity);


        return HttpStatus.OK;
    }

    public HttpStatus deleteFolderName(String accessToken, Long folderId){
        Long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken); // 사용하지 않아도, 작업을 거치며 토큰 유효성 검사함.
        UserEntity userEntity = userRepository.findOneByUserId(userId);
        FolderEntity folderEntity = folderRepository.findFolderEntityByFolderId(folderId);
        FolderEntity defaultFolderEntity = folderRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(userEntity.getUserId());

        if(folderEntity == null) {
            throw new NotFoundException("Folder not found ! ");
        }

        if (!Objects.equals(userId, folderEntity.getUser().getUserId())){
            throw new UnAuthorizedException("Not Matched User ");
        }

        if(folderEntity == defaultFolderEntity)
            throw new BadRequestFolderException("Default Folder Cannot Be Deleted");



        for (Blob blob : bucket.list(Storage.BlobListOption.prefix("folders/" + folderId)).iterateAll()) {
            blob.delete();
        }

        folderRepository.delete(folderEntity);


        return HttpStatus.OK;
    }

    public HttpStatus deleteDefaultFolder(UserEntity userEntity){
        FolderEntity folderEntity = folderRepository.findFirstByUserUserIdOrderByCreatedTimeAsc(userEntity.getUserId());

        if(folderEntity == null) {
            throw new NotFoundException("Folder not found ! ");
        }

        if (!Objects.equals(userEntity.getUserId(), folderEntity.getUser().getUserId())){
            throw new UnAuthorizedException("Not Matched User ");
        }


        for (Blob blob : bucket.list(Storage.BlobListOption.prefix("folders/" + userEntity.getCode())).iterateAll()) {
            blob.delete();

        }

        folderRepository.delete(folderEntity);


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