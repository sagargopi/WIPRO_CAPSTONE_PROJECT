# MyFin Bank — Backend (Spring Boot Microservices)

This folder contains the backend for the MyFin Bank capstone project. For full project setup and startup order (registry + services + UI), see the root README: `../README.md`.

## Services

```text
backend/
  eureka/     Service registry (Eureka Server)
  User/       User service (customers, accounts, transactions, loans, investments, notifications, chat)
  Owner/      Owner/Admin service (admin auth, customer management, loan approvals, admin notifications, admin chat)
```

## Ports (Default)

- Eureka Server: **8761**
- User Service: **8082**
- Owner/Admin Service: **8083**

Port configuration is in each service’s `src/main/resources/application.properties`.

## Databases

MySQL databases used by default:

- User service: `user_db` (schema script: `User/database/user_db_schema.sql`)
- Owner/Admin service: `admin_db` (schema script: `Owner/database/admin_db_schema.sql`)

Update DB credentials in each service’s `application.properties` if needed.

## How Services Communicate

- Services register with Eureka at `http://localhost:8761/eureka/`.
- The Owner/Admin service uses Feign clients to call endpoints exposed by the User service (e.g., customer updates and account details).

## Run (Per Service)

Each service includes the Maven Wrapper. From a service directory:

```powershell
.\mvnw.cmd spring-boot:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

Recommended startup order:

1. `eureka/`
2. `User/`
3. `Owner/`
