package org.y2k2.globa.infrastructure.persistence.inquiry.repository;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@RepositoryIntegrationTest
public class InquiryRepositoryTest {
    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private UserFixture userFixture;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = userFixture.save(
                UserFixture.builder().build()
        );
    }

    @Test
    @DisplayName("문의 내역 생성 - 성공")
    void saveInquiry() {
        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("title", Arbitraries.strings().ofMaxLength(80))
                .set("user", user)
                .set("isSolved", false)
                .sample();

        InquiryEntity savedInquiry = inquiryRepository.save(inquiry);

        log.info("Saved Inquiry: {}", savedInquiry.getInquiryId());

        Assertions
                .assertThat(savedInquiry.getInquiryId())
                .isNotNull();

        Assertions
                .assertThat(savedInquiry.getUser().getUserId())
                .isEqualTo(user.getUserId());

        Assertions
                .assertThat(savedInquiry.getIsSolved())
                .isFalse();
    }

    @Test
    @DisplayName("문의 내역 목록 조회 - 성공")
    void getInquiries() {
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

        Pageable pageable = PageRequest.of(0, 10);

        inquiryRepository.save(unSolvedInquiry);
        inquiryRepository.save(solvedInquiry);

        Page<InquiryEntity> inquiries = inquiryRepository.getInquiries(user.getUserId(), pageable);

        Assertions
                .assertThat(inquiries.getContent().size())
                .isEqualTo(2);

        Assertions
                .assertThat(inquiries.getContent())
                .isNotEmpty()
                .allSatisfy(i -> {
                    log.info("Retrieved Inquiry Id = {}, Solved = {}", i.getInquiryId(), i.getIsSolved());
                    Assertions.assertThat(i.getUser().getUserId()).isEqualTo(user.getUserId());
                });
    }

    @Test
    @DisplayName("문의 내역 조회 - 성공 (해결된 문의)")
    void getInquiry() {
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

        Pageable pageable = PageRequest.of(0, 10);

        inquiryRepository.save(unSolvedInquiry);
        inquiryRepository.save(solvedInquiry);

        Page<InquiryEntity> inquiries = inquiryRepository.getSolvedInquiries(user.getUserId(), pageable);

        Assertions
                .assertThat(inquiries.getContent().size())
                .isEqualTo(1);

        Assertions
                .assertThat(inquiries.getContent())
                .isNotEmpty()
                .allSatisfy(i -> {
                    log.info("Retrieved Inquiry Id = {}, Solved = {}", i.getInquiryId(), i.getIsSolved());
                    Assertions.assertThat(i.getUser().getUserId()).isEqualTo(user.getUserId());
                    Assertions.assertThat(i.getIsSolved()).isTrue();
                });
    }

    @Test
    @DisplayName("문의 내역 조회 - 성공 (미해결 문의)")
    void getUnsolvedInquiry() {
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

        Pageable pageable = PageRequest.of(0, 10);

        inquiryRepository.save(unSolvedInquiry);
        inquiryRepository.save(solvedInquiry);

        Page<InquiryEntity> inquiries = inquiryRepository.getUnsolvedInquiries(user.getUserId(), pageable);

        Assertions
                .assertThat(inquiries.getContent().size())
                .isEqualTo(1);

        Assertions
                .assertThat(inquiries.getContent())
                .isNotEmpty()
                .allSatisfy(i -> {
                    log.info("Retrieved Inquiry Id = {}, Solved = {}", i.getInquiryId(), i.getIsSolved());
                    Assertions.assertThat(i.getUser().getUserId()).isEqualTo(user.getUserId());
                    Assertions.assertThat(i.getIsSolved()).isFalse();
                });
    }

    @Test
    @DisplayName("문의 내역 조회 - 성공 (특정 문의)")
    void getInquiryById() {
        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("title", Arbitraries.strings().ofMaxLength(80))
                .set("user", user)
                .set("isSolved", false)
                .sample();

        InquiryEntity savedInquiry = inquiryRepository.save(inquiry);

        log.info("Saved Inquiry: {}", savedInquiry.getInquiryId());

        InquiryEntity retrievedInquiry = inquiryRepository.getInquiry(savedInquiry.getInquiryId())
                .orElseThrow(() -> new AssertionError("Inquiry not found"));

        Assertions
                .assertThat(retrievedInquiry.getInquiryId())
                .isEqualTo(savedInquiry.getInquiryId());

        Assertions
                .assertThat(retrievedInquiry.getUser().getUserId())
                .isEqualTo(user.getUserId());

        Assertions
                .assertThat(retrievedInquiry.getIsSolved())
                .isFalse();
    }

    @Test
    @DisplayName("문의 내역 조회 - 실패 (존재하지 않는 문의)")
    void getNonExistentInquiry() {
        Long nonExistentInquiryId = 999L; // Assuming this ID does not exist

        Optional<InquiryEntity> inquiry = inquiryRepository.getInquiry(nonExistentInquiryId);

        Assertions
                .assertThat(inquiry)
                .isEmpty();
    }
}
