# Distributed API Gateway

A production-style **distributed API Gateway and Microservices system** built using Spring Boot and Spring Cloud.

The project demonstrates how an API Gateway can handle authentication, service discovery, load balancing, resilience, rate limiting, centralized configuration, and communication between multiple microservice instances.

---

##  Project Overview

This project is designed around a distributed microservices architecture where the API Gateway acts as the single entry point for clients.

Instead of clients directly communicating with individual services, requests first reach the Gateway.

The Gateway is responsible for:

- API Key Authentication
- Request Routing
- Service Discovery
- Client-side Load Balancing
- Retry handling
- Circuit Breaker
- Fallback handling
- Redis-based Rate Limiting
- Centralized Configuration

The system currently contains:

- API Gateway
- User Service
- Eureka Service Registry
- Spring Cloud Config Server
- Git-based Centralized Configuration Repository
- Redis
- MySQL

---

# Architecture

```text
                              ┌─────────────────────────┐
                              │    Git Config Repo      │
                              │                         │
                              │ gateway-service.yml     │
                              │ user-service.yml        │
                              │ service-registry.yml    │
                              └────────────┬────────────┘
                                           │
                                           ↓
                              ┌─────────────────────────┐
                              │    Config Server        │
                              │        :8888            │
                              └────────────┬────────────┘
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
                    ↓                      ↓                      ↓
          ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
          │  API Gateway    │    │   User Service  │    │ Service Registry│
          │     :8080       │    │   :8081/:8082   │    │      :8761      │
          └────────┬────────┘    └────────┬────────┘    └─────────────────┘
                   │                      │
                   │                      │
                   ↓                      ↓
          ┌─────────────────┐       ┌──────────────┐
          │      Redis      │       │    MySQL     │
          │ Rate Limiting   │       │ User Database│
          └─────────────────┘       └──────────────┘
```

---

# Complete Request Flow

A normal request follows this flow:

```text
Client
   │
   ↓
API Gateway :8080
   │
   ├── Request Logging
   │
   ├── API Key Authentication
   │
   ├── Redis Rate Limiting
   │
   ├── Retry
   │
   ├── Circuit Breaker
   │
   ↓
lb://user-service
   │
   ↓
Eureka Service Registry
   │
   ├── User Service :8081
   │
   └── User Service :8082
   │
   ↓
User Service
   │
   ↓
MySQL
   │
   ↓
Response
   │
   ↓
API Gateway
   │
   ↓
Client
```

---

# Project Structure

```text
distributed-api-gateway/
│
├── gateway-service/
│   ├── src/
│   ├── pom.xml
│   └── application.properties
│
├── user-service/
│   ├── src/
│   ├── pom.xml
│   └── application.properties
│
├── service-registry/
│   ├── src/
│   ├── pom.xml
│   └── application.properties
│
├── service-config-server/
│   ├── src/
│   ├── pom.xml
│   └── application.properties
│
├── microservices-config/
│   ├── gateway-service.yml
│   ├── user-service.yml
│   └── service-registry.yml
│
└── README.md
```

---

# Tech Stack

## Backend

- Java
- Spring Boot
- Spring WebFlux
- Spring Cloud Gateway
- Spring Cloud Eureka
- Spring Cloud Config
- Spring Cloud LoadBalancer
- Spring Data JPA
- MySQL

## Resilience

- Resilience4j Retry
- Resilience4j Circuit Breaker
- TimeLimiter
- Fallback Handling

## Performance

- Redis
- Redis-based Rate Limiting

## Communication

- WebClient
- Reactive Programming

## Configuration

- Spring Cloud Config Server
- Git-based Configuration Repository

## Build Tool

- Maven

## Testing

- Postman

---

# API Gateway

The API Gateway runs on:

```text
http://localhost:8080
```

The Gateway acts as the single entry point for client requests.

Responsibilities:

- Receive client requests
- Validate API Keys
- Apply rate limiting
- Route requests
- Communicate with User Service
- Handle service failures
- Apply Retry and Circuit Breaker
- Return appropriate responses to clients

---

# API Key Authentication

The Gateway authenticates requests using the:

```http
X-API-KEY
```

header.

Example:

```http
X-API-KEY: your-api-key
```

Authentication flow:

```text
Client
   ↓
Gateway
   ↓
Read X-API-KEY
   ↓
WebClient
   ↓
User Service
   ↓
/internal/users/validate-api-key
   ↓
Validate API Key
```

If the API Key is missing:

```text
401 Unauthorized
```

If the API Key is invalid:

```text
401 Unauthorized
```

If the API Key is valid:

```text
Request continues
```

---

# WebClient

The Gateway communicates with User Service using Spring WebClient.

The project initially used Feign for service communication, but the authentication communication was migrated to a **reactive WebClient-based implementation**.

The WebClient is configured with Spring Cloud LoadBalancer support.

The logical service URL is:

```text
lb://user-service
```

This avoids directly hardcoding:

```text
http://localhost:8081
```

for service-to-service communication.

---

# Service Discovery

The project uses **Netflix Eureka** for service discovery.

Eureka Server runs on:

```text
http://localhost:8761
```

Services register themselves with Eureka.

Current services:

```text
GATEWAY-SERVICE
USER-SERVICE
SERVICE-REGISTRY
```

User Service is running with multiple instances:

```text
User Service → :8081
User Service → :8082
```

The Gateway discovers User Service using:

```text
lb://user-service
```

instead of directly depending on a fixed service address.

---

# Client-side Load Balancing

Because multiple User Service instances are available:

```text
USER-SERVICE
    ├── :8081
    └── :8082
```

Spring Cloud LoadBalancer can distribute requests between available instances.

Flow:

```text
Gateway
   ↓
lb://user-service
   ↓
Eureka
   ↓
Available User Service Instances
   ├── :8081
   └── :8082
```

This provides basic horizontal scaling.

---

# Retry

The Gateway uses Resilience4j Retry for transient failures while communicating with User Service.

Current configuration includes:

```text
Maximum attempts = 3
Wait duration   = 500ms
```

The purpose of Retry is to give a temporarily failing service another chance before considering the request failed.

Conceptually:

```text
Request
   ↓
User Service
   ↓
Failure
   ↓
Retry
   ↓
User Service
   ↓
Success / Failure
```

Retry is particularly useful for temporary network or service-level failures.

---

# Circuit Breaker

Resilience4j Circuit Breaker is used to prevent continuous requests to an unhealthy User Service.

The Circuit Breaker has three major states:

```text
CLOSED
   ↓
OPEN
   ↓
HALF_OPEN
   ↓
CLOSED
```

### CLOSED

Requests are allowed normally.

Failures are monitored.

### OPEN

If the failure threshold is reached, the circuit opens.

Requests are prevented from continuously hitting the failing service.

### HALF_OPEN

After the configured wait duration, a limited number of test calls are allowed.

If the service has recovered:

```text
HALF_OPEN → CLOSED
```

Otherwise:

```text
HALF_OPEN → OPEN
```

---

# Fallback Handling

When the authentication service becomes unavailable, the Gateway returns a structured `503 Service Unavailable` response.

Example:

```json
{
  "status": 503,
  "message": "Authentication service is unavailable"
}
```

This is handled through the Gateway's global reactive error handling.

Flow:

```text
Gateway
   ↓
WebClient
   ↓
User Service unavailable
   ↓
Retry
   ↓
Circuit Breaker
   ↓
Fallback
   ↓
Global Error Handler
   ↓
503 Service Unavailable
```

---

# Redis Rate Limiting

Redis is used for Gateway rate limiting.

Redis runs on:

```text
localhost:6379
```

Spring Cloud Gateway's `RedisRateLimiter` is used.

Current testing configuration:

```text
replenishRate = 2
burstCapacity = 4
```

The configuration is intentionally small for testing.

---

# API-Key Based Rate Limiting

The rate limiter uses a custom `KeyResolver`.

The API Key from:

```http
X-API-KEY
```

is used as the rate-limiting key.

Example:

```text
API Key A
    ↓
Redis Bucket A

API Key B
    ↓
Redis Bucket B
```

Therefore different API Keys maintain independent rate-limit state.

Example:

```text
ABC123 → independent limit
XYZ789 → independent limit
```

This prevents one API Key from consuming another API Key's rate-limit quota.

---

# User Service

User Service runs on multiple instances:

```text
localhost:8081
localhost:8082
```

Its primary responsibilities include:

- User creation
- API Key generation
- API Key validation
- User data persistence

---

#  User API

## Create User

```http
POST /users
```

Request example:

```json
{
  "name": "Dev",
  "email": "dev@example.com"
}
```

Successful response:

```text
201 Created
```

The service generates an API Key for the user.

---

# Internal API Key Validation

User Service exposes an internal endpoint:

```http
GET /internal/users/validate-api-key
```

with:

```http
X-API-KEY: your-api-key
```

The Gateway uses this endpoint to validate API Keys.

The validation is handled internally by:

```text
UserController
      +
InternalAuthController
      ↓
UserService
      ↓
UserRepository
      ↓
MySQL
```

---

# Database

User Service uses MySQL.

Database:

```text
user_service_gateway_db
```

The User entity is persisted using Spring Data JPA.

Repository operations include:

```text
findByEmail()
findByApiKey()
```

The API Key is generated using UUID.

---

# Centralized Configuration

The project uses Spring Cloud Config Server.

Config Server runs on:

```text
http://localhost:8888
```

Instead of keeping all service configuration inside every application's local configuration file, configuration is maintained in a dedicated Git repository.

Repository structure:

```text
microservices-config/
│
├── gateway-service.yml
├── user-service.yml
└── service-registry.yml
```

Architecture:

```text
Git Repository
      ↓
Config Server
      ↓
Services
```

---

# Configuration Files

## gateway-service.yml

Contains Gateway-related configuration such as:

- Server configuration
- Redis configuration
- Rate limiting configuration
- Eureka configuration
- Circuit Breaker configuration
- Retry configuration
- TimeLimiter configuration
- Logging configuration

## user-service.yml

Contains User Service configuration such as:

- MySQL configuration
- JPA configuration
- Eureka configuration

Instance-specific ports can remain outside centralized configuration when multiple local instances use different ports.

## service-registry.yml

Contains Eureka Server configuration such as:

```text
Application Name
Server Port
```

---

# Configuration Startup Flow

A service starts with its basic identity and Config Server location.

Example:

```properties
spring.application.name=gateway-service
spring.config.import=optional:configserver:http://localhost:8888
```

Then:

```text
Gateway starts
      ↓
Config Client
      ↓
Config Server
      ↓
Git repository
      ↓
gateway-service.yml
      ↓
Gateway receives configuration
```

The same architecture is used for User Service and Service Registry.

---

# Testing

The project has been tested using Postman and local multi-instance execution.

## Authentication Tests

| Scenario | Expected Result |
|---|---|
| Valid API Key | 200 |
| Missing API Key | 401 |
| Invalid API Key | 401 |

## Rate Limiting Tests

| Scenario | Expected Result |
|---|---|
| Requests within limit | Allowed |
| Limit exceeded | 429 |
| Different API Key | Independent rate-limit bucket |

## Resilience Tests

| Scenario | Expected Result |
|---|---|
| User Service available | Successful response |
| Temporary failure | Retry |
| Repeated service failure | Circuit Breaker |
| Authentication service unavailable | 503 |

## Service Discovery Tests

```text
User Service :8081 → UP
User Service :8082 → UP
Gateway → UP
Service Registry → UP
```

## Configuration Tests

Configuration was successfully loaded from the centralized Config Server for:

```text
Gateway
User Service
Service Registry
```

---

# Important HTTP Responses

```text
200 OK
```

Request successfully processed.

```text
201 Created
```

User successfully created.

```text
401 Unauthorized
```

API Key is missing or invalid.

```text
429 Too Many Requests
```

Rate limit exceeded.

```text
503 Service Unavailable
```

Authentication/User Service is unavailable and the fallback mechanism is triggered.

---

# How to Run Locally

## Prerequisites

Make sure the following are available:

```text
Java
Maven
MySQL
Docker
Redis
```

---

## Step 1 — Start Redis

Redis runs on:

```text
localhost:6379
```

Example Docker command:

```bash
docker run -d --name redis -p 6379:6379 redis:7
```

---

## Step 2 — Start Config Server

Start:

```text
service-config-server
```

Port:

```text
8888
```

---

## Step 3 — Start Service Registry

Start:

```text
service-registry
```

Port:

```text
8761
```

Eureka Dashboard:

```text
http://localhost:8761
```

---

## Step 4 — Start User Service

Start the first instance:

```text
User Service :8081
```

Start the second instance:

```text
User Service :8082
```

Both instances should register with Eureka.

---

## Step 5 — Start Gateway

Start:

```text
gateway-service
```

Port:

```text
8080
```

---

# Important URLs

| Service | URL |
|---|---|
| API Gateway | `http://localhost:8080` |
| Eureka | `http://localhost:8761` |
| Config Server | `http://localhost:8888` |
| User Service Instance 1 | `http://localhost:8081` |
| User Service Instance 2 | `http://localhost:8082` |
| Redis | `localhost:6379` |

---

# Config Server Verification

Gateway configuration can be checked using:

```text
http://localhost:8888/gateway-service/default
```

User Service configuration:

```text
http://localhost:8888/user-service/default
```

Service Registry configuration:

```text
http://localhost:8888/service-registry/default
```

These endpoints allow verification that Config Server is successfully reading configuration from the Git repository.

---

# Important Design Decisions

## Why API Gateway?

The Gateway provides a single entry point for clients and centralizes cross-cutting concerns such as:

- Authentication
- Rate Limiting
- Routing
- Resilience
- Service Discovery

---

## Why Eureka?

Eureka allows services to discover each other dynamically instead of relying on hardcoded service addresses.

Instead of:

```text
http://localhost:8081
```

the Gateway uses:

```text
lb://user-service
```

---

## Why WebClient?

WebClient fits naturally with Spring WebFlux and the reactive Gateway architecture.

It allows non-blocking HTTP communication with downstream services.

---

## Why Redis?

Redis provides a fast shared data store for maintaining rate-limit state.

This also allows rate limiting to work across multiple Gateway instances when they share the same Redis backend.

---

## Why Circuit Breaker?

Circuit Breaker prevents continuous calls to an unhealthy downstream service and provides a controlled failure path.

---

## Why Retry?

Retry handles temporary failures where a subsequent attempt may succeed.

Retry and Circuit Breaker solve different problems and are used together carefully.

---

## Why Config Server?

Centralized configuration avoids maintaining the same configuration separately inside every microservice.

It also provides a cleaner configuration management architecture as the number of services increases.

---

# Current Architecture Summary

```text
                         ┌───────────────────────┐
                         │    Git Config Repo    │
                         │                       │
                         │ gateway-service.yml   │
                         │ user-service.yml      │
                         │ service-registry.yml  │
                         └───────────┬───────────┘
                                     │
                                     ↓
                         ┌───────────────────────┐
                         │     Config Server     │
                         │        :8888          │
                         └───────────┬───────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
              ↓                      ↓                      ↓
       ┌──────────────┐       ┌──────────────┐       ┌──────────────┐
       │ API Gateway  │       │ User Service │       │    Eureka    │
       │    :8080     │       │ :8081/:8082  │       │    :8761     │
       └──────┬───────┘       └──────┬───────┘       └──────────────┘
              │                      │
       ┌──────┼───────┐              │
       │      │       │              ↓
       ↓      ↓       ↓           ┌───────┐
     Redis   Retry   Circuit       │ MySQL │
             Breaker               └───────┘
       │
       ↓
 Rate Limiting
```

---

# Project Goals Achieved

This project demonstrates practical implementation of:

- Microservice architecture
- API Gateway pattern
- Service Discovery
- Client-side Load Balancing
- Reactive programming
- WebClient
- API Key Authentication
- Distributed Rate Limiting
- Redis
- Retry pattern
- Circuit Breaker pattern
- Fallback handling
- Centralized Configuration
- Git-based configuration management
- Multiple service instances
- Database persistence
- Failure handling
- Service-to-service communication

---

# Final Status

```text
API Gateway                 ✅
User Service                ✅
Eureka Service Discovery    ✅
Multiple Instances          ✅
Load Balancing              ✅
WebClient                   ✅
API Key Authentication      ✅
Retry                       ✅
Circuit Breaker             ✅
Fallback Handling           ✅
Redis Rate Limiting         ✅
API-Key Rate Limiting       ✅
Config Server               ✅
Git Config Repository       ✅
Centralized Configuration   ✅
End-to-End Testing          ✅
```

---

# Possible Future Improvements

The current project is complete as a core distributed API Gateway system.

Possible future improvements include:

- Spring Boot Actuator
- Prometheus
- Grafana
- Distributed Tracing
- Correlation IDs
- Structured Logging
- Secret Management
- Flyway/Liquibase
- Automated Integration Testing
- Testcontainers
- Docker Compose for the complete system
- CI/CD with GitHub Actions
- Production deployment

These are intentionally kept outside the current implementation so that the core architecture remains focused and understandable.

---

#  Project Focus
The main objective of this project was not simply to create multiple Spring Boot services.
The project focuses on understanding how a distributed system behaves when services:

- communicate with each other,
- discover each other dynamically,
- scale to multiple instances,
- experience temporary failures,
- become unavailable,
- require authentication,
- need request protection,
- and require centralized configuration.

The final system therefore combines **API Gateway, Service Discovery, Load Balancing, Resilience, Rate Limiting, Reactive Communication, and Centralized Configuration** into one practical microservices architecture.
