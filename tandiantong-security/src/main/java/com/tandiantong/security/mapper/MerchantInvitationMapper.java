package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.MerchantInvitationEntity;

/**
 * 商户邀请 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface MerchantInvitationMapper extends BaseMapper<MerchantInvitationEntity> {
}
