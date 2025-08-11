package org.y2k2.globa.infrastructure.persistence.dummyimage.repository;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;

import java.util.List;

@Slf4j
@RepositoryIntegrationTest
public class DummyImageRepositoryTest {
    @Autowired
    private DummyImageRepository dummyImageRepository;

    @Test
    @DisplayName("더미 이미지 생성 - 성공")
    void createDummyImage_Success() {
        DummyImageEntity dummyImage = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(DummyImageEntity.class)
                .set("imagePath", Arbitraries.strings().ofMaxLength(200))
                .set("imageType", Arbitraries.strings().ofMaxLength(20))
                .set("imageSize", Arbitraries.longs().between(1L, 1000000L))
                .sample();

        DummyImageEntity savedImage = dummyImageRepository.save(dummyImage);

        log.info("Saved Dummy Image = {}", savedImage.getImageId());
    }

    @Test
    @DisplayName("다중 더미 이미지 삭제 - 성공")
    void deleteAllDummyImages_Success() {
        List<DummyImageEntity> dummyImages = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(DummyImageEntity.class)
                .setNull("imageId")
                .set("imagePath", Arbitraries.strings().ofMaxLength(200))
                .set("imageType", Arbitraries.strings().ofMaxLength(20))
                .set("imageSize", Arbitraries.longs().between(1L, 10000L))
                .sampleList(10);

        for (DummyImageEntity dummyImage : dummyImages) {
            dummyImageRepository.save(dummyImage);
        }

        List<DummyImageEntity> savedImages = dummyImageRepository.getImages(
                dummyImages.stream().map(DummyImageEntity::getImageId).toList()
        );

        log.info("Saved Dummy Images Count = {}", savedImages.size());

        dummyImageRepository.deleteAll(savedImages);

        List<DummyImageEntity> deletedImages = dummyImageRepository.getImages(
                savedImages.stream().map(DummyImageEntity::getImageId).toList()
        );

        log.info("Deleted Dummy Images Count = {}", deletedImages.size());

        Assertions
                .assertThat(deletedImages)
                .isEmpty();
    }

    @Test
    @DisplayName("다수 더미 이미지 조회 - 성공")
    void getImages_Success() {
        List<DummyImageEntity> dummyImages = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(DummyImageEntity.class)
                .setNull("imageId")
                .set("imagePath", Arbitraries.strings().ofMaxLength(200))
                .set("imageType", Arbitraries.strings().ofMaxLength(20))
                .set("imageSize", Arbitraries.longs().between(1L, 10000L))
                .sampleList(10);

        for (DummyImageEntity dummyImage : dummyImages) {
            dummyImageRepository.save(dummyImage);
        }

        List<DummyImageEntity> savedImages = dummyImageRepository.getImages(
                dummyImages.stream().map(DummyImageEntity::getImageId).toList()
        );

        log.info("Retrieved Dummy Images Count = {}", savedImages.size());

        Assertions
                .assertThat(savedImages)
                .hasSize(dummyImages.size());
    }
}
