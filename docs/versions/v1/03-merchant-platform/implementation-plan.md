# 03 统一后台与商户开通实施计划

**Goal:** 交付统一 B 端后台骨架和平台商户开通核心服务。

**Architecture:** 后端商户开通核心放在 `tandiantong-security` 的平台应用服务中，API 适配放在 `tandiantong-admin-api` 后续控制器。前端放在 `tandiantong-admin-web`，使用 Vue 3、TypeScript、Vite 和 Element Plus。

**Tech Stack:** Java 21、JUnit 5、Vue 3、TypeScript、Vite、Element Plus、Vitest

---

### Task 1: 阶段文档

- [x] 编写 `design.md`、`prototype.md`、`implementation-plan.md` 和 `acceptance.md`。

### Task 2: 商户开通服务

- [ ] 先写商户开通测试，覆盖租户、门店、邀请和随机入口码。
- [ ] 实现开通服务和值对象。
- [ ] 运行 `mvn -pl tandiantong-security -am test`。

### Task 3: B 端工程骨架

- [ ] 创建 `tandiantong-admin-web`。
- [ ] 实现统一登录、平台工作台、商户列表、新建商户向导、审核工作台和商户工作台。
- [ ] 实现中文菜单、响应式布局和模拟数据状态。
- [ ] 运行 `npm install`、`npm run typecheck`、`npm run build`。

### Task 4: 阶段验证与提交

- [ ] 运行 `mvn test`、`npm run build` 和 `git diff --check`。
- [ ] 更新 `acceptance.md`。
- [ ] 提交 `feat: 增加统一后台与商户开通能力`。
