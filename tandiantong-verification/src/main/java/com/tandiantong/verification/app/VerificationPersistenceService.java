package com.tandiantong.verification.app;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationPersistenceService {
    private final JdbcTemplate jdbcTemplate;
    public VerificationPersistenceService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    @Transactional
    public Credential issueOrderCredential(Long tenantId, Long storeId, String orderNo, String summary) {
        List<Credential> existing = jdbcTemplate.query("select business_no,pickup_no,token_hash,status from verification_credential where tenant_id=? and store_id=? and business_type='PRODUCT_ORDER' and business_no=?",
                (resultSet, rowNumber) -> new Credential(resultSet.getString("business_no"), resultSet.getString("pickup_no"), resultSet.getString("token_hash"), resultSet.getString("status")), tenantId, storeId, orderNo);
        if (!existing.isEmpty()) return existing.getFirst();
        LocalDate businessDate = LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        Integer current = jdbcTemplate.queryForObject("select current_value from pickup_no_sequence where tenant_id=? and store_id=? and business_date=? for update", Integer.class, tenantId, storeId, businessDate);
        int next = current == null ? 1 : current + 1;
        if (current == null) jdbcTemplate.update("insert into pickup_no_sequence (tenant_id,store_id,business_date,current_value) values (?,?,?,?)",tenantId,storeId,businessDate,next);
        else jdbcTemplate.update("update pickup_no_sequence set current_value=? where tenant_id=? and store_id=? and business_date=?",next,tenantId,storeId,businessDate);
        String token = "vk-" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String pickupNo = "A" + String.format("%03d",next);
        jdbcTemplate.update("insert into verification_credential (tenant_id,store_id,business_type,business_no,summary,business_date,pickup_no,token_hash,status) values (?,?,'PRODUCT_ORDER',?,?,?,?,?,'PENDING')",tenantId,storeId,orderNo,summary,businessDate,pickupNo,sha256(token));
        return new Credential(orderNo,pickupNo,token,"PENDING");
    }

    @Transactional
    public VerificationResult verify(Long tenantId, Long storeId, Long operatorId, String token, String reason) {
        String tokenHash=sha256(token);
        List<Credential> found=jdbcTemplate.query("select business_no,pickup_no,token_hash,status from verification_credential where tenant_id=? and store_id=? and token_hash=?",(rs,row)->new Credential(rs.getString("business_no"),rs.getString("pickup_no"),rs.getString("token_hash"),rs.getString("status")),tenantId,storeId,tokenHash);
        if(found.size()!=1) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,"核销凭证不存在");
        Credential credential=found.getFirst();
        int updated=jdbcTemplate.update("update verification_credential set status='VERIFIED' where tenant_id=? and store_id=? and token_hash=? and status='PENDING'",tenantId,storeId,tokenHash);
        if(updated==0 && !"VERIFIED".equals(credential.status())) throw new BusinessException(ErrorCode.VALIDATION_FAILED,"当前凭证不能核销");
        if(updated==1) {
            jdbcTemplate.update("insert into verification_record (tenant_id,store_id,business_type,business_no,summary,operator_user_id,reason,verified_at) select tenant_id,store_id,business_type,business_no,summary,?,?,current_timestamp(3) from verification_credential where token_hash=?",operatorId,reason == null ? "正常核销" : reason,tokenHash);
            jdbcTemplate.update("update sales_order set status='COMPLETED' where tenant_id=? and store_id=? and order_no=? and status='PENDING_VERIFY'",tenantId,storeId,credential.businessNo());
        }
        return new VerificationResult(credential.businessNo(),credential.pickupNo(),updated==1 ? "VERIFIED" : "VERIFIED");
    }
    private String sha256(String raw){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException("当前运行环境缺少 SHA-256 摘要算法",e);}}
    public record Credential(String businessNo,String pickupNo,String token,String status){}
    public record VerificationResult(String businessNo,String pickupNo,String status){}
}
