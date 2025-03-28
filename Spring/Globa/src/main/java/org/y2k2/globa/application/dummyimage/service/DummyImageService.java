//package org.y2k2.globa.application.dummyimage.service;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import org.y2k2.globa.common.annotation.FileCleanup;
//import org.y2k2.globa.application.common.dto.file.FileDto;
//import org.y2k2.globa.application.dummyimage.dto.request.RequestDummyImageDto;
//import org.y2k2.globa.application.dummyimage.dto.response.ResponseDummyImageDto;
//import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;
//import org.y2k2.globa.infrastructure.persistence.dummyimage.repository.DummyImageJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
//import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;
//import org.y2k2.globa.common.exception.CustomException;
//import org.y2k2.globa.common.exception.ErrorCode;
//import org.y2k2.globa.common.exception.FileUploadException;
//import org.y2k2.globa.application.dummyimage.mapper.DummyImageMapper;
//import org.y2k2.globa.infrastructure.persistence.userrole.repository.UserRoleJpaRepository;
//import org.y2k2.globa.common.util.file.FileStore;
//import org.y2k2.globa.application.userrole.service.UserRoleService;
//
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class DummyImageService {
//    private final FileStore fileStore;
//
//    private final UserRoleService userRoleService;
//
//    private final DummyImageJpaRepository dummyImageRepository;
//    private final UserRoleJpaRepository userRoleJpaRepository;
//
//    @Transactional
//    @FileCleanup
//    public ResponseDummyImageDto addDummyImage(RequestDummyImageDto dto, UserEntity user) {
//        Optional<UserRoleEntity> optionalUserRole = userRoleJpaRepository.findByUser(user);
//
//        if (optionalUserRole.isEmpty()) {
//            userRoleService.createUserRoleAndThrowException(user);
//        } else {
//            boolean isAdminOrEditor = userRoleService.isAdminOrEditor(optionalUserRole.get());
//            if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);
//        }
//
//        FileDto fileDto = fileStore.storeFile("notices/images/", dto.image());
//
//        try {
//            DummyImageEntity dummyImage = DummyImageMapper.INSTANCE.toEntity(fileDto);
//            DummyImageEntity createdDummyImage = dummyImageRepository.save(dummyImage);
//            return DummyImageMapper.INSTANCE.toResponseDto(createdDummyImage);
//        } catch (Exception e) {
//            throw new FileUploadException(fileDto.storePath());
//        }
//    }
//}
