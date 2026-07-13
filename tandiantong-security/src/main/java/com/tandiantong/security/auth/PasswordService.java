package com.tandiantong.security.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 密码哈希服务，统一使用 BCrypt 保存和校验后台用户密码。
 */
@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 对明文密码执行不可逆哈希。
     *
     * @param plainPassword 明文密码
     * @return BCrypt 密码哈希
     */
    public String hash(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    /**
     * 校验明文密码与已保存哈希是否匹配。
     *
     * @param plainPassword 明文密码
     * @param passwordHash 已保存密码哈希
     * @return 是否匹配
     */
    public boolean matches(String plainPassword, String passwordHash) {
        return passwordEncoder.matches(plainPassword, passwordHash);
    }
}
