# 摊点通通用兼容层

`tandiantong-common` 保留旧包名兼容能力，便于历史代码平滑迁移到 `tandiantong-framework-common`。

## 使用约束

- 新增业务代码必须优先依赖 `tandiantong-framework-common`。
- 新增基础设施代码应放入 `tandiantong-framework` 对应模块。
- 本模块不得新增业务模型、业务枚举、业务服务或新的通用能力实现。
- 旧包名类型如 `com.tandiantong.common.trace.TraceIdContext` 只能委托 framework 实现，不应继续扩展。

后续确认无外部或历史代码依赖旧包名后，可以再评估是否删除该兼容层。
