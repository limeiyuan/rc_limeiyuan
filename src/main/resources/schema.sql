-- 创建数据库
CREATE DATABASE IF NOT EXISTS http_adapter DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE http_adapter;

-- 租户表
CREATE TABLE IF NOT EXISTS tenant (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    tenant_code VARCHAR(64) NOT NULL COMMENT '租户识别码',
    tenant_name VARCHAR(128) NOT NULL COMMENT '租户名称',
    description VARCHAR(512) COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 外部接口配置表
CREATE TABLE IF NOT EXISTS external_api_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    api_code VARCHAR(64) NOT NULL COMMENT '接口编码',
    api_name VARCHAR(128) NOT NULL COMMENT '接口名称',
    api_url VARCHAR(512) NOT NULL COMMENT '接口地址',
    http_method VARCHAR(16) NOT NULL DEFAULT 'POST' COMMENT '请求方法：GET/POST/PUT/DELETE',
    content_type VARCHAR(64) DEFAULT 'application/json' COMMENT '内容类型',
    timeout INT NOT NULL DEFAULT 30000 COMMENT '超时时间（毫秒）',
    retry_count INT NOT NULL DEFAULT 3 COMMENT '重试次数',
    description VARCHAR(512) COMMENT '接口描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_api_code (tenant_id, api_code),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部接口配置表';

-- 接口参数配置表
CREATE TABLE IF NOT EXISTS api_param_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_id BIGINT NOT NULL COMMENT '接口配置ID',
    param_type VARCHAR(32) NOT NULL COMMENT '参数类型：HEADER/BODY/QUERY/PATH',
    param_key VARCHAR(128) NOT NULL COMMENT '参数键名',
    param_value VARCHAR(1024) COMMENT '参数默认值',
    value_source VARCHAR(32) NOT NULL DEFAULT 'FIXED' COMMENT '值来源：FIXED-固定值/MESSAGE-消息体取值/CONTEXT-上下文',
    value_expression VARCHAR(256) COMMENT '取值表达式（如JSONPath）',
    required TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填：0-否，1-是',
    description VARCHAR(256) COMMENT '参数说明',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_config_id (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口参数配置表';

-- 限流配置表
CREATE TABLE IF NOT EXISTS rate_limit_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    api_code VARCHAR(64) COMMENT '接口编码（空表示租户级别限流）',
    limit_type VARCHAR(32) NOT NULL DEFAULT 'QPS' COMMENT '限流类型：QPS/CONCURRENT',
    limit_value INT NOT NULL DEFAULT 100 COMMENT '限流值',
    time_window INT NOT NULL DEFAULT 1 COMMENT '时间窗口（秒）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_api (tenant_id, api_code),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限流配置表';

-- 请求日志表
CREATE TABLE IF NOT EXISTS request_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    trace_id VARCHAR(64) NOT NULL COMMENT '链路追踪ID',
    tenant_code VARCHAR(64) NOT NULL COMMENT '租户识别码',
    api_code VARCHAR(64) NOT NULL COMMENT '接口编码',
    request_url VARCHAR(512) COMMENT '请求URL',
    request_method VARCHAR(16) COMMENT '请求方法',
    request_headers TEXT COMMENT '请求头',
    request_body TEXT COMMENT '请求体',
    response_code INT COMMENT '响应状态码',
    response_body TEXT COMMENT '响应体',
    cost_time BIGINT COMMENT '耗时（毫秒）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-处理中，1-成功，2-失败',
    error_msg VARCHAR(1024) COMMENT '错误信息',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_trace_id (trace_id),
    KEY idx_tenant_api (tenant_code, api_code),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求日志表';
