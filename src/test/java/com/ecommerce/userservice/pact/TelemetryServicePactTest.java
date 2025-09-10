package com.ecommerce.userservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.ecommerce.userservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "telemetry-service")
class TelemetryServicePactTest {

    private TelemetryClient telemetryClient;

    @BeforeEach
    void setUp(MockServer mockServer) {
        telemetryClient = new TelemetryClient();
        // Set the telemetry service URL to the mock server
        ReflectionTestUtils.setField(telemetryClient, "telemetryServiceUrl", mockServer.getUrl());
        ReflectionTestUtils.setField(telemetryClient, "serviceName", "user-service");
    }

    @Pact(consumer = "user-service")
    public V4Pact startTraceEventPact(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a start trace telemetry event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.stringMatcher("traceId", "trace_[a-f0-9]{32}", "trace_12345678901234567890123456789012");
                body.stringMatcher("spanId", "span_[a-z0-9]+", "span_abcdef123456");
                body.stringValue("serviceName", "user-service");
                body.stringValue("operation", "createUser");
                body.stringValue("eventType", "SPAN");
                body.array("timestamp", array -> {
                    array.numberType(2025);
                    array.numberType(9);
                    array.numberType(10);
                    array.numberType(19);
                    array.numberType(45);
                    array.numberType(30);
                    array.numberType(123456789);
                });
                body.stringValue("status", "SUCCESS");
                body.stringValue("httpMethod", "POST");
                body.stringValue("httpUrl", "/api/users");
                body.stringValue("userId", "user123");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "user-service")
    public V4Pact finishTraceEventPact(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a finish trace telemetry event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.stringMatcher("traceId", "trace_[a-f0-9]{32}", "trace_12345678901234567890123456789012");
                body.stringMatcher("spanId", "span_[a-z0-9]+", "span_abcdef123456");
                body.stringValue("serviceName", "user-service");
                body.stringValue("operation", "createUser_complete");
                body.stringValue("eventType", "SPAN");
                body.array("timestamp", array -> {
                    array.numberType(2025);
                    array.numberType(9);
                    array.numberType(10);
                    array.numberType(19);
                    array.numberType(45);
                    array.numberType(30);
                    array.numberType(123456789);
                });
                body.numberType("durationMs");
                body.stringValue("status", "SUCCESS");
                body.numberValue("httpStatusCode", 201);
                body.stringValue("errorMessage", "");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "user-service")
    public V4Pact recordServiceCallEventPact(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a service call telemetry event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.stringMatcher("traceId", "trace_[a-f0-9]{32}", "trace_12345678901234567890123456789012");
                body.stringMatcher("spanId", "span_[a-z0-9]+", "span_abcdef123456");
                body.stringMatcher("parentSpanId", "span_[a-z0-9]+", "span_parent123456");
                body.stringValue("serviceName", "user-service");
                body.stringValue("operation", "email-service_sendWelcomeEmail");
                body.stringValue("eventType", "SPAN");
                body.array("timestamp", array -> {
                    array.numberType(2025);
                    array.numberType(9);
                    array.numberType(10);
                    array.numberType(19);
                    array.numberType(45);
                    array.numberType(30);
                    array.numberType(123456789);
                });
                body.numberValue("durationMs", 150);
                body.stringValue("status", "SUCCESS");
                body.stringValue("httpMethod", "POST");
                body.stringValue("httpUrl", "/api/emails/welcome");
                body.numberValue("httpStatusCode", 200);
                body.stringValue("metadata", "Outbound call to email-service");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact(V4Pact.class);
    }

    @Pact(consumer = "user-service")
    public V4Pact logEventPact(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a log telemetry event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.stringMatcher("traceId", "trace_[a-f0-9]{32}", "trace_12345678901234567890123456789012");
                body.stringMatcher("spanId", "span_[a-z0-9]+", "span_abcdef123456");
                body.stringValue("serviceName", "user-service");
                body.stringValue("operation", "log_info");
                body.stringValue("eventType", "LOG");
                body.array("timestamp", array -> {
                    array.numberType(2025);
                    array.numberType(9);
                    array.numberType(10);
                    array.numberType(19);
                    array.numberType(45);
                    array.numberType(30);
                    array.numberType(123456789);
                });
                body.stringValue("status", "SUCCESS");
                body.stringValue("metadata", "User created successfully");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "startTraceEventPact")
    void testStartTraceEvent() throws InterruptedException {
        // Given - setup trace context first
        TelemetryClient.TraceContext.clear();
        
        // When - start a trace
        String traceId = telemetryClient.startTrace("createUser", "POST", "/api/users", "user123");
        
        // Allow time for async WebClient call to complete
        Thread.sleep(500);
        
        // Then - verify trace context is set
        assert traceId != null;
        assert traceId.startsWith("trace_");
        assert TelemetryClient.TraceContext.getTraceId() != null;
        assert TelemetryClient.TraceContext.getSpanId() != null;
    }

    @Test
    @PactTestFor(pactMethod = "finishTraceEventPact")
    void testFinishTraceEvent() throws InterruptedException {
        // Given - setup trace context
        TelemetryClient.TraceContext.setTraceId("trace_12345678901234567890123456789012");
        TelemetryClient.TraceContext.setSpanId("span_abcdef123456");
        TelemetryClient.TraceContext.setStartTime(System.currentTimeMillis() - 100);
        
        // When - finish the trace
        telemetryClient.finishTrace("createUser", 201, null);
        
        // Allow time for async WebClient call to complete
        Thread.sleep(100);
        
        // Then - verify trace context is cleared
        assert TelemetryClient.TraceContext.getTraceId() == null;
        assert TelemetryClient.TraceContext.getSpanId() == null;
    }

    @Test
    @PactTestFor(pactMethod = "recordServiceCallEventPact")
    void testRecordServiceCallEvent() throws InterruptedException {
        // Given - setup trace context
        TelemetryClient.TraceContext.setTraceId("trace_12345678901234567890123456789012");
        TelemetryClient.TraceContext.setSpanId("span_parent123456");
        
        // When - record a service call
        telemetryClient.recordServiceCall("email-service", "sendWelcomeEmail", "POST", "/api/emails/welcome", 150, 200);
        
        // Allow time for async WebClient call to complete
        Thread.sleep(100);
        
        // Then - the contract interaction should be satisfied
        // Pact will verify the HTTP call was made correctly
    }

    @Test
    @PactTestFor(pactMethod = "logEventPact")
    void testLogEvent() throws InterruptedException {
        // Given - setup trace context
        TelemetryClient.TraceContext.setTraceId("trace_12345678901234567890123456789012");
        TelemetryClient.TraceContext.setSpanId("span_abcdef123456");
        
        // When - log an event
        telemetryClient.logEvent("User created successfully", "INFO");
        
        // Allow time for async WebClient call to complete
        Thread.sleep(100);
        
        // Then - the contract interaction should be satisfied
        // Pact will verify the HTTP call was made correctly
    }
}