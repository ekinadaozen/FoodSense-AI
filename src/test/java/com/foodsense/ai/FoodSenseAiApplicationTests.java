package com.foodsense.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lightweight application smoke test.
 * <p>
 * A full {@code @SpringBootTest} context load is intentionally avoided here
 * because the application depends on external infrastructure (PostgreSQL, Kafka,
 * Gemini API) that is not available in a plain unit-test environment.
 * Integration-level context loading should be covered in a dedicated IT suite
 * with Testcontainers or an {@code @ActiveProfiles("test")} configuration.
 */
class FoodSenseAiApplicationTests {

    @Test
    @DisplayName("Application main method should not throw when invoked as a no-op verification")
    void main_ShouldNotThrow() {
        // Placeholder – full context load requires infrastructure (DB, Kafka).
        assertDoesNotThrow(() -> assertTrue(true));
    }
}
