# 摊点通文档中心

## 当前版本

当前规划与开发版本为 [V1](versions/v1/README.md)。新需求必须先确认归属版本和阶段，禁止将未来版本能力直接补入 V1 实施文档。

## 文档结构

```text
docs/
├─ README.md
├─ standards/                 跨版本长期规范
└─ versions/
   └─ v1/                     V1 产品、架构、原型、计划和验收文档
```

## 阅读顺序

1. 阅读根目录 `AGENTS.md`，了解强制开发规则。
2. 阅读 [V1 文档索引](versions/v1/README.md)，确认当前阶段和范围。
3. 阅读 [V1 总体架构](versions/v1/00-overview/architecture.md)。
4. 根据当前阶段进入对应目录，只使用该阶段已经确认的设计、计划和验收文档。

## 跨版本规范

- [项目规则设计](standards/project-rules-design.md)
- [项目规则实施计划](standards/project-rules-implementation-plan.md)
- [文档结构迁移计划](standards/document-structure-migration-plan.md)

跨版本规范用于约束所有版本，不随单个版本完成而失效。版本专属的业务范围、架构决策、原型和实施计划必须放入对应版本目录。

## 版本维护规则

- V1 完成后保留历史，不直接改写为 V2。
- V2 开始时创建 `docs/versions/v2/`，并在本索引中增加入口。
- 通用规则变化时更新 `docs/standards/`；业务功能变化时更新对应版本和阶段目录。
- 阶段目录只在该阶段开始设计或实施时创建，禁止提前生成大量空文档。

