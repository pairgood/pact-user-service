package com.ecommerce.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class OpenApiConfigTest {

    @Autowired
    private OpenApiConfig openApiConfig;

    @Test
    void contextLoads() {
        // Test that OpenApiConfig can be loaded
        assertThat(openApiConfig).isNotNull();
    }

    @Test
    void userServiceOpenAPI_ShouldBeConfigured() {
        // Test that OpenAPI bean is created and configured
        OpenAPI openAPI = openApiConfig.userServiceOpenAPI();
        
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("User Service API");
        assertThat(openAPI.getInfo().getDescription()).contains("user management");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0");
        assertThat(openAPI.getServers()).isNotEmpty();
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("http://localhost:8081");
    }
}