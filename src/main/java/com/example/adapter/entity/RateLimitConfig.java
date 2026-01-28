package com.example.adapter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rate_limit_config")
public class RateLimitConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String apiCode;

    private String limitType;

    private Integer limitValue;

    private Integer timeWindow;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
