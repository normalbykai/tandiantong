# 摊点通项目级规则实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在仓库根目录创建可被后续开发人员和编码代理直接遵循的项目级 `AGENTS.md`。

**Architecture:** 使用单一根规则文件覆盖整个仓库，规则引用已确认的架构设计，不复制完整业务规格。内容按范围、注释、模块、后端、前端、多租户、交易、数据、测试、安全和交付顺序组织。

**Tech Stack:** Markdown、Git、Ripgrep

---

### Task 1: 创建并验证项目级规则

**Files:**
- Create: `AGENTS.md`
- Reference: `docs/superpowers/specs/2026-07-12-project-rules-design.md`
- Reference: `docs/superpowers/specs/2026-07-12-tandiantong-architecture-design.md`

- [ ] **Step 1: 创建根目录规则文件**

创建 `AGENTS.md`，至少包含以下强制内容：

```text
适用范围与规则优先级
第一阶段范围与 YAGNI 原则
必要注释必须使用简体中文，禁止英文解释性注释
Maven 模块职责和禁止跨模块访问 Repository
Java、TypeScript、API、异常与日志规范
tenant_id 与 store_id 隔离规则
RBAC 后端强制鉴权规则
金额整数分、订单快照、库存流水规则
支付、退款、任务和核销幂等规则
Flyway、Redis 缓存键和数据删除规则
测试、验证、敏感信息和 Git 安全规则
```

- [ ] **Step 2: 检查规则关键内容**

Run:

```powershell
rg -n "中文|英文解释性注释|tenant_id|store_id|幂等|Flyway|Repository|测试|敏感" AGENTS.md
```

Expected: 每个关键约束均至少匹配一次，且内容是明确的“必须”或“禁止”规则。

- [ ] **Step 3: 检查未决占位和格式问题**

Run:

```powershell
rg -n "TBD|TODO|待定|暂定|后续补充" AGENTS.md
git diff --check
```

Expected: `rg` 不返回匹配；`git diff --check` 退出码为 0。

- [ ] **Step 4: 对照设计文档检查范围**

逐项核对 `AGENTS.md` 与两份设计文档，确认没有引入微服务、消息队列、多门店运营、套餐或动态数据权限等未确认的首期要求，也没有削弱租户隔离和交易安全要求。

- [ ] **Step 5: 提交规则文件**

```powershell
git add -- AGENTS.md docs/superpowers/plans/2026-07-12-project-rules.md
git commit -m "docs: add project development rules"
```

Expected: 提交成功，工作区保持干净。
