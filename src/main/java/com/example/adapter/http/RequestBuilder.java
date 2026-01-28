package com.example.adapter.http;

import com.example.adapter.common.constant.ParamType;
import com.example.adapter.common.constant.ValueSource;
import com.example.adapter.common.exception.BusinessException;
import com.example.adapter.common.exception.ErrorCode;
import com.example.adapter.dto.AdapterMessage;
import com.example.adapter.dto.HttpRequestContext;
import com.example.adapter.entity.ApiParamConfig;
import com.example.adapter.entity.ExternalApiConfig;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RequestBuilder {

    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{(\\w+)}");

    public HttpRequestContext build(ExternalApiConfig config, List<ApiParamConfig> paramConfigs, AdapterMessage message) {
        HttpRequestContext context = new HttpRequestContext();
        context.setUrl(config.getApiUrl());
        context.setMethod(config.getHttpMethod());
        context.setContentType(config.getContentType());
        context.setTimeout(config.getTimeout());
        context.setTraceId(message.getTraceId());
        context.setTenantCode(message.getTenantCode());
        context.setApiCode(message.getApiCode());

        for (ApiParamConfig paramConfig : paramConfigs) {
            String value = resolveValue(paramConfig, message);

            if (paramConfig.getRequired() == 1 && !StringUtils.hasText(value)) {
                throw new BusinessException(ErrorCode.REQUIRED_PARAM_MISSING,
                        "Required param missing: " + paramConfig.getParamKey());
            }

            if (value == null) {
                continue;
            }

            switch (paramConfig.getParamType()) {
                case ParamType.HEADER:
                    context.getHeaders().put(paramConfig.getParamKey(), value);
                    break;
                case ParamType.QUERY:
                    context.getQueryParams().put(paramConfig.getParamKey(), value);
                    break;
                case ParamType.BODY:
                    context.getBody().put(paramConfig.getParamKey(), parseValue(value));
                    break;
                case ParamType.PATH:
                    context.setUrl(replacePath(context.getUrl(), paramConfig.getParamKey(), value));
                    break;
                default:
                    log.warn("Unknown param type: {}", paramConfig.getParamType());
            }
        }

        return context;
    }

    private String resolveValue(ApiParamConfig paramConfig, AdapterMessage message) {
        String source = paramConfig.getValueSource();
        if (!StringUtils.hasText(source)) {
            source = ValueSource.FIXED;
        }

        switch (source) {
            case ValueSource.FIXED:
                return paramConfig.getParamValue();

            case ValueSource.MESSAGE:
                return extractFromMessage(paramConfig.getValueExpression(), message);

            case ValueSource.CONTEXT:
                return extractFromContext(paramConfig.getValueExpression(), message);

            default:
                log.warn("Unknown value source: {}", source);
                return paramConfig.getParamValue();
        }
    }

    private String extractFromMessage(String expression, AdapterMessage message) {
        if (!StringUtils.hasText(expression) || message.getMessageBody() == null) {
            return null;
        }

        try {
            Object value = JsonPath.read(message.getMessageBody(), expression);
            return value != null ? String.valueOf(value) : null;
        } catch (PathNotFoundException e) {
            log.debug("Path not found in message: {}", expression);
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract value from message, expression={}", expression, e);
            return null;
        }
    }

    private String extractFromContext(String expression, AdapterMessage message) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }

        switch (expression) {
            case "traceId":
                return message.getTraceId();
            case "tenantCode":
                return message.getTenantCode();
            case "apiCode":
                return message.getApiCode();
            case "userId":
                return message.getUserInfo() != null ? message.getUserInfo().getUserId() : null;
            case "userName":
                return message.getUserInfo() != null ? message.getUserInfo().getUserName() : null;
            default:
                if (expression.startsWith("userInfo.extra.") && message.getUserInfo() != null) {
                    String key = expression.substring("userInfo.extra.".length());
                    Map<String, String> extra = message.getUserInfo().getExtra();
                    return extra != null ? extra.get(key) : null;
                }
                return null;
        }
    }

    private String replacePath(String url, String paramKey, String value) {
        return url.replace("{" + paramKey + "}", value);
    }

    private Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
