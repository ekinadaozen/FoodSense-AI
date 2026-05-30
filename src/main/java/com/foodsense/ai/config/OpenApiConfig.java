package com.foodsense.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for the FoodSense AI REST API.
 *
 * <p>Provides metadata displayed on the Swagger UI, including the API title,
 * description, version, and contact information.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the {@link OpenAPI} specification bean with project metadata.
     *
     * @return a configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI foodSenseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FoodSense AI API")
                        .description("Food Delivery Customer Feedback Analysis System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FoodSense AI Team")));
    }
}
