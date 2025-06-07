package org.y2k2.globa.application.dummyimage.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.dummyimage.dto.request.RequestDummyImageDto;
import org.y2k2.globa.application.dummyimage.dto.response.ResponseDummyImageDto;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;

@ExtendWith(MockitoExtension.class)
public class CreateDummyImageServiceTest {
    @InjectMocks
    private CreateDummyImageService createDummyImageService;

    @Mock
    private VerifyUserWritableUseCase verifyUserWritableUseCase;
    @Mock
    private DummyImageRepository dummyImageRepository;
    @Mock
    private FileStore fileStore;

    @Test
    @DisplayName("더미 이미지 생성 - 성공 (Admin)")
    void createDummyImage_Success() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image", "dummy.jpg", "image/png", new byte[]{}
        );
        RequestDummyImageDto dto = new RequestDummyImageDto(multipartFile);

        FileDto fileDto = new FileDto(
                "dummy.png",
                "AISDKNALND234523.png",
                "notices/images/AISDKNALND234523.png",
                "image/png",
                1024L
        );

        DummyImageEntity dummyImage = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(DummyImageEntity.class)
                .set("imagePath", fileDto.storePath())
                .set("imageType", fileDto.extension())
                .set("imageSize", fileDto.size())
                .sample();

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(Mockito.anyLong());

        Mockito
                .when(fileStore.storeFile("notices/images/", dto.image()))
                .thenReturn(fileDto);

        Mockito
                .when(dummyImageRepository.save(Mockito.any(DummyImageEntity.class)))
                .thenReturn(dummyImage);

        ResponseDummyImageDto response = createDummyImageService.create(dto, 1L);

        Assertions
                .assertThat(response.imageId())
                .isEqualTo(dummyImage.getImageId());

        Assertions
                .assertThat(response.path())
                .isEqualTo(dummyImage.getImagePath());

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(Mockito.anyLong());

        Mockito
                .verify(fileStore, Mockito.times(1))
                .storeFile(Mockito.eq("notices/images/"), Mockito.any());

        Mockito
                .verify(dummyImageRepository, Mockito.times(1))
                .save(Mockito.any(DummyImageEntity.class));
    }

    @Test
    @DisplayName("더미 이미지 생성 - 실패 (파일 업로드 실패)")
    void createDummyImage_Failure_FileUpload() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image", "dummy.jpg", "image/png", new byte[]{}
        );
        RequestDummyImageDto dto = new RequestDummyImageDto(multipartFile);

        FileDto fileDto = new FileDto(
                "dummy.png",
                "AISDKNALND234523.png",
                "notices/images/AISDKNALND234523.png",
                "image/png",
                1024L
        );

        DummyImageEntity dummyImage = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(DummyImageEntity.class)
                .set("imagePath", fileDto.storePath())
                .set("imageType", fileDto.extension())
                .set("imageSize", fileDto.size())
                .sample();

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(Mockito.anyLong());

        Mockito
                .when(fileStore.storeFile("notices/images/", dto.image()))
                .thenReturn(fileDto);

        Mockito
                .when(dummyImageRepository.save(Mockito.any(DummyImageEntity.class)))
                .thenThrow(new RuntimeException("Failed to save dummy image"));

        Assertions
                .assertThatThrownBy(() -> createDummyImageService.create(dto, 1L))
                .isInstanceOf(FileUploadException.class);

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(Mockito.anyLong());

        Mockito
                .verify(fileStore, Mockito.times(1))
                .storeFile(Mockito.eq("notices/images/"), Mockito.any());

        Mockito
                .verify(dummyImageRepository, Mockito.times(1))
                .save(Mockito.any(DummyImageEntity.class));
    }
}
