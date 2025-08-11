package org.y2k2.globa.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@Component
public class RedisTestStore {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void setValueExpire(String key, String value, LocalDateTime expireTime) {
        CustomTimestamp customTimestamp = new CustomTimestamp(expireTime);

        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expireAt(key, Timestamp.valueOf(customTimestamp.getTimestamp()));
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}
