# 01 工程基础验收

## 1. 验收范围

- Maven 多模块后端工程。
- Spring Boot 启动模块。
- 统一响应、错误码、业务异常和请求追踪号。
- Flyway 迁移目录和基础迁移。
- MySQL、Redis 和 Docker Compose 本地开发配置。
- 最小单元测试和启动测试。

## 2. 验收标准

| 项目 | 标准 | 结果 |
| --- | --- | --- |
| 模块结构 | 模块名称与 V1 架构一致，公共模块不包含业务模型 | 通过 |
| Java 版本 | 工程配置 Java 21 | 通过 |
| 统一响应 | 成功和失败响应包含 `success`、`code`、`message`、`traceId`、`data` | 通过 |
| 异常处理 | 业务异常返回稳定错误码和中文消息，未知异常不暴露内部细节 | 通过 |
| 追踪号 | 请求追踪号可生成、传递到响应头和响应体 | 通过 |
| Flyway | 迁移目录存在，基础迁移可被识别 | 通过 |
| 敏感配置 | 示例配置不包含真实凭据 | 通过 |
| 测试 | 阶段相关测试通过 | 通过 |

## 3. 验证记录

| 时间 | 命令 | 结果 | 说明 |
| --- | --- | --- | --- |
| 2026-07-12 | `mvn -pl tandiantong-common test` | 通过 | 使用 worktree 本地 Temurin JDK 21，6 个测试通过 |
| 2026-07-12 | `mvn -pl tandiantong-bootstrap -am test` | 通过 | 使用 worktree 本地 Temurin JDK 21，9 个测试通过 |
| 2026-07-12 | `mvn test` | 通过 | 使用 worktree 本地 Temurin JDK 21，9 个测试通过 |
| 2026-07-12 | `git diff --check` | 通过 | 无空白错误；Git 提示 `.gitignore` 后续可能按 Windows CRLF 处理 |

## 4. 已知限制

- 当前阶段不实现任何业务表、认证、租户上下文或业务接口。
- 本机全局 `JAVA_HOME` 是 JDK 17，因此阶段验证使用 `.local-tools/` 下临时 Temurin JDK 21；该目录已加入 `.gitignore`，不提交运行时文件。
