# 摊点通 V1 本地启动说明

## 1. 项目结构

摊点通 V1 使用模块化单体后端、统一 B 端后台和 C 端 uni-app 小程序工程。

```text
tandiantong-*/                 Java 21、Spring Boot 3、Maven 多模块后端
tandiantong-admin-web/         Vue 3、TypeScript、Vite、Element Plus 后台
tandiantong-mini-app/          uni-app、TypeScript 微信小程序端
docs/versions/v1/              V1 设计、计划、原型和验收文档
docker-compose.yml             本地 MySQL 8 与 Redis
```

## 2. 本地依赖

- Java 21。
- Maven 3.9 或以上。
- Node.js 20 或以上。
- Docker Desktop，用于启动 MySQL 8 和 Redis。

## 3. 启动基础设施

```powershell
docker compose up -d mysql redis
```

默认本地账号仅用于开发：

- MySQL：数据库 `tandiantong`，账号 `tandiantong`，密码 `tandiantong_local_password`。
- Redis：本地 `6379`，默认无密码。

生产环境必须通过环境变量覆盖所有密码和连接地址，禁止使用默认值。

## 4. 启动后端

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-21"
mvn -pl tandiantong-bootstrap -am spring-boot:run
```

后端默认端口为 `8080`。Flyway 会自动执行 `tandiantong-bootstrap/src/main/resources/db/migration` 下的迁移脚本。

## 5. 启动 B 端后台

```powershell
cd tandiantong-admin-web
npm install
npm run dev
```

后台页面默认由 Vite 提供本地访问地址。当前原型数据为本地静态演示数据，接口联调时再接入 `/api/platform/v1/**` 与 `/api/admin/v1/**`。

## 6. C 端小程序

```powershell
cd tandiantong-mini-app
npm install
npm run typecheck
```

首期正式验收平台为微信小程序。导入微信开发者工具前，需要配置真实小程序 AppID；当前仓库不提交真实 AppID 和密钥。

## 7. 常用验证命令

```powershell
mvn test
cd tandiantong-admin-web; npm audit --audit-level=moderate; npm run typecheck; npm run build
cd tandiantong-mini-app; npm audit --audit-level=moderate; npm run typecheck
git diff --check
```
