# 摊点通文档结构迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将原有文档迁移为“跨版本规范 + 版本 + 阶段 + 文档类型”的结构，并修正全部路径引用。

**Architecture:** 跨版本长期规则放入 `docs/standards/`，V1 架构与原型设计放入 `docs/versions/v1/`。通过根索引和 V1 索引标记阶段状态，后续每个阶段独立维护设计、计划和验收文档。

**Tech Stack:** Markdown、Git、Ripgrep

---

### Task 1: 创建文档索引和 V1 阶段目录

**Files:**
- Create: `docs/README.md`
- Create: `docs/versions/v1/README.md`

- [ ] **Step 1: 创建文档总索引**

在 `docs/README.md` 中说明跨版本规范、版本文档、当前版本和阅读顺序。

- [ ] **Step 2: 创建 V1 索引**

在 `docs/versions/v1/README.md` 中列出 V1 范围、九个开发阶段、状态含义和各文档入口。

### Task 2: 迁移现有文档

**Files:**
- Move: 原项目规则设计文档 → `docs/standards/project-rules-design.md`
- Move: 原项目规则实施计划 → `docs/standards/project-rules-implementation-plan.md`
- Move: 原 V1 架构设计文档 → `docs/versions/v1/00-overview/architecture.md`
- Move: 原 V1 产品原型设计方案 → `docs/versions/v1/00-overview/product-prototype-design.md`

- [ ] **Step 1: 创建目标目录并使用 Git 移动文件**

使用 `git mv` 保留文档历史，禁止复制后遗留重复旧文件。

- [ ] **Step 2: 创建阶段占位目录说明**

阶段目录只在开始对应阶段时创建，V1 索引先记录规划路径，避免提前生成空文档。

### Task 3: 修正引用与原型输出路径

**Files:**
- Modify: `AGENTS.md`
- Modify: `docs/standards/project-rules-design.md`
- Modify: `docs/standards/project-rules-implementation-plan.md`
- Modify: `docs/versions/v1/00-overview/product-prototype-design.md`

- [ ] **Step 1: 更新架构文档引用**

将旧架构路径统一替换为 `docs/versions/v1/00-overview/architecture.md`。

- [ ] **Step 2: 更新规则文档引用**

将规则设计和实施计划中的旧路径更新为 `docs/standards/` 下的新路径。

- [ ] **Step 3: 更新原型文档输出目录**

将旧原型输出目录更新为 `docs/versions/v1/prototype/`，保留原有五份原型交付文件结构。

### Task 4: 验证并提交迁移

- [ ] **Step 1: 检查旧路径引用**

Run:

```powershell
rg -n "旧版文档目录|旧原型目录标识" . -g '!/.git/**' -g '!/.superpowers/**'
```

Expected: 不返回任何匹配。

- [ ] **Step 2: 检查文档结构与链接目标**

Run:

```powershell
rg --files docs
git diff --check
```

Expected: 文档仅位于 `docs/standards/`、`docs/versions/v1/` 和索引位置；格式检查通过。

- [ ] **Step 3: 提交迁移**

```powershell
git add -- AGENTS.md docs
git commit -m "docs: 按版本和阶段重组开发文档"
```

Expected: 提交成功，提交主题使用中文，工作区干净。
