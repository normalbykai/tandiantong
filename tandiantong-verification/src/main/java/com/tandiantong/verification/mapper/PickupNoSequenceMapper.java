package com.tandiantong.verification.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.verification.entity.PickupNoSequenceEntity;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 取餐号序列 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface PickupNoSequenceMapper extends BaseMapper<PickupNoSequenceEntity> {

    /**
     * 原子创建或递增指定营业日的取餐号序列。
     */
    @Insert("""
            insert into pickup_no_sequence (tenant_id, store_id, business_date, current_value)
            values (#{tenantId}, #{storeId}, #{businessDate}, 1)
            on duplicate key update current_value = current_value + 1
            """)
    int increment(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                  @Param("businessDate") LocalDate businessDate);

    /**
     * 查询指定营业日当前取餐号序列值。
     */
    @Select("""
            select current_value from pickup_no_sequence
             where tenant_id = #{tenantId} and store_id = #{storeId} and business_date = #{businessDate}
            """)
    Integer selectCurrentValue(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                               @Param("businessDate") LocalDate businessDate);
}
