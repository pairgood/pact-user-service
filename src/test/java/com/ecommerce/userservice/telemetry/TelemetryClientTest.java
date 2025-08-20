package com.ecommerce.userservice.telemetry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TelemetryClientTest {

    private TelemetryClient telemetryClient;

    @BeforeEach
    void setUp() {
        telemetryClient = new TelemetryClient();
        ReflectionTestUtils.setField(telemetryClient, "telemetryServiceUrl", "http://localhost:8086");
        ReflectionTestUtils.setField(telemetryClient, "serviceName", "user-service");
        
        // Clear any existing trace context
        TelemetryClient.TraceContext.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up trace context after each test
        TelemetryClient.TraceContext.clear();
    }

    @Test
    void startTrace_ShouldReturnTraceId() {
        // When
        String traceId = telemetryClient.startTrace("test_operation", "GET", "http://localhost/test", "user123");

        // Then
        assertThat(traceId).isNotNull();
        assertThat(traceId).startsWith("trace_");
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo(traceId);
        assertThat(TelemetryClient.TraceContext.getSpanId()).isNotNull();
        assertThat(TelemetryClient.TraceContext.getStartTime()).isNotNull();
    }

    @Test
    void startTrace_WithNullUserId_ShouldWork() {
        // When
        String traceId = telemetryClient.startTrace("test_operation", "GET", "http://localhost/test", null);

        // Then
        assertThat(traceId).isNotNull();
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo(traceId);
    }

    @Test
    void finishTrace_WithActiveTrace_ShouldClearContext() {
        // Given
        String traceId = telemetryClient.startTrace("test_operation", "GET", "http://localhost/test", "user123");
        
        // When
        telemetryClient.finishTrace("test_operation", 200, null);

        // Then
        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
        assertThat(TelemetryClient.TraceContext.getSpanId()).isNull();
        assertThat(TelemetryClient.TraceContext.getStartTime()).isNull();
    }

    @Test
    void finishTrace_WithError_ShouldWork() {
        // Given
        telemetryClient.startTrace("test_operation", "GET", "http://localhost/test", "user123");
        
        // When
        telemetryClient.finishTrace("test_operation", 500, "Internal server error");

        // Then
        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
    }

    @Test
    void finishTrace_WithoutActiveTrace_ShouldNotFail() {
        // When & Then (should not throw exception)
        telemetryClient.finishTrace("test_operation", 200, null);
    }

    @Test
    void recordServiceCall_WithActiveTrace_ShouldWork() {
        // Given
        telemetryClient.startTrace("parent_operation", "GET", "http://localhost/test", "user123");
        
        // When
        telemetryClient.recordServiceCall("target-service", "get_data", "GET", "http://target/api", 150L, 200);

        // Then - Should not throw exception and trace context should remain
        assertThat(TelemetryClient.TraceContext.getTraceId()).isNotNull();
    }

    @Test
    void recordServiceCall_WithoutActiveTrace_ShouldNotFail() {
        // When & Then (should not throw exception)
        telemetryClient.recordServiceCall("target-service", "get_data", "GET", "http://target/api", 150L, 200);
    }

    @Test
    void logEvent_WithActiveTrace_ShouldWork() {
        // Given
        telemetryClient.startTrace("test_operation", "GET", "http://localhost/test", "user123");
        
        // When
        telemetryClient.logEvent("Test log message", "INFO");

        // Then - Should not throw exception and trace context should remain
        assertThat(TelemetryClient.TraceContext.getTraceId()).isNotNull();
    }

    @Test
    void logEvent_WithoutActiveTrace_ShouldNotFail() {
        // When & Then (should not throw exception)
        telemetryClient.logEvent("Test log message", "ERROR");
    }

    @Test
    void traceContext_ShouldWorkCorrectly() {
        // Test setting and getting trace context
        TelemetryClient.TraceContext.setTraceId("test-trace-123");
        TelemetryClient.TraceContext.setSpanId("test-span-456");
        TelemetryClient.TraceContext.setStartTime(12345L);

        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo("test-trace-123");
        assertThat(TelemetryClient.TraceContext.getSpanId()).isEqualTo("test-span-456");
        assertThat(TelemetryClient.TraceContext.getStartTime()).isEqualTo(12345L);

        // Test clear
        TelemetryClient.TraceContext.clear();
        assertThat(TelemetryClient.TraceContext.getTraceId()).isNull();
        assertThat(TelemetryClient.TraceContext.getSpanId()).isNull();
        assertThat(TelemetryClient.TraceContext.getStartTime()).isNull();
    }

    @Test
    void traceContext_propagate_ShouldSetTraceAndSpan() {
        // When
        TelemetryClient.TraceContext.propagate("propagated-trace", "propagated-span");

        // Then
        assertThat(TelemetryClient.TraceContext.getTraceId()).isEqualTo("propagated-trace");
        assertThat(TelemetryClient.TraceContext.getSpanId()).isEqualTo("propagated-span");
    }
}