package com.tandiantong.verification.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.verification.entity.VerificationRecordEntity;

/**
 * 核销记录 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface VerificationRecordMapper extends BaseMapper<VerificationRecordEntity> {
}
