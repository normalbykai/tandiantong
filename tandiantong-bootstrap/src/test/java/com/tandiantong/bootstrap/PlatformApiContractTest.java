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
}
