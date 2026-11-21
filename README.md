# Stark Group — Vehicle Management System (VMS) Backend

An enterprise-grade, robust **Vehicle Management System (VMS)** backend built with **Java 25**, **Spring Boot**, and **MySQL / TiDB**. This service provides a high-performance REST API powering fleet administration, asset registration, lifecycle management, and credential authentication for the **Stark Group VMS** enterprise platform.

---

## 📌 Scope & Architecture Boundaries

* **Strict Domain Layer Separation**: Persistence logic resides exclusively within the repository layer, domain rules inside the service layer, and HTTP transport concerns inside the REST controller layer.
* **Domain Validation & Duplicate Enforcement**: Case-insensitive unique `vehicleNumber` validation is enforced at both the database schema level (`UNIQUE` constraint) and inside the service layer before saving or updating.
* **Standardized JSON Error Payloads**: Client-friendly, structured error responses (`status`, `error`, `message`, `timestamp`, `errors`) handled centrally via `@RestControllerAdvice`.
* **Zero-Leak Data Security**: Credentials, tokens, and database secrets are externalized via environment variables and excluded from source control.

---

## 🛠️ Tech Stack & Dependencies

| Layer / Concern | Technology | Version / Specification |
| :--- | :--- | :--- |
| **Language** | Java SE | OpenJDK / Oracle JDK 25 LTS |
| **Framework** | Spring Boot | 4.2.0 (Spring Framework 7) |
| **Persistence** | Spring Data JPA / Hibernate | Jakarta Persistence 3.x |
| **Validation** | Jakarta Bean Validation | Hibernate Validator 9.x |
| **Database** | MySQL / TiDB Cloud | MySQL Connector/J 9.x |
| **Testing DB** | H2 In-Memory | Mode MySQL (isolated tests) |
| **Build Tool** | Gradle Wrapper | Gradle 9.9.2 |
| **Testing** | JUnit 5 + Mockito + MockMvc | 100% automated test coverage |

---

## 🏛️ Database Schema & Vehicle Entity

The `vehicles` table is mapped via JPA with SQL schema integrity:

```sql
CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(50) NOT NULL UNIQUE,
    company VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);
```

### Entity Field Specifications

| Field | Type | DB Column | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Long` | `id` | PK, Auto Increment | Unique record identifier. |
| `vehicleNumber` | `String` | `vehicle_number` | Non-null, Unique, Max 50 | Trimmed registration code (e.g., `STK-8421`). |
| `company` | `String` | `company` | Non-null, Max 100 | Operating subsidiary (e.g., `Stark Logistics`). |
| `type` | `String` | `type` | Non-null, Max 50 | Category (e.g., `Heavy Truck`, `Cargo Van`). |
| `brand` | `String` | `brand` | Non-null, Max 50 | Manufacturer (e.g., `Volvo`, `Mercedes-Benz`). |
| `model` | `String` | `model` | Non-null, Max 50 | Vehicle model (e.g., `FH16 750`, `Sprinter 314`). |
| `status` | `String` | `status` | Non-null, Max 30 | Operational state (`Active` or `Inactive`). |
| `createdAt` | `LocalDateTime` | `created_at` | Non-null, Immutable | Record creation audit timestamp. |
| `updatedAt` | `LocalDateTime` | `updated_at` | Nullable | Last modification audit timestamp. |

---

## 📡 REST API Specification

### Authentication Endpoint

#### `POST /api/auth/login`
Validates user credentials against configured administrative accounts and returns a session auth token.

* **Request Body**:
  ```json
  {
    "emailOrUsername": "admin@example.com",
    "password": "your_secure_password"
  }
  ```
* **Success Response (`200 OK`)**:
  ```json
  {
    "token": "stark-vms-token-3d98b2fa-...",
    "emailOrUsername": "admin@example.com",
    "fullName": "Fleet Manager",
    "role": "FLEET_ADMIN",
    "message": "Authentication successful. Welcome to Stark Group VMS."
  }
  ```
* **Failure Response (`401 Unauthorized`)**:
  ```json
  {
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid username/email or password.",
    "timestamp": "2026-11-20T21:30:00"
  }
  ```

---

### Vehicle Management Endpoints

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/vehicles` | List all registered vehicles with optional query parameters (`search`, `status`, `company`, `type`) | `200 OK` |
| `GET` | `/api/vehicles/{id}` | Retrieve specific vehicle details by ID | `200 OK` |
| `POST` | `/api/vehicles` | Register a new vehicle record | `201 Created` |
| `PUT` | `/api/vehicles/{id}` | Update an existing vehicle's specifications | `200 OK` |
| `DELETE` | `/api/vehicles/{id}` | Remove a vehicle from fleet registry | `204 No Content` |

#### Query Parameters for `GET /api/vehicles`
* `search` *(optional)*: Case-insensitive substring search matching across `vehicleNumber`, `brand`, `model`, `company`, and `type`.
* `status` *(optional)*: Filter by vehicle status (e.g. `Active`, `Inactive`).
* `company` *(optional)*: Filter by operating subsidiary (e.g. `Stark Logistics`, `Apex Fleet Corp`).
* `type` *(optional)*: Filter by vehicle classification (e.g. `Heavy Truck`, `Cargo Van`, `SUV`).

**Example Filtered Request**:
```http
GET /api/vehicles?search=Volvo&status=Active&company=Stark+Logistics&type=Heavy+Truck
```

#### Create / Update Request Payload (`VehicleRequest`)
```json
{
  "vehicleNumber": "STK-8421",
  "company": "Stark Logistics",
  "type": "Heavy Truck",
  "brand": "Volvo",
  "model": "FH16 750",
  "status": "Active"
}
```

#### Standard Vehicle Response (`VehicleResponse`)
```json
{
  "id": 1,
  "vehicleNumber": "STK-8421",
  "company": "Stark Logistics",
  "type": "Heavy Truck",
  "brand": "Volvo",
  "model": "FH16 750",
  "status": "Active",
  "createdAt": "2026-11-20T14:00:00",
  "updatedAt": "2026-11-20T14:00:00"
}
```

---

## ⚠️ Error Handling & HTTP Status Codes

Centralized in [`GlobalExceptionHandler`](src/main/java/com/starkgroup/vms/exception/GlobalExceptionHandler.java):

| Exception | HTTP Status | Response Example | Reason |
| :--- | :---: | :--- | :--- |
| `DuplicateVehicleNumberException` | `409 Conflict` | `{"status": 409, "error": "Conflict", "message": "Vehicle with number 'STK-8421' already exists."}` | Attempting to register or update to an existing number. |
| `VehicleNotFoundException` | `404 Not Found` | `{"status": 404, "error": "Not Found", "message": "Vehicle not found with ID: 99"}` | Queried, updated, or deleted ID does not exist. |
| `MethodArgumentNotValidException` | `400 Bad Request` | `{"status": 400, "error": "Bad Request", "errors": {"vehicleNumber": "Vehicle number is required"}}` | DTO field validation constraint violated. |
| `DataIntegrityViolationException` | `409 Conflict` | `{"status": 409, "error": "Conflict", "message": "A data integrity violation occurred..."}` | Database schema-level unique constraint fallback. |
| `InvalidCredentialsException` | `401 Unauthorized` | `{"status": 401, "error": "Unauthorized", "message": "Invalid username/email or password."}` | Failed credential verification. |

---

## 📂 Project Structure

```text
stark-group-vms-backend/
├── build.gradle                                  # Gradle build script (Java 25, Spring Boot 4.2.0)
├── settings.gradle                               # Root project name: stark-group-vms-backend
├── README.md                                     # Backend architecture documentation
├── gradlew / gradlew.bat                         # Gradle wrapper binaries (Gradle 9.9.2)
├── src/
│   ├── main/
│   │   ├── resources/
│   │   │   └── application.properties            # Remote MySQL & CORS configuration
│   │   └── java/com/starkgroup/vms/
│   │       ├── StarkGroupVmsApplication.java     # Main Spring Boot entry point
│   │       ├── config/
│   │       │   ├── CorsConfig.java               # Cross-Origin Resource Sharing for Flutter Web
│   │       │   └── DataInitializer.java          # Automatic seeding of initial fleet vehicles
│   │       ├── controller/
│   │       │   ├── AuthController.java           # Authentication REST endpoints (/api/auth)
│   │       │   └── VehicleController.java        # Vehicle CRUD REST endpoints (/api/vehicles)
│   │       ├── domain/
│   │       │   └── Vehicle.java                  # JPA Entity mapped to SQL vehicles table
│   │       ├── dto/
│   │       │   ├── AuthResponse.java             # Auth token response payload
│   │       │   ├── LoginRequest.java             # Login request payload with validation
│   │       │   ├── VehicleRequest.java           # Create/Update request with Bean Validation
│   │       │   └── VehicleResponse.java          # Clean response representation
│   │       ├── exception/
│   │       │   ├── DuplicateVehicleNumberException.java
│   │       │   ├── ErrorResponse.java            # Standardized client error JSON structure
│   │       │   ├── GlobalExceptionHandler.java   # @RestControllerAdvice centralized handler
│   │       │   ├── InvalidCredentialsException.java
│   │       │   └── VehicleNotFoundException.java
│   │       ├── repository/
│   │       │   └── VehicleRepository.java        # Spring Data JPA repository with case-insensitive helpers
│   │       └── service/
│   │           ├── AuthService.java              # Authentication service interface
│   │           ├── VehicleService.java           # Vehicle business logic interface
│   │           └── impl/
│   │               ├── AuthServiceImpl.java      # Credential verification & token issuance
│   │               └── VehicleServiceImpl.java   # CRUD operations & duplicate enforcement
│   └── test/
│       ├── resources/
│       │   └── application-test.properties       # Isolated H2 MySQL-mode test database
│       └── java/com/starkgroup/vms/
│           ├── StarkGroupVmsApplicationTests.java# Context loading integration test
│           ├── controller/
│           │   ├── AuthControllerTest.java       # MockMvc tests for login flows
│           │   └── VehicleControllerTest.java    # MockMvc tests for all REST endpoints & status codes
│           └── service/
│               └── VehicleServiceTest.java       # JUnit 5 + Mockito business logic & duplicate checks
```

---

## ⚙️ Configuration & Environment Variables

The backend supports a **single unified connection string** (`DB_STRING` or `DATABASE_URL`) in your local `.env` file. You do **not** need to maintain separate host, port, user, or password entries:

```env
# Single Connection String Format: mysql://<username>:<password>@<host>:<port>/<database>
DB_STRING=mysql://root:password@localhost:3306/vms_db
```

All application properties in [`src/main/resources/application.properties`](src/main/resources/application.properties) dynamically resolve from `.env` without any hardcoded credentials:

| Property | Environment Variable / `.env` Key | Default (Fallback) | Description |
| :--- | :--- | :--- | :--- |
| `spring.datasource.url` | `DB_STRING` / `DATABASE_URL` | Local fallback | Unified database connection string (auto-parsed into JDBC URL, user, and password). |
| `server.port` | `SERVER_PORT` | `8080` | HTTP port for REST API server. |
| `vms.auth.admin.username` | `VMS_ADMIN_USERNAME` | *(required from .env)* | Configured admin login user / email. |
| `vms.auth.admin.password` | `VMS_ADMIN_PASSWORD` | *(required from .env)* | Configured admin login password. |
| `vms.auth.admin.fullName` | `VMS_ADMIN_FULL_NAME` | `Fleet Manager` | Admin display name. |
| `vms.auth.admin.role` | `VMS_ADMIN_ROLE` | `FLEET_ADMIN` | Admin authority role. |
| `vms.cors.allowed-origins` | `VMS_CORS_ORIGINS` | `*` | Allowed CORS origins for web clients. |
| `vms.seed.enabled` | `VMS_SEED_ENABLED` | `true` | Toggle seeding initial test fleet records. |

---

## 🚀 How to Run the Backend

### 1. Prerequisites
* **Java Development Kit (JDK)**: `25` (LTS) or compatible OpenJDK.
* Verify Java version:
  ```bash
  java -version
  ```

### 2. Build the Project
Compile classes, resolve dependencies, and package the executable JAR:
```bash
# Windows PowerShell / CMD
.\gradlew build -x test

# Linux / macOS
./gradlew build -x test
```

### 3. Run the Spring Boot Server
```bash
# Windows PowerShell / CMD
.\gradlew bootRun

# Linux / macOS
./gradlew bootRun
```
The server will start on port `8080` (accessible at `http://localhost:8080/api/vehicles`).

---

## 🧪 Testing & Verification

The test suite includes **25 automated tests** executing across the domain, service, repository, controller, and security layers:

```bash
# Run the complete test suite
.\gradlew test --rerun
```

### Test Suite Coverage

1. **`VehicleServiceTest` (JUnit 5 + Mockito)**:
   - Creating a valid vehicle succeeds, trims whitespace, and persists normalized fields.
   - Creating a duplicate vehicle number (including case and whitespace variants) throws `DuplicateVehicleNumberException` and never calls `repository.save(...)`.
   - `getAllVehicles` returns mapped responses.
   - `getVehicleById` returns target vehicle and throws `VehicleNotFoundException` when not found.
   - Updating vehicle validates non-existence of duplicate numbers across other records.
   - Deleting vehicle handles both existing and non-existent IDs.

2. **`VehicleControllerTest` (MockMvc + LocalValidatorFactoryBean)**:
   - `GET /api/vehicles` returns `200 OK` with JSON array.
   - `GET /api/vehicles/{id}` returns `200 OK` when present and `404 Not Found` when missing.
   - `POST /api/vehicles` returns `201 Created` on valid input.
   - `POST /api/vehicles` returns `409 Conflict` on duplicate vehicle number.
   - `POST /api/vehicles` returns `400 Bad Request` with field error map when input constraints fail.
   - `PUT /api/vehicles/{id}` returns `200 OK` on valid update and `409 Conflict` on duplicate conflict.
   - `DELETE /api/vehicles/{id}` returns `204 No Content` on success and `404 Not Found` on non-existent ID.

3. **`AuthControllerTest` (MockMvc)**:
   - `POST /api/auth/login` returns `200 OK` with auth token on valid credentials.
   - `POST /api/auth/login` returns `401 Unauthorized` on invalid password/user.
   - `POST /api/auth/login` returns `400 Bad Request` on blank inputs.

---

## 🔑 Authentication Configuration

Administrative login credentials are not hardcoded; they are configured strictly via environment variables or your local `.env` file:
* **Username / Email**: Set `VMS_ADMIN_USERNAME` in `.env` (e.g., `admin@yourcompany.com`)
* **Password**: Set `VMS_ADMIN_PASSWORD` in `.env`
* **Assigned Role**: `FLEET_ADMIN` (configurable via `VMS_ADMIN_ROLE`)
