package org.y2k2.globa.common.util.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStore {
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Redis에 key, value를 저장합니다. <br>
     * 만료 시간을 지정하여 저장합니다.
     *
     * @param key   저장할 key
     * @param value 저장할 value
     */
    public void setValueExpire(String key, String value, LocalDateTime expireTime) {
        CustomTimestamp customTimestamp = new CustomTimestamp(expireTime);

        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expireAt(key, Timestamp.valueOf(customTimestamp.getTimestamp()));
    }

    /**
     * Redis에서 key를 기준으로 value를 가져옵니다.
     *
     * @param key   가져올 key
     */
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Redis에서 key를 기준으로 value를 가져옵니다.
     *
     * @param key   가져올 key
     */
    public String getValue(Long key) {
        return redisTemplate.opsForValue().get(String.valueOf(key));
    }

    /**
     * Redis에 저장된 key를 삭제합니다.
     *
     * @param key 삭제할 key
     */
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}
