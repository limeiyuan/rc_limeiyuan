# 项目开发功能点清单

## 一、基础架构

### 1.1 项目初始化
- [x] Spring Boot项目搭建
- [x] Maven/Gradle依赖管理
- [x] 配置文件管理（application.yml）
- [x] 日志框架配置（logback）

### 1.2 数据库设计
- [x] MySQL数据库连接配置
- [x] MyBatis/JPA集成
- [x] 数据库连接池配置（HikariCP）

---

## 二、租户系统

### 2.1 租户管理
- [x] 租户表设计（tenant）
  - tenant_id：租户ID
  - tenant_code：租户识别码（唯一）
  - tenant_name：租户名称
  - status：状态（启用/禁用）
  - create_time：创建时间
  - update_time：更新时间
- [x] 租户CRUD接口
- [x] 租户识别码校验逻辑

### 2.2 租户上下文
- [ ] 租户上下文ThreadLocal管理
- [ ] 请求拦截器自动解析租户信息
- [x] 租户数据隔离

---

## 三、外部接口配置管理

### 3.1 外部接口配置表设计
- [x] 接口配置主表（external_api_config）
  - config_id：配置ID
  - tenant_id：所属租户ID
  - api_code：接口编码（租户内唯一）
  - api_name：接口名称
  - api_url：接口地址
  - http_method：请求方法（GET/POST/PUT/DELETE）
  - content_type：内容类型
  - timeout：超时时间（毫秒）
  - retry_count：重试次数
  - status：状态
  - create_time/update_time

### 3.2 接口参数配置表设计
- [x] 参数配置表（api_param_config）
  - param_id：参数ID
  - config_id：关联接口配置ID
  - param_type：参数类型（HEADER/BODY/QUERY_PARAM/PATH_PARAM）
  - param_key：参数键名
  - param_value：参数默认值
  - value_source：值来源（FIXED固定值/MESSAGE消息体取值/CONTEXT上下文）
  - value_expression：取值表达式（如JSONPath）
  - required：是否必填
  - description：参数说明

### 3.3 接口配置管理API
- [x] 接口配置CRUD
- [x] 参数配置CRUD
- [ ] 配置导入/导出
- [ ] 配置校验

---

## 四、Kafka消息监听

### 4.1 Kafka基础配置
- [x] Kafka连接配置
- [x] Consumer配置（group-id、auto-offset-reset等）
- [x] 消息序列化/反序列化配置

### 4.2 消息监听器
- [x] 消息监听Topic配置
- [x] 消息体结构定义
  ```json
  {
    "tenantCode": "租户识别码",
    "apiCode": "接口编码",
    "traceId": "链路追踪ID",
    "messageBody": {
      "业务数据字段": "值"
    },
    "userInfo": {
      "userId": "用户ID",
      "userName": "用户名"
    }
  }
  ```
- [x] 消息解析与校验
- [x] 消息处理异常处理

### 4.3 消息重试机制
- [x] 重试策略配置（指数退避）
- [x] 死信队列处理
- [x] 重试次数记录

---

## 五、HTTP请求组装与发送

### 5.1 HTTP客户端
- [x] RestTemplate/WebClient/OkHttp封装
- [x] 连接池配置
- [x] 超时配置

### 5.2 请求组装器
- [x] Header参数组装
- [x] Body参数组装（JSON/Form）
- [x] Query参数组装
- [x] Path参数替换
- [x] 动态参数取值（从消息体/上下文获取）

### 5.3 请求发送
- [x] 同步请求发送
- [x] 响应处理
- [x] 异常处理（网络超时、连接失败等）
- [x] 请求日志记录

---

## 六、限流功能

### 6.1 限流配置
- [x] 限流配置表（rate_limit_config）
  - tenant_id：租户ID
  - api_code：接口编码（可为空表示租户级别）
  - limit_type：限流类型（QPS/并发数）
  - limit_value：限流值
  - time_window：时间窗口（秒）

### 6.2 限流实现
- [x] 限流算法实现（滑动窗口）
- [x] Redis集成（分布式限流）
- [x] 限流拦截器
- [x] 限流响应处理

---

## 七、管理系统API

### 7.1 租户管理接口
- [x] POST /api/admin/tenants - 创建租户
- [x] GET /api/admin/tenants - 租户列表
- [x] GET /api/admin/tenants/{id} - 租户详情
- [x] PUT /api/admin/tenants/{id} - 更新租户
- [x] DELETE /api/admin/tenants/{id} - 删除租户

### 7.2 接口配置管理接口
- [x] POST /api/admin/configs - 创建接口配置
- [x] GET /api/admin/configs - 接口配置列表
- [x] GET /api/admin/configs/{id} - 接口配置详情
- [x] PUT /api/admin/configs/{id} - 更新接口配置
- [x] DELETE /api/admin/configs/{id} - 删除接口配置

### 7.3 限流配置管理接口
- [x] POST /api/admin/rate-limits - 创建限流配置
- [x] GET /api/admin/rate-limits - 限流配置列表
- [x] PUT /api/admin/rate-limits/{id} - 更新限流配置
- [x] DELETE /api/admin/rate-limits/{id} - 删除限流配置

---

## 八、监控与埋点

### 8.1 日志记录
- [x] 请求日志记录
- [x] 异常日志记录
- [x] 链路追踪ID透传

### 8.2 埋点设计
- [x] 请求成功/失败埋点
- [x] 限流触发埋点
- [x] 重试次数埋点
- [x] 响应时间埋点

### 8.3 监控指标（预留）
- [ ] Metrics接口预留
- [x] 健康检查接口

---

## 九、异常处理

### 9.1 异常定义
- [x] 业务异常类定义
- [x] 全局异常处理器
- [x] 统一响应格式

### 9.2 异常场景处理
- [x] 租户不存在/已禁用
- [x] 接口配置不存在
- [x] 参数校验失败
- [x] 外部接口调用超时
- [x] 外部接口调用失败
- [x] 限流触发

---

## 十、安全性

### 10.1 接口安全
- [ ] 管理接口鉴权
- [x] 接口参数校验
- [x] SQL注入防护

### 10.2 敏感信息处理
- [ ] 敏感参数脱敏（日志中）
- [ ] 配置加密存储（可选）

---

## 开发优先级

### P0 - 核心功能（必须）✅ 已完成
1. 租户系统基础
2. 外部接口配置管理
3. Kafka消息监听
4. HTTP请求组装与发送

### P1 - 重要功能 ✅ 已完成
1. 限流功能
2. 管理系统API
3. 异常处理

### P2 - 增强功能（部分完成）
1. 监控与埋点 ✅
2. 安全性增强 ⏳

---

## 技术栈

| 组件 | 技术选型 |
|------|----------|
| 框架 | Spring Boot 2.7.18 |
| 数据库 | MySQL 8.0 |
| ORM | MyBatis-Plus 3.5.5 |
| 消息队列 | Kafka |
| 缓存/限流 | Redis |
| HTTP客户端 | OkHttp 4.12.0 |
| JSON处理 | Jackson |
| 参数校验 | Hibernate Validator |
| 文档 | SpringDoc OpenAPI |

---

## 项目结构

```
src/main/java/com/example/adapter/
├── HttpAdapterApplication.java          # 启动类
├── common/
│   ├── constant/                         # 常量定义
│   ├── exception/                        # 异常定义
│   └── response/                         # 统一响应
├── config/                               # 配置类
├── controller/                           # 管理API
├── dto/                                  # 数据传输对象
├── entity/                               # 实体类
├── http/                                 # HTTP客户端
├── kafka/                                # Kafka监听器
├── ratelimit/                            # 限流服务
├── repository/                           # 数据访问层
└── service/                              # 业务服务层

src/main/resources/
├── application.yml                       # 配置文件
├── schema.sql                            # 数据库脚本
└── mapper/                               # MyBatis XML
```

---

## 待完成功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 租户上下文ThreadLocal | P2 | 可选增强 |
| 配置导入/导出 | P2 | 批量管理 |
| 管理接口鉴权 | P1 | 安全增强 |
| 敏感参数脱敏 | P2 | 日志安全 |
| Metrics接口 | P2 | 监控增强 |

---

## 已实现的增强功能

| 功能 | 说明 |
|------|------|
| 死信队列 | 重试超过阈值后发送到DLQ，Redis记录重试次数 |
| 配置缓存 | Guava Cache缓存租户和接口配置，减少DB查询 |
| 优雅停机 | 应用关闭时等待消息处理完成 |
