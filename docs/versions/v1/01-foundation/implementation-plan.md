# 01 工程基础实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立摊点通 V1 后端工程基础，使后续阶段可以在统一模块、响应、异常、追踪、迁移和测试命令上继续开发。

**Architecture:** 使用 Maven 聚合工程和 Spring Boot 模块化单体。公共基础能力集中在 `tandiantong-common`，启动与配置集中在 `tandiantong-bootstrap`，其他业务模块仅创建已确认边界下的可编译模块。

**Tech Stack:** Java 21、Spring Boot 3、Maven、JUnit 5、Flyway、MySQL 8、Redis、Docker Compose

---

### Task 1: 阶段文档

**Files:**
- Create: `docs/versions/v1/01-foundation/design.md`
- Create: `docs/versions/v1/01-foundation/implementation-plan.md`
- Create: `docs/versions/v1/01-foundation/acceptance.md`

- [x] **Step 1: 编写阶段设计**

记录本阶段目标、范围、模块边界、基础响应、追踪号、配置迁移和测试策略。

- [x] **Step 2: 编写实施计划**

将工程基础拆分为文档、Maven 骨架、公共能力、启动配置、Docker 配置和验证提交。

### Task 2: Maven 多模块骨架

**Files:**
- Create: `pom.xml`
- Create: `tandiantong-*/pom.xml`

- [ ] **Step 1: 创建父工程和模块 POM**

父工程固定 Java 21、Spring Boot 3 和依赖版本，子模块只声明必要依赖。

- [ ] **Step 2: 运行 Maven 验证**

Run: `mvn -q -DskipTests validate`

Expected: 在 Java 21 环境下退出码为 0。

### Task 3: 公共响应、异常和追踪号

**Files:**
- Create: `tandiantong-common/src/test/java/com/tandiantong/common/api/ApiResponseTest.java`
- Create: `tandiantong-common/src/test/java/com/tandiantong/common/trace/TraceIdGeneratorTest.java`
- Create: `tandiantong-common/src/main/java/com/tandiantong/common/api/ApiResponse.java`
- Create: `tandiantong-common/src/main/java/com/tandiantong/common/api/ErrorCode.java`
- Create: `tandiantong-common/src/main/java/com/tandiantong/common/exception/BusinessException.java`
- Create: `tandiantong-common/src/main/java/com/tandiantong/common/trace/TraceIdGenerator.java`

- [ ] **Step 1: 先写公共能力测试**

测试统一响应成功/失败结构、业务异常错误码和追踪号格式。

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl tandiantong-common test`

Expected: 因生产类尚不存在而失败。

- [ ] **Step 3: 实现最小公共能力**

实现统一响应、错误码、业务异常和追踪号生成。

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -pl tandiantong-common test`

Expected: 测试通过。

### Task 4: 启动模块与基础配置

**Files:**
- Create: `tandiantong-bootstrap/src/main/java/com/tandiantong/bootstrap/TandianTongApplication.java`
- Create: `tandiantong-bootstrap/src/main/java/com/tandiantong/bootstrap/web/GlobalExceptionHandler.java`
- Create: `tandiantong-bootstrap/src/main/java/com/tandiantong/bootstrap/web/TraceIdFilter.java`
- Create: `tandiantong-bootstrap/src/main/resources/application.yml`
- Create: `tandiantong-bootstrap/src/main/resources/db/migration/V1_0_0__foundation_baseline.sql`
- Create: `tandiantong-bootstrap/src/test/java/com/tandiantong/bootstrap/TandianTongApplicationTest.java`

- [ ] **Step 1: 先写启动和 Web 基础测试**

测试 Spring 容器可加载，业务异常返回中文错误码，追踪号写入响应头。

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl tandiantong-bootstrap -am test`

Expected: 因启动类和 Web 基础类尚不存在而失败。

- [ ] **Step 3: 实现启动与配置**

实现 Spring Boot 启动类、全局异常处理、追踪号过滤器、Flyway 迁移目录和本地配置。

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -pl tandiantong-bootstrap -am test`

Expected: 测试通过。

### Task 5: 本地开发配置与阶段验收

**Files:**
- Create: `docker-compose.yml`
- Create: `.env.example`
- Modify: `docs/versions/v1/01-foundation/acceptance.md`

- [ ] **Step 1: 创建本地依赖配置**

提供 MySQL 8、Redis 和应用配置占位，不提交真实凭据。

- [ ] **Step 2: 运行阶段验证**

Run:

```powershell
mvn test
git diff --check
```

Expected: 在 Java 21 环境下全部通过；若本机缺少 Java 21，验收文档记录环境限制。

- [ ] **Step 3: 提交功能单元**

Commit: `feat: 建立V1工程基础`

Expected: 提交成功，工作区无未提交工程基础变更。
