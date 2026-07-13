package com.tandiantong.security.tenant;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.security.entity.MiniProgramSceneEntity;
import com.tandiantong.security.mapper.MiniProgramSceneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 小程序入口码应用服务，负责将外部场景码解析为服务端可信租户与门店范围。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class MerchantSceneService {

    private final MiniProgramSceneMapper miniProgramSceneMapper;

    /**
     * 解析启用状态的入口码。
     *
     * @param sceneKey 小程序入口码
     * @return 服务端可信租户与门店范围
     */
    public MerchantSceneScope resolveEnabledScene(String sceneKey) {
        MiniProgramSceneEntity scene = miniProgramSceneMapper.selectOne(Wrappers.<MiniProgramSceneEntity>lambdaQuery()
                .eq(MiniProgramSceneEntity::getSceneKey, sceneKey)
                .eq(MiniProgramSceneEntity::getEnabled, true));
        if (scene == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户入口码无效");
        }
        return new MerchantSceneScope(scene.getTenantId(), scene.getStoreId());
    }

    /**
     * 小程序入口码解析后的可信业务范围。
     *
     * @param tenantId 租户主键
     * @param storeId 门店主键
     */
    public record MerchantSceneScope(Long tenantId, Long storeId) {
    }
}
