# WIPRO Capstone Project — MyFin Bank

Full-stack banking-style application built with a React (Vite) frontend and Spring Boot microservices (User + Owner/Admin) registered with a Eureka service registry.

## Architecture

- **Frontend (Vite + React)**: `myFin-Bank/`
- **Service registry (Eureka Server)**: `backend/eureka/` (port **8761**)
- **User service**: `backend/User/` (port **8082**, MySQL DB `user_db`)
- **Owner/Admin service**: `backend/Owner/` (port **8083**, MySQL DB `admin_db`)

The frontend calls the backend services directly:

- User API: `http://localhost:8082` (configured in [api.js](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/myFin-Bank/src/services/api.js#L1-L9))
- Admin API: `http://localhost:8083` (configured in [api.js](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/myFin-Bank/src/services/api.js#L1-L9))

## Tech Stack

- **Frontend**: React + Vite, React Router, Axios, Bootstrap
- **Backend**: Spring Boot, Spring Web, Spring Data JPA, Spring Security (JWT), Spring Cloud Netflix Eureka, OpenFeign
- **Database**: MySQL

## Features

- **Authentication**: Separate login/register for Customers and Admins.
- **Customer banking**: Account balance, deposits, withdrawals, and transfers.
- **Transactions**: Transaction history with a user-friendly transaction ID format (`USER{userId}TXN{random}`).
- **Loans**: Apply for loans, calculate EMI, track loan status, and search/filter loan applications.
- **Investments**: Create investments, calculate maturity amount, and view investment summary.
- **Notifications**: Loan/transaction/system alerts with unread counts and read status.
- **Support chat**: Customer ↔ Admin messaging with conversation view and unread tracking.
- **Admin operations**: View/manage customers, review loan requests, approve/reject loans, and publish announcements.

## Prerequisites

- Java **17**
- Node.js **18+** (recommended)
- MySQL **8+**

## Database Setup

Create the databases and tables (includes sample records):

- Run [admin_db_schema.sql](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/backend/Owner/database/admin_db_schema.sql) for the Owner/Admin service
- Run [user_db_schema.sql](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/backend/User/database/user_db_schema.sql) for the User service

Default MySQL connection settings are in:

- Owner/Admin: [application.properties](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/backend/Owner/src/main/resources/application.properties#L5-L16)
- User: [application.properties](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/backend/User/src/main/resources/application.properties#L5-L16)

If your MySQL username/password differ, update those files before starting the services.

## Running the Project (Recommended Order)

### 1) Start Eureka (Service Registry)

From `backend/eureka/`:

```powershell
.\mvnw.cmd spring-boot:run
```

Eureka dashboard: http://localhost:8761

### 2) Start User Service

From `backend/User/`:

```powershell
.\mvnw.cmd spring-boot:run
```

Service runs on: http://localhost:8082

### 3) Start Owner/Admin Service

From `backend/Owner/`:

```powershell
.\mvnw.cmd spring-boot:run
```

Service runs on: http://localhost:8083

### 4) Start Frontend

From `myFin-Bank/`:

```powershell
npm install
npm run dev
```

Vite prints the local URL (usually http://localhost:5173).

## Test Accounts (From SQL Seed Data)

These are inserted by the schema scripts:

- **Admin/Owner**: `Admin` / `admin123` (also `admin1`, `admin2`)
- **User/Customer**: `customer1` / `password123` (also `customer2`)

## Scripts

### Frontend

From `myFin-Bank/`:

```powershell
npm run lint
npm run build
npm run preview
```

### Backend (per service)

From any of `backend/eureka/`, `backend/User/`, `backend/Owner/`:

```powershell
.\mvnw.cmd test
```

## Notes

- Eureka client configuration is in the backend services’ (User) [application.properties](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/backend/User/src/main/resources/application.properties#L17-L21) and (Owner) [application.properties](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/backend/Owner/src/main/resources/application.properties#L17-L21).
- If you change ports, also update the frontend base URLs in [api.js](file:///c:/Users/Lenovo/Downloads/WIPRO_CAPSTONE_PROJECT/myFin-Bank/src/services/api.js#L1-L9).


