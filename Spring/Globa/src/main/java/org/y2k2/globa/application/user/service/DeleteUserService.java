package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.survey.dto.request.RequestSurveyDto;
import org.y2k2.globa.application.survey.mapper.SurveyMapper;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.domain.survey.repository.SurveyRepository;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Service
public class DeleteUserService {
    private final FindUserUseCase findUserUseCase;

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;

    public void delete(RequestSurveyDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);
        user.delete();

        userRepository.save(user);
        surveyRepository.save(SurveyMapper.INSTANCE.toEntity(dto));
    }
}
