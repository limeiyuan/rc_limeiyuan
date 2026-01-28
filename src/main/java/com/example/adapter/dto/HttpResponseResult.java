package com.example.adapter.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HttpResponseResult {

    private boolean success;

    private int statusCode;

    private Map<String, String> headers;

    private String body;

    private long costTime;

    private String errorMessage;

    public static HttpResponseResult success(int statusCode, String body, long costTime) {
        HttpResponseResult result = new HttpResponseResult();
        result.setSuccess(true);
        result.setStatusCode(statusCode);
        result.setBody(body);
        result.setCostTime(costTime);
        return result;
    }

    public static HttpResponseResult fail(String errorMessage, long costTime) {
        HttpResponseResult result = new HttpResponseResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setCostTime(costTime);
        return result;
    }
}
