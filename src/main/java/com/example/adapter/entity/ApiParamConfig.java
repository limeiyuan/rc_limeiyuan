package com.example.adapter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("api_param_config")
public class ApiParamConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long configId;

    private String paramType;

    private String paramKey;

    private String paramValue;

    private String valueSource;

    private String valueExpression;

    private Integer required;

    private String description;

    private Integer sortOrder;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
