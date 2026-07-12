package com.tandiantong.reservation.app;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.reservation.tenant.TenantStoreScope;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationPersistenceService {
    private final JdbcTemplate jdbcTemplate;
    public ReservationPersistenceService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate=jdbcTemplate; }

    @Transactional
    public ServiceResult createService(TenantStoreScope scope, CreateServiceCommand command) {
        if(command.name()==null||command.name().isBlank()||command.priceCent()<0||command.durationMinutes()<=0) throw new BusinessException(ErrorCode.VALIDATION_FAILED,"服务项目参数不合法");
        Long id=insertId("insert into service_item (tenant_id,store_id,name,payment_mode,price_cent,duration_minutes,status) values (?,?,?,?,?,?,'ENABLED')",scope.tenantId(),scope.storeId(),command.name(),command.paymentMode(),command.priceCent(),command.durationMinutes());
        return new ServiceResult(id,command.name(),command.paymentMode(),command.priceCent(),command.durationMinutes());
    }

    @Transactional
    public SlotResult createSlot(TenantStoreScope scope, CreateSlotCommand command) {
        Integer serviceCount=jdbcTemplate.queryForObject("select count(*) from service_item where id=? and tenant_id=? and store_id=? and status='ENABLED'",Integer.class,command.serviceId(),scope.tenantId(),scope.storeId());
        if(serviceCount==null||serviceCount!=1||command.capacity()<=0) throw new BusinessException(ErrorCode.VALIDATION_FAILED,"服务项目或时段容量不合法");
        Long id=insertId("insert into service_slot (tenant_id,store_id,service_id,service_date,start_time,end_time,capacity,used_capacity,paused,version) values (?,?,?,?,?,?,?,0,false,0)",scope.tenantId(),scope.storeId(),command.serviceId(),command.serviceDate(),command.startTime(),command.endTime(),command.capacity());
        return new SlotResult(id,command.serviceId(),command.serviceDate(),command.startTime(),command.endTime(),command.capacity(),0);
    }

    public List<MiniService> listByScene(String scene) {
        return jdbcTemplate.query("select i.id,i.name,i.payment_mode,i.price_cent,i.duration_minutes,s.id slot_id,s.service_date,s.start_time,s.end_time,s.capacity,s.used_capacity from mini_program_scene m join service_item i on i.tenant_id=m.tenant_id and i.store_id=m.store_id join service_slot s on s.service_id=i.id and s.tenant_id=i.tenant_id and s.store_id=i.store_id where m.scene_key=? and m.enabled=true and i.status='ENABLED' and s.paused=false and s.service_date>=current_date order by s.service_date,s.start_time",
                (rs,row)->new MiniService(rs.getLong("id"),rs.getString("name"),rs.getString("payment_mode"),rs.getInt("price_cent"),rs.getInt("duration_minutes"),rs.getLong("slot_id"),rs.getObject("service_date",LocalDate.class),rs.getString("start_time"),rs.getString("end_time"),rs.getInt("capacity")-rs.getInt("used_capacity")),scene);
    }

    @Transactional
    public ReservationResult reserve(String scene, ReserveCommand command) {
        Scope scope=scope(scene);
        List<ReservationResult> previous=jdbcTemplate.query("select reservation_no,status,voucher_code from service_reservation r join business_idempotency_record b on b.tenant_id=r.tenant_id and b.business_no=r.reservation_no where b.tenant_id=? and b.business_type='RESERVATION_CREATE' and b.idempotency_key=?",(rs,row)->new ReservationResult(rs.getString("reservation_no"),rs.getString("status"),rs.getString("voucher_code")),scope.tenantId(),command.idempotencyKey());
        if(!previous.isEmpty()) return previous.getFirst();
        List<ServiceMode> modes=jdbcTemplate.query("select i.payment_mode from service_item i join service_slot s on s.service_id=i.id and s.tenant_id=i.tenant_id and s.store_id=i.store_id where i.id=? and s.id=? and i.tenant_id=? and i.store_id=?",(rs,row)->new ServiceMode(rs.getString(1)),command.serviceId(),command.slotId(),scope.tenantId(),scope.storeId());
        if(modes.size()!=1) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,"预约服务或时段不存在");
        int occupied=jdbcTemplate.update("update service_slot set used_capacity=used_capacity+1,version=version+1 where id=? and tenant_id=? and store_id=? and paused=false and used_capacity<capacity",command.slotId(),scope.tenantId(),scope.storeId());
        if(occupied!=1) throw new BusinessException(ErrorCode.VALIDATION_FAILED,"预约时段剩余名额不足");
        String no="YY"+scope.tenantId()+UUID.randomUUID().toString().replace("-","").substring(0,14);
        String status="FREE".equals(modes.getFirst().paymentMode())?"CONFIRMED":"PENDING_PAYMENT";
        String voucher="CONFIRMED".equals(status)?"rv-"+UUID.randomUUID().toString().replace("-",""):null;
        jdbcTemplate.update("insert into service_reservation (tenant_id,store_id,service_id,slot_id,reservation_no,status,contact_mobile,voucher_code) values (?,?,?,?,?,?,?,?)",scope.tenantId(),scope.storeId(),command.serviceId(),command.slotId(),no,status,command.contactMobile(),voucher);
        jdbcTemplate.update("insert into business_idempotency_record (tenant_id,idempotency_key,business_type,business_no,result_status) values (?,?,'RESERVATION_CREATE',?,'SUCCESS')",scope.tenantId(),command.idempotencyKey(),no);
        return new ReservationResult(no,status,voucher);
    }

    @Transactional
    public ReservationResult cancel(TenantStoreScope scope,String reservationNo) {
        List<ReservationRow> rows=jdbcTemplate.query("select slot_id,status,voucher_code from service_reservation where tenant_id=? and store_id=? and reservation_no=? for update",(rs,row)->new ReservationRow(rs.getLong("slot_id"),rs.getString("status"),rs.getString("voucher_code")),scope.tenantId(),scope.storeId(),reservationNo);
        if(rows.size()!=1) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,"预约不存在");
        ReservationRow row=rows.getFirst();
        if("CANCELED".equals(row.status())) return new ReservationResult(reservationNo,row.status(),row.voucher());
        if("FULFILLED".equals(row.status())) throw new BusinessException(ErrorCode.VALIDATION_FAILED,"已履约预约不能取消");
        jdbcTemplate.update("update service_reservation set status='CANCELED' where tenant_id=? and store_id=? and reservation_no=? and status in ('CONFIRMED','PENDING_PAYMENT')",scope.tenantId(),scope.storeId(),reservationNo);
        jdbcTemplate.update("update service_slot set used_capacity=used_capacity-1,version=version+1 where id=? and tenant_id=? and store_id=? and used_capacity>0",row.slotId(),scope.tenantId(),scope.storeId());
        return new ReservationResult(reservationNo,"CANCELED",row.voucher());
    }

    private Scope scope(String scene){List<Scope> scopes=jdbcTemplate.query("select tenant_id,store_id from mini_program_scene where scene_key=? and enabled=true",(rs,row)->new Scope(rs.getLong(1),rs.getLong(2)),scene);if(scopes.size()!=1)throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,"商户入口码无效");return scopes.getFirst();}
    private Long insertId(String sql,Object... values){return jdbcTemplate.execute((ConnectionCallback<Long>)c->{PreparedStatement s=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);s.executeUpdate();try(var keys=s.getGeneratedKeys()){if(!keys.next())throw new IllegalStateException("数据库未返回主键");return keys.getLong(1);}});}
    public record CreateServiceCommand(String name,String paymentMode,int priceCent,int durationMinutes){}
    public record CreateSlotCommand(Long serviceId,LocalDate serviceDate,String startTime,String endTime,int capacity){}
    public record ReserveCommand(String idempotencyKey,Long serviceId,Long slotId,String contactMobile){}
    public record ServiceResult(Long serviceId,String name,String paymentMode,int priceCent,int durationMinutes){}
    public record SlotResult(Long slotId,Long serviceId,LocalDate serviceDate,String startTime,String endTime,int capacity,int usedCapacity){}
    public record MiniService(Long serviceId,String name,String paymentMode,int priceCent,int durationMinutes,Long slotId,LocalDate serviceDate,String startTime,String endTime,int remainingCapacity){}
    public record ReservationResult(String reservationNo,String status,String voucherCode){}
    private record Scope(Long tenantId,Long storeId){}
    private record ServiceMode(String paymentMode){}
    private record ReservationRow(Long slotId,String status,String voucher){}
}
