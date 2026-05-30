# Changelog

All notable changes to the FoodSense AI project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-05-30

### Added
- **Complaint Service**: REST API for submitting and retrieving customer complaints
  - `POST /api/complaints` — Submit a new complaint
  - `GET /api/complaints` — Retrieve all complaints
  - `GET /api/complaints/{id}` — Retrieve complaint by ID
- **Analysis Service**: REST API for retrieving AI-powered complaint analyses
  - `GET /api/analyses` — Retrieve all analyses
  - `GET /api/analyses/complaint/{complaintId}` — Retrieve analysis by complaint ID
- **Kafka Integration**: Asynchronous complaint processing pipeline
  - `ComplaintProducer` — Publishes complaint IDs to `complaint-topic`
  - `ComplaintConsumer` — Consumes complaint IDs and triggers AI analysis
- **Google Gemini Integration**: LLM-powered complaint analysis
  - Automatic extraction of category, sentiment, priority, and summary
  - Support for 6 complaint categories, 3 sentiment levels, 3 priority levels
  - Robust JSON parsing with markdown code fence handling
- **PostgreSQL Persistence**: JPA entities with UUID primary keys
  - `complaints` table for raw customer complaints
  - `complaint_analyses` table for AI analysis results
- **Exception Handling**: Global exception handler with structured error responses
  - `ResourceNotFoundException` (404)
  - `GeminiAnalysisException` (502)
  - Validation errors (400)
  - Generic server errors (500)
- **OpenAPI / Swagger**: Interactive API documentation at `/swagger-ui.html`
- **Docker Infrastructure**: Complete Docker Compose setup
  - PostgreSQL 16 Alpine
  - Apache Kafka with Zookeeper (Confluent 7.6.0)
  - Multi-stage Dockerfile for application
- **Unit Tests**: 26 test methods across 5 test classes
  - Service layer tests with Mockito
  - Controller tests with MockMvc
  - Mapper tests (pure JUnit 5)
  - Gemini service tests with WebClient mock chain
- **Documentation**: Comprehensive project documentation
  - Project overview, architecture, setup guide
  - API documentation with example requests/responses
  - Kafka flow and Gemini integration guides
  - Database schema, testing, and deployment guides

### Technical Details
- Java 21 with text blocks and modern stream operations
- Spring Boot 3.2.5
- Spring Data JPA with Hibernate
- Spring Kafka for message processing
- Spring WebFlux WebClient for Gemini API calls
- Lombok for boilerplate reduction
- Jakarta Validation for request validation
- Springdoc OpenAPI for Swagger UI

---

## [Unreleased]

### Planned
- Integration test suite with Testcontainers
- Kafka dead letter queue and retry mechanism
- Redis caching layer
- React frontend dashboard
- Spring Security + JWT authentication
- CI/CD pipeline with GitHub Actions
- Prometheus + Grafana monitoring
