package com.example.adapter.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HttpRequestContext {

    private String url;

    private String method;

    private String contentType;

    private int timeout;

    private Map<String, String> headers = new HashMap<>();

    private Map<String, String> queryParams = new HashMap<>();

    private Map<String, Object> body = new HashMap<>();

    private String traceId;

    private String tenantCode;

    private String apiCode;
}
