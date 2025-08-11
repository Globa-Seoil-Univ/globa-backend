package org.y2k2.globa.application.answer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.answer.repository.AnswerRepository;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;

@Service
@RequiredArgsConstructor
public class DeleteAnswerService {
    private final VerifyUserWritableUseCase verifyUserWritableUseCase;

    private final InquiryRepository inquiryRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public void delete(long inquiryId, long answerId, Long userId) {
        verifyUserWritableUseCase.execute(userId);

        InquiryEntity inquiry = inquiryRepository.getInquiry(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));

        AnswerEntity answer = answerRepository.getAnswerById(answerId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANSWER));

        if (inquiry.getIsSolved()) {
            inquiry.setIsSolved(false);
        }

        inquiryRepository.save(inquiry);
        answerRepository.delete(answer);
    }
}
