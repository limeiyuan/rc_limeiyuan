package com.example.adapter.service;

import com.example.adapter.common.constant.StatusConstant;
import com.example.adapter.dto.HttpRequestContext;
import com.example.adapter.dto.HttpResponseResult;
import com.example.adapter.entity.RequestLog;
import com.example.adapter.repository.RequestLogMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogMapper requestLogMapper;
    private final ObjectMapper objectMapper;

    @Async
    public void logRequest(HttpRequestContext context, HttpResponseResult result) {
        try {
            RequestLog requestLog = new RequestLog();
            requestLog.setTraceId(context.getTraceId());
            requestLog.setTenantCode(context.getTenantCode());
            requestLog.setApiCode(context.getApiCode());
            requestLog.setRequestUrl(context.getUrl());
            requestLog.setRequestMethod(context.getMethod());
            requestLog.setRequestHeaders(toJson(context.getHeaders()));
            requestLog.setRequestBody(toJson(context.getBody()));
            requestLog.setCostTime(result.getCostTime());

            if (result.isSuccess()) {
                requestLog.setStatus(StatusConstant.LOG_SUCCESS);
                requestLog.setResponseCode(result.getStatusCode());
                requestLog.setResponseBody(result.getBody());
            } else {
                requestLog.setStatus(StatusConstant.LOG_FAILED);
                requestLog.setErrorMsg(result.getErrorMessage());
            }

            requestLogMapper.insert(requestLog);
        } catch (Exception e) {
            log.error("Failed to save request log, traceId={}", context.getTraceId(), e);
        }
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to json", e);
            return obj.toString();
        }
    }
}
