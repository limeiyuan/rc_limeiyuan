package com.example.adapter.service;

import com.example.adapter.entity.ApiParamConfig;
import com.example.adapter.entity.ExternalApiConfig;
import com.example.adapter.entity.Tenant;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 配置缓存服务 - 缓存租户和接口配置，减少数据库查询
 *
 * <p><b>注意：当前未启用</b></p>
 * <p>由于系统当前请求量不确定，租户配置需要实时查询数据库获取，保证配置变更的实时性。</p>
 * <p>此服务保留作为后续性能优化方案，当请求量增大时可启用缓存。</p>
 *
 * <p>启用方式：在 AdapterService 中注入 ConfigCacheService 替代直接调用 TenantService/ExternalApiConfigService</p>
 *
 * @see AdapterService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigCacheService {

    private final TenantService tenantService;
    private final ExternalApiConfigService apiConfigService;

    private Cache<String, Tenant> tenantCache;
    private Cache<String, ExternalApiConfig> apiConfigCache;
    private Cache<Long, List<ApiParamConfig>> paramConfigCache;

    @PostConstruct
    public void init() {
        tenantCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();

        apiConfigCache = CacheBuilder.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();

        paramConfigCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    public Tenant getActiveTenant(String tenantCode) {
        try {
            return tenantCache.get(tenantCode, () -> tenantService.getActiveTenant(tenantCode));
        } catch (Exception e) {
            return tenantService.getActiveTenant(tenantCode);
        }
    }

    public ExternalApiConfig getActiveConfig(String tenantCode, String apiCode) {
        String cacheKey = tenantCode + ":" + apiCode;
        try {
            return apiConfigCache.get(cacheKey, () -> apiConfigService.getActiveConfig(tenantCode, apiCode));
        } catch (Exception e) {
            return apiConfigService.getActiveConfig(tenantCode, apiCode);
        }
    }

    public List<ApiParamConfig> getParamConfigs(Long configId) {
        try {
            return paramConfigCache.get(configId, () -> apiConfigService.getParamConfigs(configId));
        } catch (Exception e) {
            return apiConfigService.getParamConfigs(configId);
        }
    }

    /**
     * 清除指定租户的缓存（配置更新时调用）
     */
    public void evictTenant(String tenantCode) {
        tenantCache.invalidate(tenantCode);
        log.info("Evicted tenant cache: {}", tenantCode);
    }

    /**
     * 清除指定接口配置的缓存（配置更新时调用）
     */
    public void evictApiConfig(String tenantCode, String apiCode) {
        apiConfigCache.invalidate(tenantCode + ":" + apiCode);
        log.info("Evicted api config cache: {}:{}", tenantCode, apiCode);
    }

    /**
     * 清除参数配置缓存
     */
    public void evictParamConfig(Long configId) {
        paramConfigCache.invalidate(configId);
        log.info("Evicted param config cache: {}", configId);
    }

    /**
     * 清除所有缓存
     */
    public void evictAll() {
        tenantCache.invalidateAll();
        apiConfigCache.invalidateAll();
        paramConfigCache.invalidateAll();
        log.info("Evicted all config caches");
    }
}
