# 摊点通

摊点通是面向摊点与小型门店的多租户经营系统。V1 采用模块化单体架构，包含 Java 后端、统一管理后台和微信小程序端，覆盖商户开通、商品库存、订单支付、预约、核销和经营数据等核心场景。

## 技术栈

- 后端：Java 21、Spring Boot 3、Maven 多模块、Spring Security、Flyway。
- 数据：MySQL 8、Redis 7。
- B 端：Vue 3、TypeScript、Vite、Element Plus。
- C 端：uni-app、TypeScript，首期验收平台为微信小程序。
- 本地环境：Docker Compose。

## 目录结构

```text
tandiantong-bootstrap/       应用启动、配置和 Flyway 迁移
tandiantong-common/          通用响应、异常和追踪能力
tandiantong-security/        认证、租户上下文、RBAC 和商户开通
tandiantong-catalog/         商品、SKU、加料和库存
tandiantong-order/           商品订单、支付和退款
tandiantong-reservation/     服务预约和容量控制
tandiantong-verification/    取餐号与核销凭证
tandiantong-analytics/       经营数据和 Excel 导出
tandiantong-integration/     微信支付等第三方适配
tandiantong-admin-api/       平台与租户后台 API
tandiantong-mini-api/        小程序 API 与支付回调
tandiantong-admin-web/       统一管理后台
tandiantong-mini-app/        微信小程序端
docs/                        规范、V1 设计、计划、原型和验收文档
```

## 当前状态

V1 后端主干能力已经实现并通过 Maven 测试，部分流程已连接本地 MySQL 做真实接口验证。前端仍包含原型和部分真实接口联调，不应视为全部页面已经完成验收。

已完成的主要后端能力：

- 平台与租户 JWT 登录、商户开通、管理员邀请激活和商户启用。
- 商品、SKU、初始库存、库存流水和租户范围查询。
- 商品订单创建、服务端计价、库存锁定、支付回调幂等和整单退款。
- 免费预约、容量原子占用和取消释放。
- 商品取餐号、安全核销凭证和原子核销。
- 经营数据聚合、Excel 导出和导出审计。

仍未完成或验证不足的项目，请阅读 [V1 后端开发与测试现状](docs/versions/v1/09-integration-acceptance/后端开发与测试现状.md)。

## 环境要求

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker Desktop

系统默认 Java 不是 21 时，必须先显式设置 `JAVA_HOME`：

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

默认配置仅用于本地开发：

- MySQL：`localhost:3306/tandiantong`
- 用户名：`tandiantong`
- 密码：`tandiantong_local_password`
- Redis：`localhost:6379`

可复制 `.env.example` 并通过环境变量覆盖默认值。生产环境禁止使用仓库中的本地默认密码。

### 2. 启动后端

```powershell
mvn -pl tandiantong-bootstrap -am spring-boot:run
```

后端默认监听 `8080` 端口。启动时 Flyway 自动执行 `tandiantong-bootstrap/src/main/resources/db/migration/` 下的迁移。

API 前缀：

- 小程序：`/api/mini/v1/**`
- 租户后台：`/api/admin/v1/**`
- 平台后台：`/api/platform/v1/**`
- 第三方回调：`/api/callback/**`

### 3. 启动 B 端后台

```powershell
cd tandiantong-admin-web
npm install
npm run dev
```

Vite 会输出本地访问地址。登录和经营数据等页面已接入部分真实接口，其余页面仍需结合现状文档进行人工确认。

### 4. 检查 C 端小程序

```powershell
cd tandiantong-mini-app
npm install
npm run typecheck
```

微信开发者工具需要配置自己的小程序 AppID。仓库不保存真实 AppID、支付密钥或其他生产凭据。

## 验证命令

```powershell
mvn test

cd tandiantong-admin-web
npm audit --audit-level=moderate
npm run typecheck
npm run build

cd ../tandiantong-mini-app
npm audit --audit-level=moderate
npm run typecheck

cd ..
git diff --check
```

## 文档入口

- [文档中心](docs/README.md)
- [V1 文档索引](docs/versions/v1/README.md)
- [总体架构](docs/versions/v1/00-overview/architecture.md)
- [V1 后端开发与测试现状](docs/versions/v1/09-integration-acceptance/后端开发与测试现状.md)
- [外部服务接入说明](docs/versions/v1/09-integration-acceptance/external-services.md)

## 安全说明

- 金额统一使用整数分，禁止使用浮点数表示交易金额。
- 租户和门店范围必须来自服务端可信上下文，禁止信任客户端直接提交的租户标识。
- 本地微信支付适配器仅用于开发和测试，不代表真实微信支付已经接入。
- 密码、令牌、证书、数据库凭据和对象存储密钥不得提交到仓库。
