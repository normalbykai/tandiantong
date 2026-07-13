package com.tandiantong.security.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.security.entity.PlatformUserEntity;
import com.tandiantong.security.mapper.PlatformUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 平台管理员应用服务，负责平台初始管理员的幂等创建。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class PlatformAdministratorService {

    private static final String ENABLED_STATUS = "ENABLED";
    private static final int INITIAL_TOKEN_VERSION = 1;

    private final PlatformUserMapper platformUserMapper;
    private final PasswordService passwordService;

    /**
     * 手机号不存在时创建平台管理员。
     *
     * @param mobile 登录手机号
     * @param password 初始密码
     * @param displayName 展示名称
     */
    @Transactional
    public void createIfAbsent(String mobile, String password, String displayName) {
        Long count = platformUserMapper.selectCount(Wrappers.<PlatformUserEntity>lambdaQuery()
                .eq(PlatformUserEntity::getMobile, mobile));
        if (count > 0) {
            return;
        }
        PlatformUserEntity user = new PlatformUserEntity();
        user.setMobile(mobile);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordService.hash(password));
        user.setStatus(ENABLED_STATUS);
        user.setTokenVersion(INITIAL_TOKEN_VERSION);
        platformUserMapper.insert(user);
    }
}
