package com.tandiantong.bootstrap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import com.tandiantong.security.auth.DatabaseAuthenticationService;
import com.tandiantong.catalog.product.CatalogPersistenceService;
import com.tandiantong.order.app.PersistentOrderService;
import com.tandiantong.analytics.app.AnalyticsPersistenceService;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.verification.app.VerificationPersistenceService;

import static org.mockito.BDDMockito.given;
import java.time.Instant;
import com.tandiantong.security.tenant.PaymentConfigStatus;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformApiContractTest {

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
    private AnalyticsPersistenceService analyticsPersistenceService;

    @MockBean
    private ReservationPersistenceService reservationPersistenceService;

    @MockBean
    private VerificationPersistenceService verificationPersistenceService;

    @Test
    void shouldExposePlatformHealthThroughVersionedApi() throws Exception {
        mockMvc.perform(get("/api/platform/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("平台服务正常"));
    }

    @Test
    void shouldRejectMerchantProvisioningWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/platform/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantName\":\"春风小铺\",\"storeAddress\":\"杭州市西湖区春风路1号\",\"adminName\":\"张晓春\",\"adminMobile\":\"13800008000\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "platform-admin", roles = "PLATFORM")
    void shouldCreateMerchantThroughPlatformApi() throws Exception {
        given(merchantProvisioningService.provision(org.mockito.ArgumentMatchers.any()))
                .willReturn(new MerchantProvisioningService.ProvisionedMerchant(1001L, 1002L, "春风小铺", "春风小铺默认门店",
                        "invite-test", Instant.parse("2026-07-19T00:00:00Z"), "scene-test", PaymentConfigStatus.NOT_CONFIGURED));
        mockMvc.perform(post("/api/platform/v1/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantName\":\"春风小铺\",\"storeAddress\":\"杭州市西湖区春风路1号\",\"adminName\":\"张晓春\",\"adminMobile\":\"13800008000\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.merchantName").value("春风小铺"))
                .andExpect(jsonPath("$.data.invitationCode").value("invite-test"));
    }

    @Test
    void shouldExposeInvitationActivationEndpointWithoutSession() throws Exception {
        mockMvc.perform(post("/api/admin/v1/auth/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invitationCode\":\"invite-test\",\"password\":\"安全密码123\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "platform-admin", roles = "PLATFORM")
    void shouldEnableMerchantThroughPlatformApi() throws Exception {
        mockMvc.perform(post("/api/platform/v1/merchants/1001/enable"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectCatalogCreationFromPlatformDomain() throws Exception {
        mockMvc.perform(post("/api/admin/v1/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"桂花拿铁\",\"categoryName\":\"咖啡\",\"basePriceCent\":1800,\"onShelf\":true,\"skus\":[{\"specificationText\":\"中杯\",\"skuCode\":\"LATTE-M\",\"priceCent\":1800,\"initialStock\":10,\"warningStock\":2}]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldExposeMiniCatalogThroughScene() throws Exception {
        given(catalogPersistenceService.listOnShelfByScene("scene-test"))
                .willReturn(java.util.List.of(new CatalogPersistenceService.MiniProduct(1L, "桂花拿铁", "桂花香气", 1800, "咖啡",11L,20)));
        mockMvc.perform(get("/api/mini/v1/catalog/products").param("scene", "scene-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].productName").value("桂花拿铁"));
    }
}
