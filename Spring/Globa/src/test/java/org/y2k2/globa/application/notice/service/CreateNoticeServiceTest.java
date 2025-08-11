package org.y2k2.globa.application.notice.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.notice.dto.request.RequestNoticeAddDto;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithTopicDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.domain.noticeimage.repository.NoticeImageRepository;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.noticeimage.entity.NoticeImageEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateNoticeServiceTest {
    @InjectMocks
    private CreateNoticeService createNoticeService;

    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private FileStore fileStore;
    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private VerifyUserWritableUseCase verifyUserWritableUseCase;
    @Mock
    private CreateNotificationUseCase createNotificationUseCase;
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private NoticeImageRepository noticeImageRepository;
    @Mock
    private DummyImageRepository dummyImageRepository;

    @Test
    @DisplayName("공지사항 생성 - 성공 (이미지 X)")
    void createNotice_Success() {
        Long userId = 1L;

        MultipartFile mockThumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        RequestNoticeAddDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestNoticeAddDto.class)
                .set("imageIds", null)
                .set("thumbnail", mockThumbnail)
                .sample();

        UserEntity uploader = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        FileDto thumbnail = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FileDto.class)
                .set("storePath", "notices/thumbnails/" + Arbitraries.strings())
                .sample();

        NoticeEntity notice = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NoticeEntity.class)
                .set("thumbnailPath", thumbnail.storePath())
                .set("user", uploader)
                .set("title", dto.title())
                .set("content", dto.content())
                .set("bgColor", dto.bgColor())
                .sample();

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(uploader.getUserId());

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(uploader);

        Mockito
                .when(fileStore.storeFile(Mockito.anyString(), Mockito.any(MultipartFile.class)))
                .thenReturn(thumbnail);

        Mockito
                .when(noticeRepository.save(Mockito.any(NoticeEntity.class)))
                .thenReturn(notice);

        Mockito
                .doNothing()
                .when(createNotificationUseCase)
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(RequestNotificationWithTopicDto.class));

        Long createdNoticeId = createNoticeService.create(dto, userId);

        Assertions
                .assertThat(createdNoticeId)
                .isEqualTo(notice.getNoticeId());

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(fileStore, Mockito.times(1))
                .storeFile(Mockito.eq("notices/thumbnails/"), Mockito.any(MultipartFile.class));

        Mockito
                .verify(noticeRepository, Mockito.times(1))
                .save(Mockito.any(NoticeEntity.class));

        Mockito
                .verify(dummyImageRepository, Mockito.never())
                .getImages(Mockito.anyList());

        Mockito
                .verify(dummyImageRepository, Mockito.never())
                .deleteAll(Mockito.anyList());

        Mockito
                .verify(noticeImageRepository, Mockito.never())
                .saveAll(Mockito.anyList());

        Mockito
                .verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(RequestNotificationWithTopicDto.class));
    }

    @Test
    @DisplayName("공지사항 생성 - 성공 (이미지 O)")
    void createNoticeWithImages_Success() {
        Long userId = 1L;
        Long[] imageIds = {1L, 2L, 3L};

        MultipartFile mockThumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        RequestNoticeAddDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestNoticeAddDto.class)
                .set("imageIds", imageIds)
                .set("thumbnail", mockThumbnail)
                .sample();

        UserEntity uploader = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        FileDto thumbnail = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FileDto.class)
                .set("storePath", "notices/thumbnails/" + Arbitraries.strings())
                .sample();

        NoticeEntity notice = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NoticeEntity.class)
                .set("thumbnailPath", thumbnail.storePath())
                .set("user", uploader)
                .set("title", dto.title())
                .set("content", dto.content())
                .set("bgColor", dto.bgColor())
                .sample();

        List<DummyImageEntity> dummyImages = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMe(DummyImageEntity.class, imageIds.length);

        List<NoticeImageEntity> noticeImages = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMe(NoticeImageEntity.class, imageIds.length);

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(uploader.getUserId());

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(uploader);

        Mockito
                .when(fileStore.storeFile(Mockito.anyString(), Mockito.any(MultipartFile.class)))
                .thenReturn(thumbnail);

        Mockito
                .when(noticeRepository.save(Mockito.any(NoticeEntity.class)))
                .thenReturn(notice);

        Mockito
                .when(dummyImageRepository.getImages(Mockito.anyList()))
                .thenReturn(dummyImages);

        Mockito
                .doNothing()
                .when(dummyImageRepository)
                .deleteAll(Mockito.anyList());

        Mockito
                .when(noticeImageRepository.saveAll(Mockito.anyList()))
                .thenReturn(noticeImages);

        Mockito
                .doNothing()
                .when(createNotificationUseCase)
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(RequestNotificationWithTopicDto.class));

        Long createdNoticeId = createNoticeService.create(dto, userId);

        Assertions
                .assertThat(createdNoticeId)
                .isEqualTo(notice.getNoticeId());

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(fileStore, Mockito.times(1))
                .storeFile(Mockito.eq("notices/thumbnails/"), Mockito.any(MultipartFile.class));

        Mockito
                .verify(noticeRepository, Mockito.times(1))
                .save(Mockito.any(NoticeEntity.class));

        Mockito
                .verify(dummyImageRepository, Mockito.times(1))
                .getImages(Mockito.anyList());

        Mockito
                .verify(dummyImageRepository, Mockito.times(1))
                .deleteAll(Mockito.anyList());

        Mockito
                .verify(noticeImageRepository, Mockito.times(1))
                .saveAll(Mockito.anyList());

        Mockito
                .verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(RequestNotificationWithTopicDto.class));
    }

    @Test
    @DisplayName("공지사항 생성 - 실패 (파일 업로드 예외 발생)")
    void createNotice_Failure_FileUploadException() {
        Long userId = 1L;

        MultipartFile mockThumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        RequestNoticeAddDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestNoticeAddDto.class)
                .set("imageIds", null)
                .set("thumbnail", mockThumbnail)
                .sample();

        UserEntity uploader = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        FileDto thumbnail = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FileDto.class)
                .set("storePath", "notices/thumbnails/" + Arbitraries.strings())
                .sample();

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(uploader.getUserId());

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(uploader);

        Mockito
                .when(fileStore.storeFile(Mockito.anyString(), Mockito.any(MultipartFile.class)))
                .thenReturn(thumbnail);

        Mockito
                .when(noticeRepository.save(Mockito.any(NoticeEntity.class)))
                .thenThrow(new RuntimeException("File upload failed"));

        Assertions
                .assertThatThrownBy(() -> createNoticeService.create(dto, userId))
                .isInstanceOf(FileUploadException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_FILE_UPLOAD);
        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(fileStore, Mockito.times(1))
                .storeFile(Mockito.eq("notices/thumbnails/"), Mockito.any(MultipartFile.class));

        Mockito
                .verify(noticeRepository, Mockito.times(1))
                .save(Mockito.any(NoticeEntity.class));

        Mockito
                .verify(dummyImageRepository, Mockito.never())
                .getImages(Mockito.anyList());

        Mockito
                .verify(dummyImageRepository, Mockito.never())
                .deleteAll(Mockito.anyList());

        Mockito
                .verify(noticeImageRepository, Mockito.never())
                .saveAll(Mockito.anyList());

        Mockito
                .verify(createNotificationUseCase, Mockito.never())
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(publisher, Mockito.never())
                .publishEvent(Mockito.any(RequestNotificationWithTopicDto.class));
    }
}
