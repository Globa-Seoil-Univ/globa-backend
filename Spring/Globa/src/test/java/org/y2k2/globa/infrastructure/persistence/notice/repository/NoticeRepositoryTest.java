package org.y2k2.globa.infrastructure.persistence.notice.repository;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.fixture.notice.NoticeFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
@RepositoryIntegrationTest
public class NoticeRepositoryTest {
    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserFixture userFixture;

    private UserEntity admin;

    @BeforeEach
    void setUp() {
        // 권한은 설정하진 않았지만 Admin이라고 가정하고 테스트를 진행한다.
        admin = userFixture.save(
                UserFixture.builder().build()
        );
    }

    @Test
    @DisplayName("공지사항 생성 - 성공")
    void saveNotice_Success() {
        NoticeEntity notice = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NoticeEntity.class)
                .set("user", admin)
                .set("title", Arbitraries.strings().ofMaxLength(80))
                .set("thumbnailPath", Arbitraries.strings().ofMaxLength(200))
                .set("thumbnailType", Arbitraries.strings().ofMaxLength(20))
                .set("thumbnailSize", Arbitraries.longs().between(0, 100000))
                // 배경색은 8자리의 16진수 문자열로 설정 (예: #RRGGBBAA)
                .set("bgColor", "#" + Arbitraries.strings().ofMinLength(8).ofMaxLength(8).alpha().numeric().sample())
                .sample();

        NoticeEntity savedNotice = noticeRepository.save(notice);

        log.info("Saved notice = {}", savedNotice.getNoticeId());
        log.info("bgColor = {}", savedNotice.getBgColor());

        Assertions.assertThat(savedNotice.getNoticeId()).isNotNull();
        Assertions.assertThat(savedNotice.getUser().getUserId()).isEqualTo(admin.getUserId());
        Assertions.assertThat(savedNotice.getTitle()).isEqualTo(notice.getTitle());
        Assertions.assertThat(savedNotice.getThumbnailPath()).isEqualTo(notice.getThumbnailPath());
        Assertions.assertThat(savedNotice.getThumbnailType()).isEqualTo(notice.getThumbnailType());
        Assertions.assertThat(savedNotice.getThumbnailSize()).isEqualTo(notice.getThumbnailSize());
        Assertions.assertThat(savedNotice.getBgColor()).isEqualTo(notice.getBgColor());
    }

    @Test
    @DisplayName("공지사항 N개 조회 - 성공")
    void getNotices_Success() {
        int noticeCount = 5;

        for (int i = 0; i < noticeCount; i++) {
            NoticeEntity notice = NoticeFixture.builder()
                    .user(admin)
                    .build();
            noticeRepository.save(notice);
        }

        List<NoticeEntity> notices = noticeRepository.getNotices(Limit.of(10));
        log.info("Retrieved notices = {}", notices.stream().map(NoticeEntity::getNoticeId).toList());

        Assertions.assertThat(notices).isNotEmpty();
        Assertions.assertThat(notices.size()).isEqualTo(noticeCount);

        Assertions.assertThat(notices)
                .allSatisfy(notice -> {
                    Assertions.assertThat(notice.getNoticeId()).isNotNull();
                });
    }

    @Test
    @DisplayName("공지사항 N개 조회 - 성공 (빈 목록)")
    void getNotices_EmptyList() {
        List<NoticeEntity> notices = noticeRepository.getNotices(Limit.of(10));
        log.info("Retrieved notices = {}", notices);

        Assertions.assertThat(notices).isEmpty();
    }

    @Test
    @DisplayName("공지사항 상세 조회 - 성공")
    void getNotice_Success() {
        NoticeEntity notice = NoticeFixture.builder()
                .user(admin)
                .build();
        NoticeEntity savedNotice = noticeRepository.save(notice);

        log.info("Saved notice = {}", savedNotice.getNoticeId());

        Optional<NoticeEntity> retrievedNotice = noticeRepository.getNotice(savedNotice.getNoticeId());

        Assertions.assertThat(retrievedNotice).isPresent();

        log.info("Retrieved notice = {}", retrievedNotice.get().getNoticeId());

        Assertions.assertThat(retrievedNotice).isNotNull();
        Assertions.assertThat(retrievedNotice.get().getNoticeId()).isEqualTo(savedNotice.getNoticeId());
        Assertions.assertThat(retrievedNotice.get().getUser().getUserId()).isEqualTo(admin.getUserId());
    }
}
