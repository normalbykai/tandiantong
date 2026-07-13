package com.tandiantong.verification.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.verification.entity.VerificationCredentialEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 核销凭证 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface VerificationCredentialMapper extends BaseMapper<VerificationCredentialEntity> {

    /**
     * 按租户、门店、令牌哈希和当前状态原子更新凭证状态。
     */
    @Update("""
            update verification_credential set status = #{targetStatus}
             where tenant_id = #{tenantId} and store_id = #{storeId} and token_hash = #{tokenHash}
               and status = #{currentStatus}
            """)
    int updateStatusByToken(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                            @Param("tokenHash") String tokenHash, @Param("currentStatus") String currentStatus,
                            @Param("targetStatus") String targetStatus);

    /**
     * 按业务单号取消尚未使用的核销凭证。
     */
    @Update("""
            update verification_credential set status = #{targetStatus}
             where tenant_id = #{tenantId} and store_id = #{storeId} and business_type = #{businessType}
               and business_no = #{businessNo} and status = #{currentStatus}
            """)
    int updateBusinessStatus(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                             @Param("businessType") String businessType, @Param("businessNo") String businessNo,
                             @Param("currentStatus") String currentStatus, @Param("targetStatus") String targetStatus);
}
