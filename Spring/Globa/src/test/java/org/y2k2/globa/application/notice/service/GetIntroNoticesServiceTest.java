package org.y2k2.globa.application.notice.service;

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
import org.springframework.data.domain.Limit;
import org.y2k2.globa.application.notice.dto.response.NoticeIntroDto;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeIntroDto;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetIntroNoticesServiceTest {
    @InjectMocks
    private GetIntroNoticesService getIntroNoticesService;

    @Mock
    private NoticeRepository noticeRepository;

    @Test
    @DisplayName("공지사항 3개 조회 - 성공")
    void getIntroNotices_Success() {
        List<NoticeEntity> notices = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMe(NoticeEntity.class, 3);

        Mockito
                .when(noticeRepository.getNotices(Mockito.any(Limit.class)))
                .thenReturn(notices);

        ResponseNoticeIntroDto response = getIntroNoticesService.get();

        Assertions
                .assertThat(response.notices())
                .isNotNull()
                .hasSize(3)
                .allSatisfy(dto -> {
                    Assertions.assertThat(dto.noticeId()).isNotNull();
                });

        Mockito.verify(noticeRepository, Mockito.times(1))
                .getNotices(Mockito.any(Limit.class));
    }

    @Test
    @DisplayName("공지사항 3개 조회 - 성공 (빈 목록)")
    void getIntroNotices_EmptyList() {
        Mockito
                .when(noticeRepository.getNotices(Mockito.any(Limit.class)))
                .thenReturn(List.of());

        ResponseNoticeIntroDto response = getIntroNoticesService.get();

        Assertions
                .assertThat(response.notices())
                .isNotNull()
                .isEmpty();

        Mockito.verify(noticeRepository, Mockito.times(1))
                .getNotices(Mockito.any(Limit.class));
    }
}
