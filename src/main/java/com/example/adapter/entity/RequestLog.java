package com.example.adapter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("request_log")
public class RequestLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String traceId;

    private String tenantCode;

    private String apiCode;

    private String requestUrl;

    private String requestMethod;

    private String requestHeaders;

    private String requestBody;

    private Integer responseCode;

    private String responseBody;

    private Long costTime;

    private Integer status;

    private String errorMsg;

    private Integer retryCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
