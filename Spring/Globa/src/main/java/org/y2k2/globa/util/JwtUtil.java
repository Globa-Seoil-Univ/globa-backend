package org.y2k2.globa.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final RedisTemplate<String, String> redisTemplate;
    public Boolean insertRedisRefreshToken(Long id, String refreshToken) {

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(id.toString(), refreshToken);
        return true;
    }

    public String getRefreshToken(Long userId){
        return redisTemplate.opsForValue().get(String.valueOf(userId));
    }

    public void deleteValue(String key){
        redisTemplate.delete(key);
    }
}
