# Finance Tracker API (Deployed)
[![codecov](https://codecov.io/gh/NatePombi/finance-tracker-api-aws/graph/badge.svg?token=WSUBYBXDIB)](https://codecov.io/gh/NatePombi/finance-tracker-api-aws)
![Java](https://img.shields.io/badge/Java-17-blue)
![Build](https://github.com/NatePombi/finance-tracker-api/actions/workflows/test.yml/badge.svg)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.5.10-green)
![Last Commit](https://img.shields.io/github/last-commit/NatePombi/finance-tracker-api-aws)
[![Live Demo](https://img.shields.io/badge/Live%20Demo-Render-46E3B7?logo=render&logoColor=white)](https://finance-tracker-api-aws.onrender.com/swagger-ui/index.html)


A production ready RESTful API for managing personal finances, including accounts, transactions, and authentication. The system is built with Spring Boot and deployed on Render, following a layered architecture (Controller → Service → Repository) to ensure maintainability, scalability, and separation of concerns.

It models real world financial operations with a strong focus on data integrity, security (JWT authentication), and database consistency (PostgreSQL + Flyway migrations).

In addition to core financial features, the system integrates AI-powered financial analysis via external APIs, enabling users to receive insights into their spending behavior, detect unusual spending patterns, and get data driven saving recommendations based on actual transaction history.

---


## Cloud Architecture

Client → Render → PostgreSQL


- Application hosted on Render
- PostgreSQL database hosted on Render
- Secure configuration using environment variables

---

## Live Demo

- Swagger UI:
  - https://finance-tracker-api-aws.onrender.com/swagger-ui/index.html

The API is publicly deployed and can be tested through the interactive Swagger documentation.


---

## How AI Integration Works

### User triggers analysis endpoint:
- GET /ai/analyze

  - Backend retrieves user transactions from the database
  -  Transactions are converted into structured text
   - A prompt is generated and sent to the AI API
   - AI returns financial insights, which are sent back to the user

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
* ![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini-blue?logo=google&logoColor=white)
* [![Deployed on Render](https://img.shields.io/badge/Deployed%20on-Render-46E3B7?logo=render&logoColor=white)](https://render.com/)

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
  - Up to a specific Date

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

##   How to Run


#### Running Project Locally

1. Clone repository
```bash
  git clone git@github.com:NatePombi/Finance-Tracker-API.git
  cd Finance-Tracker-API
```

2. Create .env file
```bash
  DB_URL=jdbc:postgresql://localhost:5432/financetracker
  DB_USERNAME=postgres
  DB_PASSWORD=yourpassword
  API_KEY=your_sendgrid_api_key
```

3. Run Locally

```bash
  docker compose up --build
```

App will be available at:
http://localhost:8080

Swagger UI:
http://localhost:8080/swagger-ui/index.html

---

## Docker Setup

* Build Image
  ```bash
  docker build -t finance-tracker-api .
  ```

* Run container
  ```bash
  docker run -p 8080:8080 finance-tracker-api
  ```
---

## CI/CD Pipeline (GitHub Actions)

on every push to main or master:

Pipeline does:
* Runs tests with Maven
* Generates JaCoCo coverage report
* Builds JAR
* Builds Docker
* Pushes image to Docker Hub

---

## Deployment (AWS EC2)
The application is deployed using Docker on Render.

Deployment Architecture
GitHub

↓

Render

↓

Docker

↓

Spring Boot API

↓

PostgreSQL

Render handles the application hosting while PostgreSQL provides the persistent database.

Environment variables are used to keep credentials and sensitive configuration outside of the source code.

---

## Environment Variables

This project uses environment variables for secure configuration:

| Variable    | Description                  |
| ----------- | ---------------------------- |
| DB_URL      | PostgreSQL connection string |
| DB_USERNAME | DB username                  |
| DB_PASSWORD | DB password                  |
| API_KEY     | SendGrid API key             |
| JWT_SECRET  | Secret for JWT tokens        |


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

## API Preview (Live)

![Swagger UI](/.docs/swagger1.png)
![Swagger UI](/.docs/swagger2.png)
![Swagger UI](/.docs/swagger3.png)

- Responses

![Swagger UI](/.docs/swagger4.png)
![Swagger UI](/.docs/swagger5.png)
![Swagger UI](/.docs/swagger6.png)
![Swagger UI](/.docs/swagger7.png)

- Authentication 

![Swagger UI](/.docs/swagger8.png)
![Swagger UI](/.docs/swagger9.png)

---

## API Preview (Local)

![Swagger UI](/.docs/swaggerScreen2.png)

![Swagger UI](/.docs/swaggerScreen.png)


___

## Project Status

✅ Fully functional backend API
✅ Deployed on AWS
✅ Dockerized and production-ready
✅ Database migrations with Flyway
✅ Interactive API documentation

---

## Future Enhancements

- Budget tracking per category
- Account transfers (account-to-account)
- Caching optimization for heavy aggregations
- Improved test coverage (unit & integration tests)
- Kubernetes deployment 
- Role-based access control (Admin/User)
- Frontend integration (React/Angular)


---

## Status

In the development phases.  

---

## Author
Built by Nathan Pombi