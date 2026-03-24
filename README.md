# Finance Tracker API
[![codecov](https://codecov.io/gh/NatePombi/finance-tracker-api/graph/badge.svg?token=WSUBYBXDIB)](https://codecov.io/gh/NatePombi/finance-tracker-api)
![Java](https://img.shields.io/badge/Java-17-blue)
![Build](https://github.com/NatePombi/finance-tracker-api/actions/workflows/test.yml/badge.svg)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.5.6-green)
![Last Commit](https://img.shields.io/github/last-commit/NatePombi/finance-tracker-api)



A production-ready backend API for managing personal finances, including accounts, transactions, and authentication.
Built with Spring Boot, PostgreSQL, Docker, and Flyway.

---

## Overview

* Domain-driven design

* Multi-account financial modeling

* Secure user-scoped data access

* Ledger-based balance calculation

* Aggregation queries & reporting

* Pagination and filtering

* Clean service-layer architecture


The system models real financial relationships:

```
User
 ├── Accounts
 │     └── Transactions
 └── Categories
```
Transactions belong to Accounts, and Accounts belong to Users ensuring strong ownership boundaries and data integrity.

---

## Tech Stack

* ![Java](https://img.shields.io/badge/Java-17-blue)
* ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-green)
* ![Spring Security](https://img.shields.io/badge/Security-JWT-yellow)
* ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-3-brightgreen?logo=spring&logoColor=white)
* ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-blue)
* ![Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)
* ![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?logo=flyway&logoColor=white)
* ![Docker](https://img.shields.io/badge/Docker-Containerization-2496ED?logo=docker&logoColor=white)
* ![Docker Compose](https://img.shields.io/badge/Docker%20Compose-Orchestration-2496ED?logo=docker&logoColor=white)
* ![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black)

---

## Core Features(Implemented)

### Authentication

* JWT-based authentication

* User registration & login

* Secure, user-scoped access to all resources

### Account Management

- Support for multiple account types (Checking, Savings, Credit)
- Strict ownership validation per user
- Ledger-based balance calculation (derived from transactions)
- No stored balance field (ensures financial accuracy)

### Transaction System

- Transactions linked to accounts
- Category validation based on transaction type (income/expense)
- Pagination support for large datasets

### Date Filtering
- Filter transactions:
  - Between dates
  - From a specific date
  - Up to a specific date

### Analytics
- Monthly income and expense aggregation
- Category-based monthly summaries

###  Reporting & Analytics
- Monthly financial summary including:
  - Total income
  - Total expenses
  - Net balance
- Expense breakdown by category

### Architectural Decisions

- Transactions do **not** store User directly  
  → Ownership is derived through Account relationships

- Account balances are **calculated dynamically**  
  → Ensures consistency and avoids stale data

- All queries are scoped using: account.user

- `JOIN FETCH` is used to prevent N+1 query issues

- Business logic and validations are handled in the **service layer**

---

### How to Run

#### Running the Application

### Prerequisites
- Docker installed

### Run with Docker

```bash
  docker compose up --build
```

App will be available at:
http://localhost:8080

Swagger UI:
http://localhost:8080/swagger-ui/index.html


---

## API Example


### Example API Endpoints

### Create Account
POST /api/v1/accounts

### Get Accounts
GET /api/v1/accounts

### Get Balance
GET /api/v1/accounts/{id}



---

## API Preview

![Swagger UI](/.docs/swaggerScreen2.png)

![Swagger UI](/.docs/swaggerScreen.png)


___

## Current Status

* Core financial domain complete:

* Authentication

* Accounts

* Transactions

* Categories

* Monthly reporting

System supports multi-account financial tracking with proper ownership boundaries.

---

## Next Planned Enhancements

* Budget tracking per category

* Account transfers (account-to-account)

* Global exception handler

* API documentation (OpenAPI / Swagger)

* Docker support

* Test coverage (unit & integration tests)

* Caching optimization for heavy aggregations

---

## Status

In development — built as part of a professional backend portfolio.
