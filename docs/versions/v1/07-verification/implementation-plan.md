# 07 取餐号与核销实施计划

## 1. 功能单元

1. 数据库迁移：创建核销凭证、核销记录和取餐号序列表。
2. 核销领域服务：生成商品取餐号、签发核销凭证、扫码核销、重复核销返回原结果。
3. 自动化测试：覆盖令牌安全、取餐号非凭据、重复核销、跨租户拒绝和预约核销。
4. 前端原型：B 端核销中心和 C 端凭证安全提示。
5. 验收验证：后端测试、前端审计、类型检查、构建和空白检查。

## 2. 验证方式

- `mvn -pl tandiantong-verification -am test`
- `mvn test`
- `npm audit --audit-level=moderate`
- `npm run typecheck`
- `npm run build`
- `git diff --check`

