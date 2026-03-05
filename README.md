# 🏠 Accommodation Booking App

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![MySQL](https://img.shields.io/badge/MySQL-DB-4479A1?style=for-the-badge&logo=mysql)](https://www.mysql.com/)

A Spring Boot backend for managing accommodations, bookings, and payments.

The application provides:
- 🏘️ **Accommodation inventory** (create/update/delete + public listing)
- 📅 **Bookings** (create, list, update, delete + scheduled expiration)
- 💳 **Payments** (Stripe Checkout sessions + success/cancel callbacks)
- 🔐 **JWT authentication** (register/login + role-based authorization)
- 📣 **Telegram notifications** (optional, for key events)
- 📚 **OpenAPI / Swagger UI** documentation

---

## 📑 Table of Contents

- [🧰 Tech Stack](#-tech-stack)
- [🗂️ Project Structure (high level)](#️-project-structure-high-level)
- [👥 Features & Roles](#-features--roles)
    - [🧑‍💼 Roles](#-roles)
    - [📏 Business Rules (examples)](#-business-rules-examples)
- [🌐 API Overview](#-api-overview)
    - [📖 OpenAPI / Swagger](#-openapi--swagger)
- [🚀 Getting Started](#-getting-started)
    - [✅ 1) Prerequisites](#-1-prerequisites)
    - [🔧 2) Environment variables](#-2-environment-variables)
    - [▶️ 3) Run locally (Maven)](#️-3-run-locally-maven)
- [🔐 Authentication (JWT)](#-authentication-jwt)
- [🗃️ Database & Migrations](#️-database--migrations)
- [💬 Telegram Notifications](#-telegram-notifications)
- [💳 Stripe Payments](#-stripe-payments)
- [🧪 Testing](#-testing)
- [🛠️ Common Troubleshooting](#️-common-troubleshooting)

---

## 🧰 Tech Stack

- ☕ **Java** (SDK 21)
- 🍃 **Spring Boot** (Web, Security, Data JPA, Validation)
- 🧪 **Liquibase** (database migrations + seed data)
- 🐬 **MySQL**
- 🪪 **JWT** (stateless auth)
- 💳 **Stripe** (Checkout sessions)
- 💬 **Telegram Bots API** (notifications)
- 🧩 **MapStruct** (DTO mapping)
- 🧷 **Lombok**
- ✅ **Testing**: JUnit 5, Mockito, Spring Boot Test

---

## 🗂️ Project Structure (high level)

- 🧭 `src/main/java/.../controller` — REST controllers (API endpoints)
- 🧠 `src/main/java/.../service` — services (business logic)
- 🗄️ `src/main/java/.../repository` — Spring Data JPA repositories
- 🧱 `src/main/java/.../model` — JPA entities
- 🧾 `src/main/resources/db/changelog` — Liquibase changelogs + seed data
- 🧪 `src/test` — unit + integration tests

---

## Features & Roles

### 🧑‍💼 Roles
- 🛡️ `ADMIN`
  - Manage accommodations
  - View any user’s bookings (by user id + status)
  - Delete bookings
  - View payments (depending on service rules)
- 🙋 `CUSTOMER`
  - Browse accommodations
  - Create/manage own bookings
  - Create payment sessions for own bookings
  - View own profile and update it

### 📏 Business Rules (examples)
- ✅ Bookings require valid dates and availability.
- ⛔ Customer cannot create new booking if they have **pending payments**.
- 💳 Payments can be created only for **PENDING** bookings.
- ⏰ Scheduled job marks eligible bookings as **EXPIRED**.

---

## 🌐 API Overview

Base endpoints:
- 🩺 `GET /health` — health check
- 🧾 `POST /auth/register` — register a new user
- 🔑 `POST /auth/login` — login and receive JWT token
- 🏘️ `GET /accommodations` — public list of accommodations
- ➕ `POST /accommodations` — create accommodation (ADMIN)
- 🆕 `POST /bookings` — create booking (ADMIN/CUSTOMER)
- 📋 `GET /bookings?user_id={id}&status={status}` — bookings by user+status (ADMIN)
- 👤 `GET /bookings/my` — bookings of authenticated user
- 💳 `POST /payments` — create Stripe Checkout session (ADMIN/CUSTOMER)
- ✅ `GET /payments/success?session_id=...` — Stripe success callback (public)
- ❌ `GET /payments/cancel?session_id=...` — Stripe cancel callback (public)
- 🙍 `GET /users/me` — current user profile
- ✏️ `PATCH /users/me` — update current user profile

### 📖 OpenAPI / Swagger
Once the application is running, Swagger UI is available at:

- 🧭 `GET /swagger-ui.html`  
or
- 🧭 `GET /swagger-ui/index.html`

And OpenAPI JSON:
- 🧾 `GET /v3/api-docs`

---

## 🚀 Getting Started

### ✅ 1) Prerequisites
- ☕ Java 21 installed
- 🧰 Maven (or use the included `mvnw`)
- 🐬 A MySQL database (for local run), or use 🐳 Docker

### 🔧 2) Environment variables
The app reads configuration from `src/main/resources/application.properties` and supports `.env` import.

Typical variables you should provide (example names):
- 🧷 `SPRING_LOCAL_PORT` — local port used to build `app.base-url`
- ⏳ `JWT_EXPIRATION` — token TTL in ms (e.g., `3600000`)
- 🔑 `JWT_SECRET` — secret key for signing JWT (HMAC)
- 🤖 `TELEGRAM_BOT_USERNAME` — Telegram bot username
- 🪙 `TELEGRAM_BOT_TOKEN` — Telegram bot token
- 🧑‍💻 `TELEGRAM_ADMIN_CHAT_ID` — chat id for notifications
- 💳 `STRIPE_SECRET_KEY` — Stripe secret key

> 💡 Tip: See `.env.template` if present in the repository, copy it to `.env`, fill values, and run.

### ▶️ 3) Run locally (Maven)
bash ./mvnw spring-boot:run
Or build a jar and run:
bash ./mvnw clean package && java -jar target/accommodation-booking-app-0.0.1-SNAPSHOT.jar
> Tip: Use `mvnw.cmd` on Windows. `docker-compose up` to run the app with a MySQL database.

---

## 🔐 Authentication (JWT)

1. 🧾 Register: `POST /auth/register`
2. 🔑 Login: `POST /auth/login` → response contains `{ "token": "..." }`
3. 🧷 Call protected endpoints with:
    - Header: `Authorization: Bearer <token>`

---

## 🗃️ Database & Migrations

- 🧪 Liquibase migrations are located in `src/main/resources/db/changelog`.
- 📌 The master changelog is: `db.changelog-master.yaml`.
- 🌱 Seed data is included via changelogs (roles, users, sample locations, amenities, accommodations, bookings, payments).

---

## 💬 Telegram Notifications

Telegram integration can be toggled via property:
- ✅/❌ `telegram.enabled=true|false`

When enabled, the application sends messages on events such as:
- 🏘️ Accommodation created
- 📅 Booking created/updated/deleted
- ✅ Payment succeeded (and other informational events)

---

## 💳 Stripe Payments

The payment flow is based on Stripe Checkout sessions:
1. 🧾 Create session: `POST /payments`
2. 🔁 Client redirects user to Stripe Checkout URL
3. ↩️ Stripe redirects back to:
    - ✅ `/payments/success?session_id=...` or
    - ❌ `/payments/cancel?session_id=...`

> 🧪 For local development, use test keys (`sk_test_...`).  
> 🔒 For production, use live keys and HTTPS.

---

## 🧪 Testing

This repository contains both:
- ⚡ **Unit tests** (Mockito): fast, isolate business rules
- 🧩 **Integration tests** (Spring Boot + MockMvc): validate controller behavior, security, and persistence

### ▶️ Run all tests
mvn test

### 🐳 Integration test database
Tests are configured to use **Testcontainers via JDBC driver**, so you don’t need a local MySQL instance for tests.

---

## 🛠️ Common Troubleshooting

### 🐳 Tests failing due to Docker/Testcontainers
- ✅ Make sure Docker Desktop / Docker Engine is running.
- 🪟 On Windows, ensure WSL2 backend is available.

### 🚨 Getting 500 errors instead of 401/403
If exceptions are handled globally, authentication/authorization errors may be converted to 500.
You can refine exception handling later to return proper HTTP codes (401/403/400) consistently.

### 📚 Swagger not available
Ensure the app is running and that security configuration permits access to:
- `/swagger-ui/**`
- `/v3/api-docs/**`

---
