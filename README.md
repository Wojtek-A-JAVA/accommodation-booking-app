# 🏠 Accommodation Booking App

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![MySQL](https://img.shields.io/badge/MySQL-DB-4479A1?style=for-the-badge&logo=mysql)](https://www.mysql.com/)

A Spring Boot backend application for managing accommodations, bookings, users, and payments.

The project demonstrates production-style backend features including:

- 🔐 JWT authentication & role-based authorization
- ⚡ API rate limiting
- 💳 Stripe Checkout integration
- 🧪 Integration testing with Testcontainers
- 🧱 Database versioning with Liquibase
- 🐳 Dockerized environment
- 📣 Telegram notifications
- 📚 OpenAPI / Swagger documentation
- ⏰ Scheduled booking expiration
- 🛡️ Global exception handling

---

# ✨ Key Features

- 🏘️ Accommodation inventory management
- 📅 Booking management with validation and business rules
- 💳 Stripe payment flow
- 🔐 JWT authentication with stateless security
- 👥 Role-based authorization (`ADMIN`, `CUSTOMER`)
- ⚡ API rate limiting using Bucket4j
- 🧱 Database migrations with Liquibase
- 🧪 Unit and integration tests
- 🐳 Docker + Docker Compose support
- 📣 Telegram notifications for important events
- 📚 Swagger/OpenAPI documentation
- ⏰ Scheduled expiration of outdated bookings

---

# 📑 Table of Contents

- [🧰 Tech Stack](#-tech-stack)
- [🗂️ Project Structure](#️-project-structure)
- [👥 Features & Roles](#-features--roles)
- [⚡ Rate Limiting](#-rate-limiting)
- [🛡️ Security Notes](#️-security-notes)
- [🌐 API Overview](#-api-overview)
- [🚀 Getting Started](#-getting-started)
- [🐳 Run with Docker Compose](#-run-with-docker-compose)
- [🔐 Authentication (JWT)](#-authentication-jwt)
- [🗃️ Database & Migrations](#️-database--migrations)
- [💳 Stripe Payments](#-stripe-payments)
- [💬 Telegram Notifications](#-telegram-notifications)
- [🧭 Application Flow](#-application-flow)
- [🧪 Testing](#-testing)
- [🛠️ Common Troubleshooting](#️-common-troubleshooting)

---

# 🧰 Tech Stack

- ☕ Java 21
- 🍃 Spring Boot 3
- 🔐 Spring Security + JWT
- 🗄️ Spring Data JPA
- 🧪 Bean Validation
- 🧱 Liquibase
- 🐬 MySQL
- ⚡ Bucket4j
- 💳 Stripe API
- 📣 Telegram Bots API
- 🧩 MapStruct
- 🧷 Lombok
- 🧪 JUnit 5 + Mockito + Spring Boot Test
- 🐳 Docker & Docker Compose
- 🧪 Testcontainers

---

# 🗂️ Project Structure

```text
src/main/java/accommodation/booking/app
├── controller      # REST API controllers
├── service         # Business logic
├── repository      # Spring Data repositories
├── model           # JPA entities
├── dto             # Request / response DTOs
├── mapper          # MapStruct mappers
├── security        # JWT + Spring Security + rate limit
├── exception       # Exception handling
├── scheduler       # Scheduled jobs
├── notification    # Telegram
└── config          # Application configuration
```

```text
src/main/resources
├── db/changelog    # Liquibase migrations + seed data
├── application.properties
├── application.local.properties
└── application-docker.properties
```

---

# 👥 Features & Roles

## 🧑‍💼 Roles

### 🛡️ ADMIN

- Manage accommodations
- View all bookings
- Manage payments
- Access administration endpoints
- Monitor rate limiting statistics

### 🙋 CUSTOMER

- Browse accommodations
- Create and manage own bookings
- Create payment sessions
- View and edit own profile

---

## 📏 Business Rules

- ✅ Bookings require valid dates and availability
- ⛔ User cannot create booking with pending payments
- 💳 Payments are allowed only for `PENDING` bookings
- ⏰ Expired bookings are updated automatically by scheduler
- 🛡️ Protected endpoints require valid JWT token
- 🚫 DELETE operations are restricted to preserve demo data integrity

---

# ⚡ Rate Limiting

The API includes rate limiting protection using Bucket4j.

Limits:

- 🧾 Register: `5 requests/minute per IP`
- 🔑 Login: `10 requests/minute per IP`
- 👤 Authenticated API: `50 requests/minute per user`
- 🌐 Fallback: per IP if user is unauthenticated

When the limit is exceeded:

- API returns HTTP `429 Too Many Requests`

GET endpoints are intentionally excluded from rate limiting to avoid unnecessary restrictions on public data browsing.

Administration endpoint:

- 📊 `GET /rate-limit`

---

# 🛡️ Security Notes

- 🔐 JWT authentication is required for protected endpoints
- 🧂 Passwords are hashed using BCrypt
- 👥 Role-based authorization with Spring Security
- ✅ Request DTO validation
- 🛡️ Global exception handling
- ⚡ Rate limiting for authentication and write endpoints
- 🚫 Invalid or expired JWT tokens return HTTP `401 Unauthorized`

---

# 🌐 API Overview

## Public Endpoints

- 🩺 `GET /health`
- 🧾 `POST /auth/register`
- 🔑 `POST /auth/login`
- 🏘️ `GET /accommodations`
- 📚 `GET /swagger-ui/index.html`

---

## Protected Endpoints

### 👤 Users

- 🙍 `GET /users/me`
- ✏️ `PATCH /users/me`

### 📅 Bookings

- 🆕 `POST /bookings`
- 📋 `GET /bookings/my`
- 📋 `GET /bookings?user_id={id}&status={status}`

### 🏘️ Accommodations

- ➕ `POST /accommodations`
- ✏️ `PUT /accommodations/{id}`
- ❌ `DELETE /accommodations/{id}`

### 💳 Payments

- 💳 `POST /payments`
- ✅ `GET /payments/success`
- ❌ `GET /payments/cancel`

### ⚙️ Administration

- 📊 `GET /rate-limit`

---

# 📖 OpenAPI / Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

---

# 🚀 Getting Started

## ✅ Prerequisites

- ☕ Java 21
- 🧰 Maven
- 🐳 Docker Desktop
- 🐬 MySQL (optional for local run)

---

# 🔧 Environment Variables

The application supports `.env` configuration.

Example variables:

```env
# Server
SPRING_LOCAL_PORT=8080

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=3600000

# Stripe
STRIPE_SECRET_KEY=sk_test_xxx

# Telegram
TELEGRAM_ENABLED=false
TELEGRAM_BOT_USERNAME=your_bot
TELEGRAM_BOT_TOKEN=your_token
TELEGRAM_ADMIN_CHAT_ID=123456789
```

---

# ▶️ Run Locally (Maven)

```bash
./mvnw spring-boot:run
```

Or:

```bash
./mvnw clean package
java -jar target/accommodation-booking-app-0.0.1-SNAPSHOT.jar
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

---

# 🐳 Run with Docker Compose

Build and start the application:

```bash
docker compose up --build
```

Application:

- API → `http://localhost:8080`
- Swagger → `http://localhost:8080/swagger-ui/index.html`

Stop containers:

```bash
docker compose down
```

Remove volumes:

```bash
docker compose down -v
```

---

# 🔐 Authentication (JWT)

## Login Flow

1. 🧾 Register user
2. 🔑 Login
3. 📦 Receive JWT token
4. 🧷 Use token in requests:

```http
Authorization: Bearer <token>
```

Expired or invalid tokens return:

```json
{
  "error": "Unauthorized",
  "message": "JWT token expired"
}
```

---

# 🗃️ Database & Migrations

Liquibase manages schema and seed data.

Location:

```text
src/main/resources/db/changelog
```

Master changelog:

```text
db.changelog-master.yaml
```

Seed data includes:

- 👥 Roles
- 👤 Users
- 🏘️ Accommodations
- 📅 Bookings
- 💳 Payments

---

# 💳 Stripe Payments

Payment flow:

1. 🧾 Create Stripe session:

```http
POST /payments
```

2. 🔁 User is redirected to Stripe Checkout

3. ↩️ Stripe redirects to:

- ✅ `/payments/success`
- ❌ `/payments/cancel`

Use Stripe test keys for development.

---

# 💬 Telegram Notifications

Telegram integration is optional.

Enable:

```properties
telegram.enabled=true
```

Notifications include:

- 🏘️ Accommodation created
- 📅 Booking events
- 💳 Payment status changes

---

# 🧭 Application Flow

1. User registers and logs in
2. JWT token is generated
3. User creates booking
4. User creates Stripe payment session
5. Stripe redirects to success/cancel callback
6. Booking/payment status is updated
7. Telegram notification is sent

---

# 🧪 Testing

The project contains:

- ⚡ Unit tests
- 🧩 Integration tests
- 🔐 Security tests
- ✅ Validation tests
- 🗄️ Repository tests

Run tests:

```bash
mvn test
```

---

## 🐳 Testcontainers

Integration tests use Testcontainers.

Requirements:

- Docker Desktop running
- Internet connection (first image pull)

No local MySQL instance is required for tests.

---

# 🛠️ Common Troubleshooting

## 🐳 Docker issues

If containers fail:

```bash
docker compose down -v
docker compose up --build
```

---

## 🚨 HTTP 401

JWT exceptions should be handled in security filters to return proper `401 Unauthorized`.

---

## 📚 Swagger unavailable

Ensure security configuration allows:

```text
/swagger-ui/**
/v3/api-docs/**
```

---

# 📌 Future Improvements

Possible future extensions:

- 📧 Email notifications
- 🌍 Multi-language support
- 📊 Admin dashboard
- 🧾 Invoice generation
- ☁️ Cloud deployment
- 📦 CI/CD pipeline
- 🔄 Refresh tokens
- 🧠 Caching with Redis

---

# 📄 License

This project is for portfolio purposes.

