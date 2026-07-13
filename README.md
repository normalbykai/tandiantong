# 摊点通

摊点通是面向摊点、线下零售和到店服务商户的多租户经营系统。当前开发版本为 **V1.1**，采用 Maven 多模块的模块化单体架构，包含商户与平台管理后台、顾客微信小程序以及统一 Java 后端。

V1.1 已完成持久层与认证体系重构：数据库访问统一使用 MyBatis-Plus，登录态与权限域由 Sa-Token 管理，接口文档使用 Knife4j，数据库实体使用普通 Java 类和 Lombok。V1 已冻结，仅作为历史资料保留。

## 技术架构

| 范围 | 技术选型 |
| --- | --- |
| Java 后端 | Java 21、Spring Boot 3.3.7、Maven 多模块 |
| 持久层 | MyBatis-Plus 3.5.9、MySQL 8、Flyway 10.20.1 |
| 认证与授权 | Sa-Token 1.39.0、独立的平台与租户权限域 |
| 接口文档 | Knife4j 4.5.0、开放接口规范 |
| 实体与模型 | 数据库实体使用普通类与 Lombok；请求、响应和值对象可以使用 `record` |
| 缓存与并发辅助 | Redis 7 |
| 管理后台 | Vue 3、TypeScript、Vite、Element Plus |
| 微信小程序 | uni-app、Vue 3、TypeScript |
| 本地基础设施 | Docker Compose |

后端遵循清晰的分层边界：

- 控制层负责协议适配、参数校验、认证入口和响应转换，不编写业务规则。
- 应用层负责业务编排、事务、幂等、状态流转和跨模块应用服务协作，不编写 SQL。
- 领域层保存不依赖数据库的业务状态、值对象和规则。
- 实体层使用普通 Java 类映射数据库表，字段提供中文含义。
- 持久层通过 MyBatis-Plus Mapper 完成查询、聚合、行锁和原子条件更新。
- 第三方能力集中在集成模块，业务模块不直接依赖第三方 SDK。

## 模块结构

```text
tandiantong-bootstrap/       应用启动、框架配置和 Flyway 迁移
tandiantong-common/          统一响应、异常、错误码和请求追踪
tandiantong-security/        登录、用户、租户上下文、RBAC 和商户开通
tandiantong-catalog/         商品、SKU、加料、库存和库存流水
tandiantong-order/           商品订单、支付、退款和业务幂等
tandiantong-reservation/     服务项目、时段、预约和容量控制
tandiantong-verification/    取餐号、核销凭证和核销记录
tandiantong-analytics/       经营指标、Excel 导出和导出审计
tandiantong-integration/     微信支付等第三方服务适配
tandiantong-admin-api/       平台与租户管理后台 API
tandiantong-mini-api/        顾客小程序 API 与支付回调
tandiantong-admin-web/       Vue 3 管理后台
tandiantong-mini-app/        uni-app 微信小程序
docs/versions/v1.1/          当前设计、开发路线和验收基线
```

业务模块之间必须通过应用服务或明确接口协作，不得跨模块访问 Mapper。所有租户业务访问以 MyBatis-Plus 多租户拦截器作为基础隔离，订单、退款、预约、核销和权限等敏感操作还必须显式校验租户与门店归属。

## 当前状态

V1.1 已完成：

- 移除业务代码中的 `JdbcTemplate`，数据库访问统一迁移到 MyBatis-Plus Mapper。
- 将自研 JWT 过滤器和令牌服务替换为 Sa-Token 登录态与权限域认证。
- 建立普通类数据库实体，使用 Lombok 减少访问器和构造代码。
- 完成商品、库存、订单、退款、预约、容量控制、核销与经营数据持久化改造。
- 对库存锁定、支付确认、退款回补、预约容量和核销状态使用原子条件更新。
- 为管理后台与小程序接口补充 Knife4j 中文接口说明。
- 通过 Flyway 管理数据库迁移，并补充数据库表中文含义。
- 使用 Java 21 完成后端全量编译和自动化测试。

后续重点包括：

- 从数据库角色关系加载权限码，补强接口级授权与关键操作审计。
- 完成待支付订单超时释放、退款失败重试和顾客订单闭环。
- 完成付费预约、预约超时释放、预约核销和顾客预约闭环。
- 接入真实微信登录、微信支付验签、退款查询和失败重试。
- 完成双租户、并发、幂等、异常恢复和部署恢复验收。

当前微信支付适配器仅用于本地开发，不代表真实微信支付已经接入。详细范围参见 [后续开发路线](docs/versions/v1.1/总览/后续开发路线.md)。

## 环境要求

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker Desktop 或兼容的 Docker Compose 环境

确认 Java 环境：

```powershell
java -version
mvn -version
```

系统默认 Java 不是 21 时，需要先设置 `JAVA_HOME`：

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## 本地启动

### 1. 启动 MySQL 和 Redis

```powershell
docker compose up -d mysql redis
docker compose ps
```

本地默认连接：

- MySQL：`localhost:3306/tandiantong`
- MySQL 用户名：`tandiantong`
- MySQL 密码：`tandiantong_local_password`
- Redis：`localhost:6379`

这些默认值只适用于本地开发。生产环境必须通过环境变量或部署密钥注入配置，禁止使用示例密码。

### 2. 启动后端

```powershell
mvn -pl tandiantong-bootstrap -am spring-boot:run
```

后端默认监听 `8080` 端口，启动时 Flyway 会自动执行数据库迁移。

- Knife4j 调试页面：`http://localhost:8080/doc.html`
- 认证请求头：`Authorization: Bearer <token>`
- 小程序接口：`/api/mini/v1/**`
- 租户后台接口：`/api/admin/v1/**`
- 平台后台接口：`/api/platform/v1/**`
- 第三方回调：`/api/callback/**`

### 3. 启动管理后台

```powershell
cd tandiantong-admin-web
npm install
npm run dev
```

Vite 会在终端输出本地访问地址。管理后台已有部分页面连接真实接口，其余页面仍需按照 V1.1 路线逐步联调和验收。

### 4. 检查微信小程序

```powershell
cd tandiantong-mini-app
npm install
npm run typecheck
```

微信开发者工具需要配置开发者自己的小程序 AppID。仓库不会保存真实 AppID、支付密钥、证书或其他生产凭据。

## 验证命令

```powershell
# 后端编译与测试
mvn -q -DskipTests compile
mvn -q test

# 管理后台类型检查与构建
cd tandiantong-admin-web
npm run typecheck
npm run build

# 小程序类型检查
cd ../tandiantong-mini-app
npm run typecheck

# 返回仓库根目录检查差异格式
cd ..
git diff --check
```

## 文档入口

- [文档中心](docs/README.md)
- [V1.1 文档索引](docs/versions/v1.1/文档索引.md)
- [V1.1 总体架构](docs/versions/v1.1/总览/总体架构.md)
- [V1.1 重构基线](docs/versions/v1.1/总览/重构基线.md)
- [V1.1 重构差异总结](docs/versions/v1.1/总览/重构差异总结.md)
- [V1.1 后续开发路线](docs/versions/v1.1/总览/后续开发路线.md)
- [V1.1 测试与验收基线](docs/versions/v1.1/质量保障/测试与验收基线.md)

## 安全基线

- 金额统一使用整数分，禁止使用浮点数表示交易金额。
- 租户和门店范围必须来自服务端可信上下文，禁止信任客户端直接提交的租户标识。
- 创建订单、支付回调、退款、预约和核销必须保证业务幂等。
- 库存、预约容量和核销必须使用带当前状态条件的原子更新。
- 密码、验证码、完整令牌、证书、支付密钥和生产数据库凭据不得写入日志或提交到仓库。
- 前端菜单和按钮权限只用于改善体验，所有敏感操作必须在后端再次鉴权。
