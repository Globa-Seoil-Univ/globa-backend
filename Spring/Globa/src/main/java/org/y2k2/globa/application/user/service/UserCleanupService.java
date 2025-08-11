package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.comment.usecase.CleanupCommentUseCase;
import org.y2k2.globa.application.record.command.CleanupRecordCommand;
import org.y2k2.globa.application.record.usecase.CleanupRecordUseCase;
import org.y2k2.globa.application.user.command.CleanupUserCommand;
import org.y2k2.globa.application.user.usecase.CleanupUserUseCase;
import org.y2k2.globa.application.userrole.command.CleanupUserRoleCommand;
import org.y2k2.globa.application.userrole.usecase.CleanupUserRoleUseCase;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCleanupService {
    private final UserRepository userRepository;

    private final CleanupUserUseCase cleanupUserUseCase;
    private final CleanupUserRoleUseCase cleanupUserRoleUseCase;
    private final CleanupRecordUseCase cleanupRecordUseCase;
    private final CleanupCommentUseCase cleanupCommentUseCase;

    public int process() {
        List<UserEntity> inactiveUsers = userRepository.getInActiveUsers();

        if (inactiveUsers.isEmpty()) {
            return 0;
        }

        List<Long> userIds = inactiveUsers.stream()
                .map(UserEntity::getUserId)
                .toList();

        // 1. 문서 삭제
        cleanupRecordUseCase.execute(CleanupRecordCommand.of(userIds));

        // 2. 사용자 권한 삭제
        cleanupUserRoleUseCase.execute(CleanupUserRoleCommand.of(userIds));

        // 3. 사용자 삭제
        cleanupUserUseCase.execute(CleanupUserCommand.of(inactiveUsers));

        // 4. 댓글 상태 변경
        cleanupCommentUseCase.execute(null);

        return inactiveUsers.size();
    }
}
