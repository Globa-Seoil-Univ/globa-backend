package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.InquiryEntity;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    Optional<InquiryEntity> findByInquiryId(long inquiryId);
    Page<InquiryEntity> findAllByUserOrderByCreatedTimeDesc(UserEntity user, Pageable pageable);
    Page<InquiryEntity> findAllByUserAndIsSolvedIsTrueOrderByCreatedTimeDesc(UserEntity user, Pageable pageable);
    Page<InquiryEntity> findAllByUserAndIsSolvedIsFalseOrderByCreatedTimeDesc(UserEntity user, Pageable pageable);
}
