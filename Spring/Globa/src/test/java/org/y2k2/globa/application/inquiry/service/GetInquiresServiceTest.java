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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.y2k2.globa.application.inquiry.dto.common.InquiryDto;
import org.y2k2.globa.application.inquiry.dto.request.InquiryPaginationDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDto;
import org.y2k2.globa.common.type.InquirySort;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetInquiresServiceTest {
    @InjectMocks
    private GetInquiresService getInquiresService;

    @Mock
    private InquiryRepository inquiryRepository;

    @Test
    @DisplayName("문의 목록 조회 - 성공 (최신)")
    void getInquires_Success() {
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
        InquiryEntity solvedInquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("title", Arbitraries.strings().ofMaxLength(80))
                .set("user", user)
                .set("isSolved", true)
                .sample();

        InquiryPaginationDto pagination = new InquiryPaginationDto(1, 10, InquirySort.R);
        Page<InquiryEntity> inquiries = new PageImpl<>(List.of(unSolvedInquiry, solvedInquiry));

        Mockito
                .when(inquiryRepository.getInquiries(Mockito.anyLong(), Mockito.any()))
                .thenReturn(inquiries);

        ResponseInquiryDto response = getInquiresService.get(pagination, user.getUserId());

        Assertions
                .assertThat(response.inquires())
                .hasSize(2)
                .extracting(InquiryDto::inquiryId)
                .containsExactlyInAnyOrder(
                        unSolvedInquiry.getInquiryId(),
                        solvedInquiry.getInquiryId()
                );

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .getSolvedInquiries(Mockito.anyLong(), Mockito.any());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .getUnsolvedInquiries(Mockito.anyLong(), Mockito.any());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getInquiries(Mockito.eq(user.getUserId()), Mockito.any());
    }

    @Test
    @DisplayName("문의 목록 조회 - 성공 (해결된 문의)")
    void getInquires_Success_Solved() {
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

        InquiryPaginationDto pagination = new InquiryPaginationDto(1, 10, InquirySort.S);
        Page<InquiryEntity> inquiries = new PageImpl<>(List.of(solvedInquiry));

        Mockito
                .when(inquiryRepository.getSolvedInquiries(Mockito.anyLong(), Mockito.any()))
                .thenReturn(inquiries);

        ResponseInquiryDto response = getInquiresService.get(pagination, user.getUserId());

        Assertions
                .assertThat(response.inquires())
                .hasSize(1)
                .extracting(InquiryDto::inquiryId)
                .containsExactly(solvedInquiry.getInquiryId());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .getUnsolvedInquiries(Mockito.anyLong(), Mockito.any());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .getInquiries(Mockito.anyLong(), Mockito.any());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getSolvedInquiries(Mockito.eq(user.getUserId()), Mockito.any());
    }

    @Test
    @DisplayName("문의 목록 조회 - 성공 (미해결 문의)")
    void getInquires_Success_Unsolved() {
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

        InquiryPaginationDto pagination = new InquiryPaginationDto(1, 10, InquirySort.N);
        Page<InquiryEntity> inquiries = new PageImpl<>(List.of(unSolvedInquiry));

        Mockito
                .when(inquiryRepository.getUnsolvedInquiries(Mockito.anyLong(), Mockito.any()))
                .thenReturn(inquiries);

        ResponseInquiryDto response = getInquiresService.get(pagination, user.getUserId());

        Assertions
                .assertThat(response.inquires())
                .hasSize(1)
                .extracting(InquiryDto::inquiryId)
                .containsExactly(unSolvedInquiry.getInquiryId());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .getSolvedInquiries(Mockito.anyLong(), Mockito.any());

        Mockito
                .verify(inquiryRepository, Mockito.never())
                .getInquiries(Mockito.anyLong(), Mockito.any());

        Mockito
                .verify(inquiryRepository, Mockito.times(1))
                .getUnsolvedInquiries(Mockito.eq(user.getUserId()), Mockito.any());
    }
}
