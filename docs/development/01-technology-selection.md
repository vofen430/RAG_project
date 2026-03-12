# Technology Selection Specification

## 1. Project Scope

This project is a locally deployed RAG application for document upload, indexing, retrieval, explainable answer generation, and chat-based interaction.

The selected stack is fixed for the delivery package in this folder.

## 2. Backend Technology

- Java 17
- Spring Boot 3.2.x
- Spring Web
- Spring WebFlux
- Spring Security
- Spring Validation
- MyBatis-Plus
- Flyway

## 3. Frontend Technology

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios
- Fetch with Server-Sent Events
- Marked for Markdown rendering

## 4. Data and Storage Technology

- PostgreSQL 16
- pgvector
- Redis 7
- Local file storage for source documents

## 5. RAG and Application Integration Technology

- External embedding service integration
- External reranking service integration
- External chat completion service integration
- Structured prompt templates
- Retrieval trace persistence
- Citation-based answer rendering

Provider-specific model API details are not part of this document package. The implementation team should use the provider API manual for provider request and response handling.

## 6. Deployment and Runtime Technology

- Docker Engine
- Docker Compose
- Maven Wrapper
- Node.js 24
- npm 11

## 7. Testing Technology

- JUnit 5
- Mockito
- Spring Boot Test
- Vitest
- Playwright

## 8. Observability and Operations

- Spring Boot Actuator
- SLF4J
- Logback

## 9. Standard Version Baseline

- Java 17
- Spring Boot 3.2.5
- Node.js 24
- npm 11
- PostgreSQL 16
- Redis 7
- Docker Compose v2
