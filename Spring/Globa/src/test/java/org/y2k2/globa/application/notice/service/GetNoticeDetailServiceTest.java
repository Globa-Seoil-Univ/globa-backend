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
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeDetailDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class GetNoticeDetailServiceTest {
    @InjectMocks
    private GetNoticeDetailService getNoticeDetailService;

    @Mock
    private NoticeRepository noticeRepository;

    @Test
    @DisplayName("공지사항 상세 조회 - 성공")
    void getNoticeDetail_Success() {
        Long noticeId = 1L;
        NoticeEntity notice = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(NoticeEntity.class)
                .set("noticeId", noticeId)
                .sample();

        Mockito
                .when(noticeRepository.getNotice(noticeId))
                .thenReturn(Optional.of(notice));

        ResponseNoticeDetailDto response = getNoticeDetailService.get(notice.getNoticeId());

        Assertions
                .assertThat(response)
                .isNotNull();

        Assertions
                .assertThat(response.title())
                .isEqualTo(notice.getTitle());

        Assertions
                .assertThat(response.content())
                .isEqualTo(notice.getContent());

        Mockito
                .verify(noticeRepository, Mockito.times(1))
                .getNotice(noticeId);
    }

    @Test
    @DisplayName("공지사항 상세 조회 - 실패 (공지사항 X)")
    void getNoticeDetail_NotFound() {
        Long noticeId = 1L;

        Mockito
                .when(noticeRepository.getNotice(noticeId))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> getNoticeDetailService.get(noticeId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_NOTICE);

        Mockito
                .verify(noticeRepository, Mockito.times(1))
                .getNotice(noticeId);
    }
}
