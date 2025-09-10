# User Service

> **🔵 This service is highlighted in the architecture diagram below**

Authentication and user profile management service for the e-commerce microservices ecosystem.

## Service Role: Producer Only
This service provides user authentication and profile data to other services but does not consume external APIs.

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐
│ 🔵 User Service │    │ Product Service │
│   (Port 8081)   │    │   (Port 8082)   │
│                 │    │                 │
│ • Authentication│    │ • Product Catalog│
│ • User Profiles │    │ • Inventory Mgmt│
│ • JWT Tokens    │    │ • Pricing       │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          │ validates users      │ fetches products
          │                      │
          ▼                      ▼
    ┌─────────────────────────────────┐
    │        Order Service            │
    │        (Port 8083)              │
    │                                 │
    │ • Order Management              │
    │ • Order Processing              │
    │ • Consumes User & Product APIs  │
    └─────────────┬───────────────────┘
                  │
                  │ triggers payment
                  │
                  ▼
    ┌─────────────────────────────────┐
    │       Payment Service           │
    │       (Port 8084)               │
    │                                 │
    │ • Payment Processing            │
    │ • Gateway Integration           │
    │ • Refund Management             │
    └─────────────┬───────────────────┘
                  │
                  │ sends notifications
                  │
                  ▼
    ┌─────────────────────────────────┐
    │    Notification Service         │
    │       (Port 8085)               │
    │                                 │
    │ • Email Notifications           │
    │ • SMS Notifications             │
    │ • Order & Payment Updates       │
    └─────────────────────────────────┘
                  │
                  │ All services send telemetry data
                  │
                  ▼
    ┌─────────────────────────────────┐
    │📊  Telemetry Service            │
    │       (Port 8086)               │
    │                                 │
    │ • Distributed Tracing           │
    │ • Service Metrics               │
    │ • Request Tracking              │
    │ • Performance Monitoring        │
    └─────────────────────────────────┘
```

## Features

- **User Registration**: Create new user accounts with validation
- **Authentication**: JWT-based authentication system
- **User Profiles**: Complete user profile management
- **Token Validation**: Secure token validation for other services
- **Password Security**: BCrypt password hashing

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security with JWT
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **Java Version**: 17

## API Endpoints

### Authentication
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User login (returns JWT token)
- `GET /api/users/validate/{token}` - Validate JWT token

### User Management  
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users
- `PUT /api/users/{id}` - Update user profile

## Running the Service

### Prerequisites
- Java 17+
- Gradle (or use included Gradle wrapper)

### Start the Service
```bash
./gradlew bootRun
```

The service will start on **port 8081**.

### Database Access
- **H2 Console**: http://localhost:8081/h2-console
- **JDBC URL**: `jdbc:h2:mem:userdb`
- **Username**: `sa`
- **Password**: (empty)

## Service Dependencies

### Consumers of This Service
- **Order Service**: Validates user existence during order creation

### External Dependencies
- **Telemetry Service**: Sends telemetry data and trace information

## Example Usage

### Register a User
```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

### Get User Details
```bash
curl -X GET http://localhost:8081/api/users/1
```

## Telemetry Integration

This service automatically sends telemetry data to the **[Telemetry Service](../telemetry-service/README.md)** for distributed tracing and monitoring.

### Telemetry Features
- **Request Tracing**: All API endpoints are instrumented with trace collection
- **Service Metrics**: Performance metrics for user operations
- **Error Tracking**: Automatic error capture and reporting
- **Inter-Service Calls**: Tracks calls made by dependent services

### Traced Operations
- User registration (`register_user`)
- User login (`login_user`)  
- User profile retrieval (`get_user`)
- Password updates and profile modifications

### Telemetry Configuration
The service connects to the telemetry service using:
```properties
telemetry.service.url=http://localhost:8086
```

## Related Services

- **[Order Service](../order-service/README.md)**: Consumes this service for user validation
- **[Product Service](../product-service/README.md)**: Independent service
- **[Payment Service](../payment-service/README.md)**: Independent service  
- **[Notification Service](../notification-service/README.md)**: Independent service
- **[Telemetry Service](../telemetry-service/README.md)**: Collects telemetry data from this service

## Pact Contract Testing

This service uses [Pact](https://pact.io/) for consumer contract testing to ensure reliable communication with external services.

### Consumer Role

This service acts as a consumer for the following external services:
- **Telemetry Service**: Sends telemetry events via HTTP POST to `/api/telemetry/events`

### Running Pact Tests

#### Consumer Tests
```bash
# Run consumer tests and generate contracts
./gradlew pactTest

# Generated contracts will be in build/pacts/
```

#### Publishing Contracts
```bash
# Publish contracts to Pactflow
./gradlew pactPublish
```

### Contract Testing Approach

This implementation follows Pact's **"Be conservative in what you send"** principle:

- Consumer tests define minimal request structures with only required fields
- Request bodies cannot contain fields not defined in the contract
- Tests validate that actual API calls match contract expectations exactly
- Mock servers reject requests with unexpected extra fields

### Contract Files

Consumer contracts are generated in:
- `build/pacts/` - Local contract files  
- Pactflow - Centralized contract storage and management

### Troubleshooting

#### Common Issues

1. **Consumer Test Failures**
   - **Extra fields in request**: Remove any fields from request body that aren't actually needed
   - **Mock server expectation mismatch**: Verify HTTP method, path, headers, and body structure
   - **Content-Type headers**: Ensure request headers match exactly what the service sends
   - **URL path parameters**: Check that path parameters are correctly formatted in the contract

2. **Contract Generation Issues**
   - **Missing @Pact annotation**: Ensure each contract method has proper annotations
   - **Invalid JSON structure**: Verify LambdaDsl body definitions match actual data structures
   - **Provider state setup**: Ensure provider state descriptions are descriptive and specific

3. **Pactflow Integration Issues**
   - **Authentication**: Verify `PACT_BROKER_TOKEN` environment variable is set
   - **Base URL**: Confirm `PACT_BROKER_BASE_URL` points to `https://pairgood.pactflow.io`
   - **Network connectivity**: Check firewall/proxy settings if publishing fails

#### Debug Commands

```bash
# Run with debug output
./gradlew pactTest --info --debug

# Run specific test class
./gradlew pactTest --tests="*TelemetryServicePactTest*"

# Generate contracts without publishing
./gradlew pactTest -x pactPublish

# Clean and regenerate contracts
./gradlew clean pactTest
```

#### Debug Logging

Add to `application-test.properties` for detailed Pact logging:
```properties
logging.level.au.com.dius.pact=DEBUG
logging.level.org.apache.http=DEBUG
```

### Contract Evolution

When external services change their APIs:

1. **New Fields in Responses**: No action needed - consumers ignore extra fields
2. **Removed Response Fields**: Update consumer tests if those fields were being used
3. **New Required Request Fields**: Update consumer tests and service code
4. **Changed Endpoints**: Update consumer contract paths and service client code

### Integration with CI/CD

Consumer contract tests run automatically on:
- **Pull Requests**: Generate and validate contracts
- **Main Branch**: Publish contracts to Pactflow for provider verification
- **Feature Branches**: Generate contracts for validation (not published)

### Manual Testing

For local development against real services:
```bash
# Test against local services (disable Pact)
./gradlew test -Dpact.verifier.disabled=true

# Test against staging services
export EXTERNAL_SERVICE_URL=https://staging.example.com
./gradlew test -Dpact.verifier.disabled=true
```

### Contract Documentation

Generated contracts document:
- **API interactions**: What endpoints this service calls
- **Request formats**: Exact structure of requests sent
- **Response expectations**: What fields this service relies on
- **Error handling**: How this service handles different response scenarios

### Implementation Status

**Current Status**: Infrastructure setup complete
- ✅ Pact dependencies added to build.gradle
- ✅ Gradle tasks configured (pactTest, pactPublish)
- ✅ CI/CD pipeline updated with Pact steps
- ✅ Basic test structure in place

**Next Steps**: Implement actual consumer contracts
- 🔲 Consumer tests for TelemetryClient.startTrace()
- 🔲 Consumer tests for TelemetryClient.finishTrace()
- 🔲 Consumer tests for TelemetryClient.recordServiceCall()
- 🔲 Consumer tests for TelemetryClient.logEvent()

The placeholder tests are ready to be replaced with actual Pact consumer contract tests that verify the telemetry service API interactions.