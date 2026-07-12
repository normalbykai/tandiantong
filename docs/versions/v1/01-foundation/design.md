# 01 工程基础阶段设计

## 1. 阶段目标

本阶段建立摊点通 V1 的工程底座，使后续多租户、商品、订单、预约和核销能力都能在统一的模块、配置、响应、异常、日志、迁移和测试规范上开发。

## 2. 范围

- 创建 Java 21、Spring Boot 3、Maven 多模块后端工程。
- 创建统一启动模块 `tandiantong-bootstrap`。
- 创建稳定通用模块 `tandiantong-common`，只包含统一响应、错误码、业务异常、分页、追踪号和基础工具。
- 预留已确认的业务模块目录和 Maven 模块，不在本阶段实现业务表或业务规则。
- 接入 Flyway 迁移目录、MySQL 8 和 Redis 配置骨架。
- 建立后端单元测试、Spring Boot 最小启动测试和 Maven 静态检查命令。
- 创建 Docker Compose 本地依赖配置，便于后续阶段联调 MySQL 与 Redis。

## 3. 不做事项

- 不创建具体租户、商品、订单、预约、支付或核销业务表。
- 不实现登录、RBAC、租户拦截器或平台开通流程。
- 不引入微服务、消息队列、搜索引擎、注册中心、配置中心或 Kubernetes。
- 不提前实现未确认的多门店、套餐、人员排班、动态数据权限或平台分账能力。

## 4. 模块边界

后端根工程聚合以下模块：

```text
tandiantong-bootstrap
tandiantong-common
tandiantong-security
tandiantong-catalog
tandiantong-order
tandiantong-reservation
tandiantong-verification
tandiantong-analytics
tandiantong-integration
tandiantong-admin-api
tandiantong-mini-api
```

`tandiantong-common` 不依赖业务模块。业务模块可以依赖 `tandiantong-common`，API 模块可以依赖对应业务模块。`tandiantong-bootstrap` 负责最终装配和启动。

## 5. 基础响应与异常

所有 API 返回统一结构：

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "处理成功",
  "traceId": "追踪号",
  "data": {}
}
```

业务异常使用稳定错误码和中文可读消息。未知异常对外统一返回中文安全提示，禁止暴露堆栈、SQL、密钥或内部实现细节。

## 6. 请求追踪号

系统优先使用请求头 `X-Trace-Id`，缺失时服务端生成。追踪号写入响应头、统一响应体和日志上下文。日志说明使用中文描述业务事件。

## 7. 配置与迁移

本阶段提供本地开发配置：

- MySQL 8 使用环境变量注入地址、账号和密码，示例值只使用本地占位。
- Redis 使用环境变量注入地址和端口。
- Flyway 目录为 `tandiantong-bootstrap/src/main/resources/db/migration`。
- 第一条迁移只创建 Flyway 可验证的基础标记表，不创建业务表。

## 8. 测试策略

- `tandiantong-common` 单元测试覆盖统一响应、业务异常和追踪号生成。
- `tandiantong-bootstrap` 启动测试覆盖 Spring 容器最小装配。
- Maven 构建必须执行单元测试并检查 Java 版本配置。

## 9. 验收标准

- Maven 多模块结构清晰，模块依赖方向符合架构设计。
- 应用可在本地配置下完成最小启动。
- Flyway 迁移目录存在，并可被 Spring Boot 配置识别。
- 统一异常不会向客户端暴露内部细节。
- 阶段相关测试和 `git diff --check` 通过，或明确记录环境限制。
