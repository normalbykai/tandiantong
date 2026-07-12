# 02 多租户、认证与 RBAC 实施计划

**Goal:** 建立 V1 平台与租户权限域隔离、服务端可信上下文、密码哈希、令牌和 RBAC 权限判定。

**Architecture:** 安全能力集中在 `tandiantong-security`，API 和启动模块只装配过滤器与异常响应。数据库迁移放在 `tandiantong-bootstrap`，后续业务模块通过安全上下文读取当前用户、租户、门店和权限。

**Tech Stack:** Java 21、Spring Security、JJWT、JUnit 5、Flyway、MySQL 8

---

### Task 1: 阶段文档

**Files:**
- Create: `docs/versions/v1/02-tenant-rbac/design.md`
- Create: `docs/versions/v1/02-tenant-rbac/prototype.md`
- Create: `docs/versions/v1/02-tenant-rbac/implementation-plan.md`
- Create: `docs/versions/v1/02-tenant-rbac/acceptance.md`

- [x] **Step 1: 编写设计和原型说明**

明确平台与租户权限域、上下文来源、RBAC 边界和本阶段不做事项。

### Task 2: 安全领域模型与上下文

**Files:**
- Create: `tandiantong-security/src/test/java/com/tandiantong/security/context/SecurityContextHolderTest.java`
- Create: `tandiantong-security/src/main/java/com/tandiantong/security/context/*.java`

- [ ] **Step 1: 先写上下文测试**

验证上下文设置、读取、清理和租户域必填规则。

- [ ] **Step 2: 实现上下文模型**

实现权限域、当前用户、租户上下文和线程隔离。

### Task 3: 密码与令牌

**Files:**
- Create: `tandiantong-security/src/test/java/com/tandiantong/security/auth/PasswordAndTokenServiceTest.java`
- Create: `tandiantong-security/src/main/java/com/tandiantong/security/auth/*.java`

- [ ] **Step 1: 先写密码和令牌测试**

验证密码哈希不是明文、正确密码可验证、错误密码失败、令牌可解析且不能跨权限域。

- [ ] **Step 2: 实现密码和令牌服务**

使用 BCrypt 和 HMAC-SHA JWT，密钥通过配置注入，示例值仅用于本地开发。

### Task 4: RBAC 权限判定与数据库迁移

**Files:**
- Create: `tandiantong-security/src/test/java/com/tandiantong/security/rbac/RbacServiceTest.java`
- Create: `tandiantong-security/src/main/java/com/tandiantong/security/rbac/*.java`
- Create: `tandiantong-bootstrap/src/main/resources/db/migration/V1_1_0__tenant_rbac.sql`

- [ ] **Step 1: 先写 RBAC 测试**

验证两个租户同名角色互不影响、用户只能使用自己拥有的 API 权限。

- [ ] **Step 2: 实现权限判定和迁移**

实现权限模型和安全表结构，所有租户侧唯一索引包含 `tenant_id`。

### Task 5: 阶段验证与提交

- [ ] **Step 1: 运行阶段测试**

Run: `mvn -pl tandiantong-security -am test`

- [ ] **Step 2: 运行全量后端测试和格式检查**

Run:

```powershell
mvn test
git diff --check
```

- [ ] **Step 3: 更新验收并提交**

Commit: `feat: 建立多租户认证与RBAC底座`
