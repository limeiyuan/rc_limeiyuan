package com.example.adapter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("external_api_config")
public class ExternalApiConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String apiCode;

    private String apiName;

    private String apiUrl;

    private String httpMethod;

    private String contentType;

    private Integer timeout;

    private Integer retryCount;

    private String description;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
