# Distributed API Gateway

A production-style distributed API Gateway built using Spring Boot and Spring Cloud.

The project demonstrates API Gateway routing, service discovery, client-side load balancing,
API key authentication, resilience patterns, Redis-based rate limiting, and centralized
configuration.

## Architecture

Client
  |
  v
API Gateway :8080
  |
  +-- API Key Authentication
  |
  +-- Redis Rate Limiting
  |
  +-- Retry
  |
  +-- Circuit Breaker
  |
  v
Eureka Service Registry :8761
  |
  +-- User Service :8081
  |
  +-- User Service :8082


Config Server :8888
  |
  +-- Gateway Configuration
  +-- User Service Configuration
  +-- Service Registry Configuration

## Services

### API Gateway
Port: 8080

Responsibilities:
- Request routing
- API key authentication
- Rate limiting
- Retry handling
- Circuit breaker
- Fallback handling
- Load balancing

### User Service
Ports:
- 8081
- 8082

Responsibilities:
- User creation
- API key generation
- API key validation

### Service Registry
Port: 8761

Technology:
- Eureka Server

Responsibilities:
- Service registration
- Service discovery

### Config Server
Port: 8888

Responsibilities:
- Centralized configuration
- Git-based configuration management

## Tech Stack

- Java
- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Eureka
- Spring Cloud Config
- Spring WebFlux
- WebClient
- Spring Cloud LoadBalancer
- Resilience4j
- Redis
- MySQL
- Spring Data JPA
- Maven
- Docker

## Request Flow

1. Client sends request to API Gateway.
2. Gateway extracts the API key.
3. API key is validated through User Service.
4. Retry and Circuit Breaker protect the authentication call.
5. Redis-based rate limiter checks the API key's request limit.
6. Gateway discovers User Service through Eureka.
7. Spring Cloud LoadBalancer selects an available User Service instance.
8. Request is forwarded to User Service.
9. User Service processes the request and returns the response.

## Resilience

### Retry

Retries temporary failures while communicating with User Service.

### Circuit Breaker

Stops repeated calls to an unavailable User Service and provides fallback handling.

### Rate Limiting

Redis-based API-key rate limiting prevents excessive requests.

Current testing configuration:

- Replenish Rate: 2 requests/second
- Burst Capacity: 4

## Authentication

The Gateway uses an API key provided through:

X-API-KEY

Possible responses:

- 200 - Valid request
- 401 - Missing or invalid API key
- 429 - Rate limit exceeded
- 503 - Authentication service unavailable

## Centralized Configuration

Configuration is maintained in a separate Git repository.

Configuration files:

- gateway-service.yml
- user-service.yml
- service-registry.yml

Config Server reads these configurations and provides them to the respective services.

## Running the Project

Start services in the following order:

1. Config Server
2. Service Registry
3. User Service
4. API Gateway
5. Redis

Multiple User Service instances can be started on ports 8081 and 8082.

## Testing

The following scenarios have been verified:

- API key authentication
- Invalid API key handling
- Missing API key handling
- Redis rate limiting
- Independent rate limits for different API keys
- Retry mechanism
- Circuit breaker
- 503 fallback handling
- Eureka service discovery
- Multiple User Service instances
- Load balancing
- Centralized configuration
- End-to-end Gateway → User Service communication
