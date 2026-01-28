package com.example.adapter.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AdapterMessage {

    private String tenantCode;

    private String apiCode;

    private String traceId;

    private Map<String, Object> messageBody;

    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private String userId;
        private String userName;
        private Map<String, String> extra;
    }
}
