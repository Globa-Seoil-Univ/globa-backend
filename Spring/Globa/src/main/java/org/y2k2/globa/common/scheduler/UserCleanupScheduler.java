package org.y2k2.globa.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.service.UserCleanupService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {
    private final UserCleanupService userCleanupService;

    @Scheduled(cron = "0 0 4 * * ?") // 매일 새벽 4시마다 실행
    public void cleanupDeletedUsers() {
        log.info("Starting user cleanup process...");

        try {
            int deletedCount = userCleanupService.process();
            log.info("{} users have been cleaned up.", deletedCount);
        } catch (Exception e) {
            log.error("Error during user cleanup process = ", e);
        }
    }
}
