package com.tandiantong.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.tandiantong.analytics.app.AnalyticsPersistenceService;
import com.tandiantong.catalog.product.CatalogPersistenceService;
import com.tandiantong.order.app.PersistentOrderService;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.security.auth.DatabaseAuthenticationService;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import com.tandiantong.verification.app.VerificationPersistenceService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    private static final List<String> HTTP_METHODS = List.of("get", "post", "put", "delete", "patch");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private MerchantProvisioningService merchantProvisioningService;
    @MockBean private DatabaseAuthenticationService databaseAuthenticationService;
    @MockBean private CatalogPersistenceService catalogPersistenceService;
    @MockBean private PersistentOrderService persistentOrderService;
    @MockBean private AnalyticsPersistenceService analyticsPersistenceService;
    @MockBean private ReservationPersistenceService reservationPersistenceService;
    @MockBean private VerificationPersistenceService verificationPersistenceService;

    @Test
    void shouldExposeChineseDescriptionsForEveryBusinessOperationAndSchemaProperty() throws Exception {
        String content = mockMvc.perform(get("/v3/api-docs").characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode document = objectMapper.readTree(content);

        document.path("paths").properties().forEach(path -> HTTP_METHODS.forEach(method -> {
            JsonNode operation = path.getValue().path(method);
            if (!operation.isMissingNode()) {
                assertChinese(operation.path("summary").asText(), path.getKey() + " 的接口摘要");
                assertChinese(operation.path("description").asText(), path.getKey() + " 的接口说明");
                operation.path("parameters").forEach(parameter ->
                        assertChinese(parameter.path("description").asText(), path.getKey() + " 的参数说明"));
            }
        }));

        document.path("components").path("schemas").properties().forEach(schema -> {
            assertChinese(schema.getValue().path("description").asText(), schema.getKey() + " 的模型说明");
            schema.getValue().path("properties").properties().forEach(property -> {
                if (!property.getValue().has("$ref")) {
                    assertChinese(property.getValue().path("description").asText(),
                            schema.getKey() + "." + property.getKey() + " 的字段说明");
                }
            });
        });
    }

    @Test
    void shouldDocumentOrderRequestAndResponseFieldsInChinese() throws Exception {
        String content = mockMvc.perform(get("/v3/api-docs").characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode schemas = objectMapper.readTree(content).path("components").path("schemas");

        assertThat(schemas.path("CreateOrderRequest").path("properties").path("sceneKey").path("description").asText())
                .contains("商户小程序入口码");
        assertThat(schemas.path("OrderResponse").path("properties").path("orderNo").path("description").asText())
                .contains("平台商品订单号");
        assertThat(schemas.path("OrderResponse").path("properties").path("verificationToken").path("description").asText())
                .contains("安全核销令牌");
    }

    private void assertChinese(String value, String subject) {
        assertThat(value).as(subject).isNotBlank().containsPattern("[\\u4e00-\\u9fa5]");
    }
}
