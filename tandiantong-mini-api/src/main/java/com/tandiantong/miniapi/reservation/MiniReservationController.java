package com.tandiantong.miniapi.reservation;

import com.tandiantong.miniapi.reservation.dto.ReserveRequest;
import com.tandiantong.miniapi.reservation.dto.MiniServiceResponse;
import com.tandiantong.miniapi.reservation.dto.ReservationResponse;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.security.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 小程序预约服务查询与提交接口。 */
@RestController
@RequestMapping("/api/mini/v1/reservations")
@Tag(name = "小程序预约", description = "顾客查询可预约服务并提交预约")
public class MiniReservationController {
    private final ReservationPersistenceService service;
    public MiniReservationController(ReservationPersistenceService service){this.service=service;}
    @Operation(summary = "查询可预约服务", description = "查询指定商户入口下可预约的服务项目、时段和剩余容量")
    @GetMapping("/services") public List<MiniServiceResponse> services(
            @Parameter(description = "商户小程序入口码", example = "scene_xinghe_001", required = true)
            @RequestParam("scene") String scene){return service.listByScene(scene).stream().map(MiniServiceResponse::from).toList();}
    @Operation(summary = "提交预约", description = "使用幂等键占用预约时段容量并创建顾客预约")
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request){return ReservationResponse.from(service.reserve(request.sceneKey(),new ReservationPersistenceService.ReserveCommand(request.idempotencyKey(),request.serviceId(),request.slotId(),request.contactMobile())));}

    @Operation(summary = "查询预约列表", description = "按当前租户门店和联系电话查询顾客预约列表")
    @GetMapping
    public List<ReservationPersistenceService.CustomerReservationView> list(
            @Parameter(description = "顾客联系电话", example = "13800000000", required = true)
            @RequestParam("contactMobile") String contactMobile,
            @Parameter(description = "预约状态筛选，不传则查询全部状态", example = "CONFIRMED")
            @RequestParam(value = "status", required = false) String status) {
        var user = SecurityContextHolder.currentUser();
        return service.listCustomerReservations(user.tenantId(), user.storeId(), contactMobile, status);
    }

    @Operation(summary = "查询预约详情", description = "按预约单号查询当前顾客预约详情")
    @GetMapping("/{reservationNo}")
    public ReservationPersistenceService.CustomerReservationView detail(
            @Parameter(description = "平台预约单号", example = "YY10001ABCDEF123456", required = true)
            @PathVariable("reservationNo") String reservationNo,
            @Parameter(description = "顾客联系电话", example = "13800000000", required = true)
            @RequestParam("contactMobile") String contactMobile) {
        var user = SecurityContextHolder.currentUser();
        return service.getCustomerReservationDetail(user.tenantId(), user.storeId(), reservationNo, contactMobile);
    }

    @Operation(summary = "取消预约", description = "顾客取消本人尚未履约的预约并释放容量")
    @DeleteMapping("/{reservationNo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @Parameter(description = "平台预约单号", example = "YY10001ABCDEF123456", required = true)
            @PathVariable("reservationNo") String reservationNo,
            @Parameter(description = "顾客联系电话", example = "13800000000", required = true)
            @RequestParam("contactMobile") String contactMobile) {
        var user = SecurityContextHolder.currentUser();
        service.cancelCustomerReservation(user.tenantId(), user.storeId(), reservationNo, contactMobile);
    }
}
