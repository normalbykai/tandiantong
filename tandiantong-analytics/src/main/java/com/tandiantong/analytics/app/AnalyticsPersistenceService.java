package com.tandiantong.analytics.app;

import com.tandiantong.analytics.tenant.TenantStoreScope;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsPersistenceService {
    private final JdbcTemplate jdbcTemplate;
    public AnalyticsPersistenceService(JdbcTemplate jdbcTemplate){this.jdbcTemplate=jdbcTemplate;}

    public Dashboard dashboard(TenantStoreScope scope,LocalDate start,LocalDate end){
        Summary order=jdbcTemplate.queryForObject("select count(*) order_count,coalesce(sum(case when status in ('PENDING_VERIFY','COMPLETED','REFUNDED') then pay_amount_cent else 0 end),0) gross,coalesce(sum(case when status='REFUNDED' then pay_amount_cent else 0 end),0) refunds,sum(case when status='PENDING_VERIFY' then 1 else 0 end) pending from sales_order where tenant_id=? and store_id=? and date(created_at) between ? and ?",(rs,row)->new Summary(rs.getInt("order_count"),rs.getInt("gross"),rs.getInt("refunds"),rs.getInt("pending")),scope.tenantId(),scope.storeId(),start,end);
        ReservationMetrics reservation=jdbcTemplate.queryForObject("select count(*) total,sum(case when status='CANCELED' then 1 else 0 end) canceled,sum(case when status='FULFILLED' then 1 else 0 end) fulfilled from service_reservation where tenant_id=? and store_id=? and date(created_at) between ? and ?",(rs,row)->new ReservationMetrics(rs.getInt("total"),rs.getInt("canceled"),rs.getInt("fulfilled")),scope.tenantId(),scope.storeId(),start,end);
        List<ProductMetric> products=jdbcTemplate.query("select i.product_name,sum(i.quantity) quantity,sum(i.subtotal_cent) amount from sales_order_item i join sales_order o on o.tenant_id=i.tenant_id and o.store_id=i.store_id and o.order_no=i.order_no where i.tenant_id=? and i.store_id=? and date(i.created_at) between ? and ? and o.status in ('PENDING_VERIFY','COMPLETED','REFUNDED') group by i.product_name order by quantity desc limit 20",(rs,row)->new ProductMetric(rs.getString("product_name"),rs.getInt("quantity"),rs.getInt("amount")),scope.tenantId(),scope.storeId(),start,end);
        return new Dashboard(order,reservation,products);
    }

    @Transactional
    public ExportFile export(TenantStoreScope scope,LocalDate start,LocalDate end,String contact){
        Dashboard dashboard=dashboard(scope,start,end);
        String fileName="经营数据-"+start+"-"+end+".xlsx";
        byte[] content=workbook(dashboard);
        jdbcTemplate.update("insert into analytics_export_task (tenant_id,store_id,export_type,start_date,end_date,file_name,status,operator_user_id,operator_contact_masked,audit_message,finished_at) values (?,?,'TRANSACTION',?,?,?,'FINISHED',?,?,?,current_timestamp(3))",scope.tenantId(),scope.storeId(),start,end,fileName,scope.operatorUserId(),mask(contact),"租户经营数据导出完成");
        return new ExportFile(fileName,content);
    }

    private byte[] workbook(Dashboard data){try(var workbook=new XSSFWorkbook();var output=new ByteArrayOutputStream()){var summary=workbook.createSheet("经营概览");String[][] rows={{"指标","数值"},{"订单数",String.valueOf(data.order().orderCount())},{"实收金额（分）",String.valueOf(data.order().grossCent())},{"退款金额（分）",String.valueOf(data.order().refundCent())},{"待核销订单",String.valueOf(data.order().pendingVerification())},{"预约数",String.valueOf(data.reservation().total())}};for(int i=0;i<rows.length;i++){var row=summary.createRow(i);for(int j=0;j<rows[i].length;j++)row.createCell(j).setCellValue(rows[i][j]);}var ranking=workbook.createSheet("商品排行");var head=ranking.createRow(0);head.createCell(0).setCellValue("商品");head.createCell(1).setCellValue("销量");head.createCell(2).setCellValue("销售额（分）");for(int i=0;i<data.products().size();i++){var item=data.products().get(i);var row=ranking.createRow(i+1);row.createCell(0).setCellValue(item.name());row.createCell(1).setCellValue(item.quantity());row.createCell(2).setCellValue(item.amountCent());}workbook.write(output);return output.toByteArray();}catch(IOException e){throw new IllegalStateException("生成经营数据 Excel 失败",e);}}
    private String mask(String contact){if(contact==null||contact.length()<7)return "****";return contact.substring(0,3)+"****"+contact.substring(contact.length()-4);}
    public record Summary(int orderCount,int grossCent,int refundCent,int pendingVerification){}
    public record ReservationMetrics(int total,int canceled,int fulfilled){}
    public record ProductMetric(String name,int quantity,int amountCent){}
    public record Dashboard(Summary order,ReservationMetrics reservation,List<ProductMetric> products){}
    public record ExportFile(String fileName,byte[] content){}
}
