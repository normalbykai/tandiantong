package com.tandiantong.framework.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.tandiantong.framework.mybatis.core.tenant.TenantIdProvider;
import com.tandiantong.framework.mybatis.core.tenant.TenantTableProvider;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** MyBatis-Plus 统一自动配置。 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class TandiantongMybatisAutoConfiguration {

    /**
     * 注册分页和租户拦截器，确保 Mapper 查询默认具备租户隔离能力。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            ObjectProvider<TenantIdProvider> tenantIdProvider,
            ObjectProvider<TenantTableProvider> tenantTableProvider) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler(
                tenantIdProvider,
                tenantTableProvider)));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    private static class TenantLineHandler implements com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler {

        private final ObjectProvider<TenantIdProvider> tenantIdProvider;

        private final ObjectProvider<TenantTableProvider> tenantTableProvider;

        private TenantLineHandler(
                ObjectProvider<TenantIdProvider> tenantIdProvider,
                ObjectProvider<TenantTableProvider> tenantTableProvider) {
            this.tenantIdProvider = tenantIdProvider;
            this.tenantTableProvider = tenantTableProvider;
        }

        /**
         * 租户编号必须来自服务端可信用户上下文，禁止从请求参数中读取。
         */
        @Override
        public Expression getTenantId() {
            TenantIdProvider provider = tenantIdProvider.getIfAvailable();
            if (provider == null) {
                throw new IllegalStateException("当前请求缺少租户编号提供器");
            }
            return new LongValue(provider.currentTenantId());
        }

        /**
         * 只有已经确认包含 tenant_id 的业务表才进入自动租户隔离。
         */
        @Override
        public boolean ignoreTable(String tableName) {
            TenantTableProvider provider = tenantTableProvider.getIfAvailable();
            if (provider == null) {
                return true;
            }
            Set<String> tenantTables = provider.tenantTables();
            return tenantTables == null || !tenantTables.contains(tableName);
        }
    }
}
