# Finance Tracker API
[![codecov](https://codecov.io/gh/NatePombi/finance-tracker-api/graph/badge.svg?token=WSUBYBXDIB)](https://codecov.io/gh/NatePombi/finance-tracker-api)
![Java](https://img.shields.io/badge/Java-17-blue)
![Build](https://github.com/NatePombi/finance-tracker-api/actions/workflows/test.yml/badge.svg)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.5.6-green)
![Last Commit](https://img.shields.io/github/last-commit/NatePombi/finance-tracker-api)



A production-style personal finance backend built with Spring Boot, designed with real-world financial domain modeling and scalable architecture principles.

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

---

## Core Features(Implemented)

### Authentication

* JWT-based authentication

* User registration & login

* Secure user-scoped queries

### Account Management

* Multiple accounts per user (Checking, Savings, Credit)

* Secure ownership validation

* Ledger-based balance calculation (derived from transactions)

* No stored balance field (financially correct architecture)

### Transaction System

* Transactions linked to accounts

* Category validation against transaction type

* Pagination support

* Date filtering:

  * Between dates

  * From date

  * To date

* Monthly income & expense aggregation

* Category-based monthly summaries


### Reporting & Analytics

* Monthly summary report:

  * Total income

  * Total expenses

  * Net balance

* Expense breakdown by category (monthly)

### Architectural Decisions

* Transactions do NOT store User directly (ownership derived via Account)

* Account balances are computed dynamically using aggregation queries

* All repository queries are scoped by account.user

* JOIN FETCH used to prevent N+1 problems

* Service layer handles business validation logic

---

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
