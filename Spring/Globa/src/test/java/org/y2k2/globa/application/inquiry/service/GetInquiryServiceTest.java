package org.y2k2.globa.application.inquiry.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDetailDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.answer.repository.AnswerRepository;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class GetInquiryServiceTest {
    @InjectMocks
    private GetInquiryService getInquiryService;

    @Mock
    private InquiryRepository inquiryRepository;
    @Mock
    private AnswerRepository answerRepository;

    @Test
    @DisplayName("문의 상세 조회 - 성공 (미해결)")
    void getUnSolvedInquiry_Success() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);
        InquiryEntity unSolvedInquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("title", Arbitraries.strings().ofMaxLength(80))
                .set("user", user)
                .set("isSolved", false)
                .sample();

        Mockito
                .when(inquiryRepository.getInquiry(unSolvedInquiry.getInquiryId()))
                .thenReturn(Optional.of(unSolvedInquiry));

        ResponseInquiryDetailDto responseInquiryDetailDto = getInquiryService.get(
                unSolvedInquiry.getInquiryId(),
                user.getUserId()
        );

        Assertions.assertThat(responseInquiryDetailDto.title())
                .isEqualTo(unSolvedInquiry.getTitle());

        Assertions.assertThat(responseInquiryDetailDto.answer())
                .isNull();

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(unSolvedInquiry.getInquiryId());

        Mockito.verify(answerRepository, Mockito.never())
                .getAnswerByInquiryId(unSolvedInquiry.getInquiryId());
    }

    @Test
    @DisplayName("문의 상세 조회 - 성공 (해결됨)")
    void getSolvedInquiry_Success() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);
        InquiryEntity solvedInquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("title", Arbitraries.strings().ofMaxLength(80))
                .set("user", user)
                .set("isSolved", true)
                .sample();
        AnswerEntity answer = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(AnswerEntity.class)
                .set("user", user)
                .set("inquiry", solvedInquiry)
                .sample();

        Mockito
                .when(inquiryRepository.getInquiry(solvedInquiry.getInquiryId()))
                .thenReturn(Optional.of(solvedInquiry));

        Mockito
                .when(answerRepository.getAnswerByInquiryId(solvedInquiry.getInquiryId()))
                .thenReturn(Optional.of(answer));

        ResponseInquiryDetailDto responseInquiryDetailDto = getInquiryService.get(
                solvedInquiry.getInquiryId(),
                user.getUserId()
        );

        Assertions
                .assertThat(responseInquiryDetailDto.title())
                .isEqualTo(solvedInquiry.getTitle());

        Assertions
                .assertThat(responseInquiryDetailDto.answer().title())
                .isEqualTo(answer.getTitle());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(solvedInquiry.getInquiryId());

        Mockito.verify(answerRepository, Mockito.times(1))
                .getAnswerByInquiryId(solvedInquiry.getInquiryId());
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (문의 X)")
    void getInquiry_NotFound() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);
        Long inquiryId = 1L;

        Mockito
                .when(inquiryRepository.getInquiry(inquiryId))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> getInquiryService.get(inquiryId, user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_INQUIRY);

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiryId);

        Mockito.verify(answerRepository, Mockito.never())
                .getAnswerByInquiryId(Mockito.anyLong());
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (문의 작성자 불일치)")
    void getInquiry_MismatchOwner() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);
        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("title", Arbitraries.strings().ofMaxLength(80))
                .set("user", user)
                .sample();
        Long otherUserId = 2L;

        Mockito
                .when(inquiryRepository.getInquiry(inquiry.getInquiryId()))
                .thenReturn(Optional.of(inquiry));

        Assertions.assertThatThrownBy(() -> getInquiryService.get(inquiry.getInquiryId(), otherUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_INQUIRY_OWNER);

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiry.getInquiryId());

        Mockito.verify(answerRepository, Mockito.never())
                .getAnswerByInquiryId(Mockito.anyLong());
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (답변 X)")
    void getInquiry_NotFoundAnswer() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(UserEntity.class);
        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("title", Arbitraries.strings().ofMaxLength(80))
                .set("user", user)
                .set("isSolved", true)
                .sample();

        Mockito
                .when(inquiryRepository.getInquiry(inquiry.getInquiryId()))
                .thenReturn(Optional.of(inquiry));

        Mockito
                .when(answerRepository.getAnswerByInquiryId(inquiry.getInquiryId()))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> getInquiryService.get(inquiry.getInquiryId(), user.getUserId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ANSWER);

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiry(inquiry.getInquiryId());

        Mockito.verify(answerRepository, Mockito.times(1))
                .getAnswerByInquiryId(inquiry.getInquiryId());
    }
}
