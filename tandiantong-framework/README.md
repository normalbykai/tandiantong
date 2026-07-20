# 摊点通框架组件

`tandiantong-framework` 用于沉淀稳定、可复用、与摊点通具体业务模型解耦的基础能力。业务模块可以依赖这里的 starter，但 framework 模块不得反向依赖商品、订单、预约、核销、商户等业务模块。

## 模块职责

| 模块 | 职责 |
| --- | --- |
| `tandiantong-framework-common` | 统一响应、错误码、业务异常和追踪号等基础类型。 |
| `tandiantong-spring-boot-starter-web` | Web 自动配置、追踪号过滤器、请求上下文清理、统一响应包装和全局异常处理。 |
| `tandiantong-spring-boot-starter-biz-ip` | HTTP 客户端真实 IP 解析。 |
| `tandiantong-spring-boot-starter-biz-tenant` | 当前线程可信租户上下文。 |
| `tandiantong-spring-boot-starter-security` | 登录用户上下文和权限域等安全基础类型。 |
| `tandiantong-spring-boot-starter-operate-log` | 操作日志命令、记录接口和请求上下文补全。 |
| `tandiantong-spring-boot-starter-mybatis` | MyBatis-Plus 分页和租户隔离自动配置，具体租户表范围由业务侧 `TenantTableProvider` 提供。 |

## 请求上下文清理

`tandiantong-spring-boot-starter-web` 会在请求结束后调用所有 `ThreadLocalContextCleaner`，用于清理 framework 层的线程上下文。tenant 和 security starter 会分别注册租户上下文、登录用户上下文清理器，业务侧仍应清理自己的业务用户上下文。

## MyBatis 租户表范围

`tandiantong-spring-boot-starter-mybatis` 只负责注册 MyBatis-Plus 分页和租户拦截器，不在 framework 中硬编码具体业务表。业务项目必须提供 `TenantTableProvider`，明确哪些表包含 `tenant_id` 并需要自动租户隔离；没有提供时默认忽略全部表。

## 下沉边界

- 可以下沉：统一响应、异常、追踪号、IP 解析、租户上下文、登录用户上下文、MyBatis 通用配置、操作日志基础命令。
- 禁止下沉：商户、门店、角色、权限、订单、支付、退款、核销、预约、商品等业务模型和业务规则。
- 业务模块需要保留明确应用服务边界，framework 只提供基础设施能力，不替代业务审计、RBAC 校验或交易状态机。
