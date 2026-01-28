package com.example.adapter.ratelimit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.adapter.common.constant.StatusConstant;
import com.example.adapter.common.exception.RateLimitException;
import com.example.adapter.entity.RateLimitConfig;
import com.example.adapter.repository.RateLimitConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitConfigMapper rateLimitConfigMapper;
    private final StringRedisTemplate redisTemplate;

    @Value("${adapter.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${adapter.rate-limit.default-qps:100}")
    private int defaultQps;

    private static final String RATE_LIMIT_KEY_PREFIX = "adapter:ratelimit:";

    private static final String RATE_LIMIT_SCRIPT =
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = tonumber(redis.call('get', key) or '0') " +
            "if current >= limit then " +
            "    return 0 " +
            "else " +
            "    redis.call('incr', key) " +
            "    if current == 0 then " +
            "        redis.call('expire', key, window) " +
            "    end " +
            "    return 1 " +
            "end";

    public void checkRateLimit(Long tenantId, String apiCode) {
        if (!enabled) {
            return;
        }

        RateLimitConfig config = getConfig(tenantId, apiCode);
        if (config == null) {
            config = getConfig(tenantId, null);
        }

        int limit = defaultQps;
        int window = 1;
        if (config != null && config.getStatus() == StatusConstant.ENABLED) {
            limit = config.getLimitValue();
            window = config.getTimeWindow();
        }

        String key = buildKey(tenantId, apiCode);
        boolean allowed = tryAcquire(key, limit, window);

        if (!allowed) {
            log.warn("Rate limit exceeded, tenantId={}, apiCode={}, limit={}/{}s",
                    tenantId, apiCode, limit, window);
            throw new RateLimitException("Rate limit exceeded: " + limit + " requests per " + window + " seconds");
        }
    }

    private RateLimitConfig getConfig(Long tenantId, String apiCode) {
        LambdaQueryWrapper<RateLimitConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RateLimitConfig::getTenantId, tenantId);
        if (apiCode != null) {
            wrapper.eq(RateLimitConfig::getApiCode, apiCode);
        } else {
            wrapper.isNull(RateLimitConfig::getApiCode);
        }
        return rateLimitConfigMapper.selectOne(wrapper);
    }

    private String buildKey(Long tenantId, String apiCode) {
        return RATE_LIMIT_KEY_PREFIX + tenantId + ":" + (apiCode != null ? apiCode : "default");
    }

    private boolean tryAcquire(String key, int limit, int window) {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script,
                    Collections.singletonList(key),
                    String.valueOf(limit),
                    String.valueOf(window));
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Failed to check rate limit, key={}", key, e);
            return true;
        }
    }
}
