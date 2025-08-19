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
- None (this is a producer-only service)

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