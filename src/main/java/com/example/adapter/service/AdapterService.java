package com.example.adapter.service;

import com.example.adapter.common.exception.RetryableException;
import com.example.adapter.dto.AdapterMessage;
import com.example.adapter.dto.HttpRequestContext;
import com.example.adapter.dto.HttpResponseResult;
import com.example.adapter.entity.ApiParamConfig;
import com.example.adapter.entity.ExternalApiConfig;
import com.example.adapter.entity.Tenant;
import com.example.adapter.http.HttpClientService;
import com.example.adapter.http.RequestBuilder;
import com.example.adapter.ratelimit.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdapterService {

    private final TenantService tenantService;
    private final ExternalApiConfigService apiConfigService;
    private final RateLimitService rateLimitService;
    private final RequestBuilder requestBuilder;
    private final HttpClientService httpClientService;
    private final RequestLogService requestLogService;

    public void process(AdapterMessage message) {
        Tenant tenant = tenantService.getActiveTenant(message.getTenantCode());
        ExternalApiConfig apiConfig = apiConfigService.getActiveConfig(message.getTenantCode(), message.getApiCode());

        rateLimitService.checkRateLimit(tenant.getId(), message.getApiCode());

        List<ApiParamConfig> paramConfigs = apiConfigService.getParamConfigs(apiConfig.getId());

        HttpRequestContext context = requestBuilder.build(apiConfig, paramConfigs, message);

        HttpResponseResult result = executeWithRetry(context, apiConfig.getRetryCount());

        requestLogService.logRequest(context, result);

        if (!result.isSuccess()) {
            log.error("HTTP request failed after retries, traceId={}, error={}",
                    message.getTraceId(), result.getErrorMessage());
            // 网络异常、超时等环境原因，抛出可重试异常
            throw new RetryableException("HTTP request failed: " + result.getErrorMessage());
        }

        // 检查HTTP状态码，5xx错误视为环境问题，需要重试
        if (result.getStatusCode() >= 500) {
            log.error("HTTP request returned server error, traceId={}, statusCode={}",
                    message.getTraceId(), result.getStatusCode());
            throw new RetryableException("HTTP request returned server error: " + result.getStatusCode());
        }
    }

    private HttpResponseResult executeWithRetry(HttpRequestContext context, int maxRetry) {
        HttpResponseResult result = null;
        int retryCount = 0;

        do {
            if (retryCount > 0) {
                log.info("Retrying HTTP request, traceId={}, retry={}/{}",
                        context.getTraceId(), retryCount, maxRetry);
                sleep(calculateBackoff(retryCount));
            }

            result = httpClientService.execute(context);

            if (result.isSuccess() && isSuccessStatusCode(result.getStatusCode())) {
                return result;
            }

            retryCount++;
        } while (retryCount <= maxRetry && shouldRetry(result));

        return result;
    }

    private boolean shouldRetry(HttpResponseResult result) {
        if (!result.isSuccess()) {
            return true;
        }
        int statusCode = result.getStatusCode();
        return statusCode >= 500 || statusCode == 429;
    }

    private boolean isSuccessStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private long calculateBackoff(int retryCount) {
        return (long) Math.min(1000 * Math.pow(2, retryCount - 1), 30000);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
