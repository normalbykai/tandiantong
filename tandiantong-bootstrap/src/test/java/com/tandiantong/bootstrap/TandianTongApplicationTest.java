package com.tandiantong.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.analytics.app.AnalyticsPersistenceService;
import com.tandiantong.catalog.product.CatalogPersistenceService;
import com.tandiantong.order.app.PersistentOrderService;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.security.auth.DatabaseAuthenticationService;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import com.tandiantong.verification.app.VerificationPersistenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TandianTongApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MerchantProvisioningService merchantProvisioningService;

    @MockBean
    private DatabaseAuthenticationService databaseAuthenticationService;

    @MockBean
    private CatalogPersistenceService catalogPersistenceService;

    @MockBean
    private PersistentOrderService persistentOrderService;

    @MockBean
    private ReservationPersistenceService reservationPersistenceService;

    @MockBean
    private VerificationPersistenceService verificationPersistenceService;

    @MockBean
    private AnalyticsPersistenceService analyticsPersistenceService;

    @Test
    void shouldLoadSpringContext() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void shouldReturnTraceIdHeaderAndBody() throws Exception {
        mockMvc.perform(get("/foundation-test/ok")
                        .header("X-Trace-Id", "client-trace-001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", "client-trace-001"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").value("client-trace-001"))
                .andExpect(jsonPath("$.data.status").value("基础服务正常"));
    }

    @Test
    void shouldHideInternalDetailsForBusinessException() throws Exception {
        mockMvc.perform(get("/foundation-test/business-error")
                        .header("X-Trace-Id", "trace-business")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("手机号格式不正确"))
                .andExpect(jsonPath("$.traceId").value("trace-business"));
    }

    @TestConfiguration
    static class FoundationTestControllerConfig {

        @Bean
        FoundationTestController foundationTestController() {
            return new FoundationTestController();
        }
    }

    @RestController
    static class FoundationTestController {

        @GetMapping("/foundation-test/ok")
        FoundationStatus ok() {
            return new FoundationStatus("基础服务正常");
        }

        @GetMapping("/foundation-test/business-error")
        String businessError() {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "手机号格式不正确");
        }
    }

    record FoundationStatus(String status) {
    }
}
