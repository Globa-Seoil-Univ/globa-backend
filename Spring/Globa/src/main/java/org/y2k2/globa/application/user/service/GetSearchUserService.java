package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.application.user.mapper.UserMapper;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Service
public class GetSearchUserService {
    private final UserRepository userRepository;

    public ResponseUserSearchDto getSearchUser(String code) {
        UserEntity user = userRepository.getUserByCode(code)
                .orElse(null);

        if (user == null || user.getIsDeleted()) {
            return null;
        }

        return UserMapper.INSTANCE.toResponseUserSearchDto(user);
    }
}
