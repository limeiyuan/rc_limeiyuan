package com.example.adapter.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "成功"),
    SYSTEM_ERROR(500, "系统错误"),
    PARAM_ERROR(400, "参数错误"),

    TENANT_NOT_FOUND(1001, "租户不存在"),
    TENANT_DISABLED(1002, "租户已禁用"),
    TENANT_CODE_EXISTS(1003, "租户识别码已存在"),

    API_CONFIG_NOT_FOUND(2001, "接口配置不存在"),
    API_CONFIG_DISABLED(2002, "接口配置已禁用"),
    API_CODE_EXISTS(2003, "接口编码已存在"),

    PARAM_CONFIG_NOT_FOUND(3001, "参数配置不存在"),
    REQUIRED_PARAM_MISSING(3002, "必填参数缺失"),

    RATE_LIMIT_EXCEEDED(4001, "请求频率超限"),

    HTTP_REQUEST_FAILED(5001, "HTTP请求失败"),
    HTTP_REQUEST_TIMEOUT(5002, "HTTP请求超时"),

    MESSAGE_PARSE_ERROR(6001, "消息解析失败"),
    MESSAGE_PARAM_ERROR(6002, "消息参数错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
