# EMS Microservices — Trainee Guide

**Tech Stack:** Java 17 · Spring Boot 3.2 · Spring Cloud 2023 · Spring Data REST · H2 · Eureka · Spring Cloud Gateway · Maven Multi-Module

---

## Table of Contents

1. [What Is This Project?](#1-what-is-this-project)
2. [Microservices vs Monolith — The Big Picture](#2-microservices-vs-monolith--the-big-picture)
3. [Tech Stack Explained](#3-tech-stack-explained)
4. [Project Structure](#4-project-structure)
5. [Architecture Diagram](#5-architecture-diagram)
6. [Business Domain & Entities](#6-business-domain--entities)
7. [How Each Concept Is Implemented](#7-how-each-concept-is-implemented)
8. [Prerequisites & Installation](#8-prerequisites--installation)
9. [How to Run](#9-how-to-run)
10. [Verifying Everything Works](#10-verifying-everything-works)
11. [Complete API Reference](#11-complete-api-reference)
12. [Sample Request & Response Walkthrough](#12-sample-request--response-walkthrough)
13. [H2 In-Browser SQL Console](#13-h2-in-browser-sql-console)
14. [Actuator — Health & Monitoring](#14-actuator--health--monitoring)
15. [Common Errors & Fixes](#15-common-errors--fixes)
16. [Design Decisions Worth Understanding](#16-design-decisions-worth-understanding)
17. [What to Build Next (Learning Path)](#17-what-to-build-next-learning-path)
18. [Quick-Reference Cheat Sheet](#18-quick-reference-cheat-sheet)

---

## 1. What Is This Project?

This is a **Hello-World-level Employee Management System (EMS)** built as a set of Spring Boot microservices. The goal is not a production-ready application — it is a learning sandbox that demonstrates every major Spring Cloud microservices concept in the simplest possible way.

By the end of studying this project, a trainee will understand:

- Why microservices exist and what problems they solve
- How services register themselves and discover each other (Eureka)
- How a single API Gateway acts as the front door for all services
- How Spring Data REST eliminates boilerplate controller code
- How each service owns its own isolated database
- How to run, test, and debug a multi-service system locally

---

## 2. Microservices vs Monolith — The Big Picture

### Monolith (the old way)

```
┌───────────────────────────────────────────────────┐
│                  EMS Application                  │
│                                                   │
│  EmployeeController  DepartmentController  etc.   │
│  EmployeeService     DepartmentService            │
│  EmployeeRepo        DepartmentRepo               │
│                                                   │
│              Single shared database               │
└───────────────────────────────────────────────────┘
         Deployed as one big WAR/JAR
```

Problems with a monolith as it grows:
- One bug can bring down the entire application
- You must redeploy everything even for a tiny change
- Different parts of the app cannot scale independently
- Teams step on each other's code

### Microservices (the new way)

```
┌─────────────────┐  ┌──────────────────┐  ┌─────────────────┐
│ Employee        │  │ Department       │  │ Project         │
│ Service         │  │ Service          │  │ Service         │
│                 │  │                  │  │                 │
│ Own DB          │  │ Own DB           │  │ Own DB          │
└─────────────────┘  └──────────────────┘  └─────────────────┘
       Deployed independently — scale independently
```

Benefits:
- Each service can be deployed, scaled, and updated independently
- A failure in one service does not crash the others
- Teams can own separate services with no code conflicts
- Each service can even use a different programming language or database

**Trade-off:** More moving parts to manage. That is exactly why tools like Eureka and API Gateway exist.

---

## 3. Tech Stack Explained

### Java 17
The LTS (Long-Term Support) release of Java. Spring Boot 3.x requires Java 17 minimum. Uses records, sealed classes, text blocks, and pattern matching — but this project uses only the basics.

### Spring Boot 3.2
The opinionated framework that auto-configures almost everything. You declare what you need (via annotations and `pom.xml` dependencies); Spring Boot wires it all together.

### Spring Cloud 2023.0.1
A collection of libraries that solve distributed systems problems: service discovery, routing, load balancing, circuit breakers, etc. Built on top of Spring Boot.

### Spring Data REST
An extension of Spring Data JPA. You write only a `JpaRepository` interface — Spring Data REST automatically exposes it as a full REST API with CRUD operations, pagination, sorting, and custom search endpoints. **Zero controller code needed.**

### H2 (In-Memory Database)
A pure-Java database that starts inside your application and lives only in RAM. Perfect for demos and testing — no installation needed. The data disappears when the service restarts, which is intentional here.

### Netflix Eureka (Service Registry)
A server that acts as a phone book for microservices. Each service announces itself to Eureka on startup ("I am `employee-service` running on `192.168.1.5:8081`"). Other services and the gateway ask Eureka for the current address of any service by name.

### Spring Cloud Gateway
The single entry point for all external requests. Clients (browsers, Postman, mobile apps) only need to know one URL: `http://localhost:8080`. The gateway routes each request to the correct downstream service using rules you define in `application.yml`.

### Spring Boot Actuator
Adds production-ready endpoints to every service: `/actuator/health`, `/actuator/info`, `/actuator/metrics`, etc. Used for monitoring, health checks, and debugging without writing any code.

### Maven Multi-Module
A single root `pom.xml` that declares five sub-modules. Running `mvn clean package` from the root builds all five JARs in one shot with shared dependency management.

---

## 4. Project Structure

```
ems-microservices/                      ← Root (parent POM only)
│
├── pom.xml                             ← Parent POM; shared Spring Boot + Cloud versions
│
├── eureka-server/                      ← Module 1: Service Registry
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ems/eureka/
│       │   └── EurekaServerApplication.java    ← @EnableEurekaServer
│       └── resources/
│           └── application.yml                 ← port 8761, don't register self
│
├── api-gateway/                        ← Module 2: API Gateway
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ems/gateway/
│       │   └── ApiGatewayApplication.java
│       └── resources/
│           └── application.yml                 ← port 8080, route rules, lb:// URIs
│
├── employee-service/                   ← Module 3: Employee domain
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ems/employee/
│       │   ├── EmployeeServiceApplication.java ← seed data here
│       │   ├── entity/
│       │   │   └── Employee.java               ← @Entity
│       │   └── repository/
│       │       └── EmployeeRepository.java     ← @RepositoryRestResource
│       └── resources/
│           └── application.yml                 ← port 8081, H2, Eureka client
│
├── department-service/                 ← Module 4: Department + Job domain
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ems/department/
│       │   ├── DepartmentServiceApplication.java
│       │   ├── entity/
│       │   │   ├── Department.java
│       │   │   └── Job.java
│       │   └── repository/
│       │       ├── DepartmentRepository.java
│       │       └── JobRepository.java
│       └── resources/
│           └── application.yml                 ← port 8082
│
└── project-service/                    ← Module 5: Project domain
    ├── pom.xml
    └── src/main/
        ├── java/com/ems/project/
        │   ├── ProjectServiceApplication.java
        │   ├── entity/
        │   │   └── Project.java
        │   └── repository/
        │       └── ProjectRepository.java
        └── resources/
            └── application.yml                 ← port 8083
```

### File count summary

| Module | Java files | Config files |
|--------|-----------|--------------|
| eureka-server | 1 | 1 |
| api-gateway | 1 | 1 |
| employee-service | 3 | 1 |
| department-service | 4 | 1 |
| project-service | 3 | 1 |
| **Total** | **12** | **5** |

This is the power of Spring Data REST — a fully functional microservices system in 17 files.

---

## 5. Architecture Diagram

```
                    ╔══════════════════════════════════════╗
                    ║          EUREKA SERVER               ║
                    ║          port 8761                   ║
                    ║                                      ║
                    ║  Registry (phone book):              ║
                    ║  employee-service  → 127.0.0.1:8081  ║
                    ║  department-service→ 127.0.0.1:8082  ║
                    ║  project-service   → 127.0.0.1:8083  ║
                    ║  api-gateway       → 127.0.0.1:8080  ║
                    ╚═══════════╤══════════════════════════╝
                                │
              ┌─────────────────┼──────────────────┐
              │ register on     │ register on       │ register on
              │ startup         │ startup           │ startup
              ▼                 ▼                   ▼
   ┌──────────────────┐ ┌───────────────────┐ ┌──────────────────┐
   │  EMPLOYEE-SVC    │ │  DEPARTMENT-SVC   │ │  PROJECT-SVC     │
   │  port 8081       │ │  port 8082        │ │  port 8083       │
   │                  │ │                   │ │                  │
   │  Entity:         │ │  Entities:        │ │  Entity:         │
   │  Employee        │ │  Department       │ │  Project         │
   │  (id, firstName, │ │  (id, name,       │ │  (id, name,      │
   │   lastName,      │ │   location)       │ │   description,   │
   │   email,         │ │                   │ │   startDate,     │
   │   departmentId,  │ │  Job              │ │   endDate,       │
   │   jobId)         │ │  (id, title,      │ │   status,        │
   │                  │ │   description,    │ │   departmentId)  │
   │  H2: employeedb  │ │   minSalary,      │ │                  │
   │                  │ │   maxSalary)      │ │  H2: projectdb   │
   └──────────────────┘ │                   │ └──────────────────┘
                        │  H2: departmentdb │
                        └───────────────────┘
              ▲                 ▲                   ▲
              └─────────────────┼───────────────────┘
                                │ routes via lb:// (asks Eureka for address)
                    ╔═══════════╧══════════════════════════╗
                    ║            API GATEWAY               ║
                    ║            port 8080                 ║
                    ║                                      ║
                    ║  /api/employees/**  → employee-svc   ║
                    ║  /api/departments/**→ department-svc ║
                    ║  /api/jobs/**       → department-svc ║
                    ║  /api/projects/**   → project-svc    ║
                    ╚═══════════╤══════════════════════════╝
                                │
                    ┌───────────┴───────────┐
                    │   External Clients    │
                    │  (Postman / Browser)  │
                    └───────────────────────┘
```

### Request flow (step by step)

```
Postman → GET http://localhost:8080/api/employees

Step 1: Request arrives at API Gateway (port 8080)
Step 2: Gateway matches route: /api/employees/** → lb://employee-service
Step 3: Gateway asks Eureka: "Where is employee-service?"
Step 4: Eureka replies: "127.0.0.1:8081"
Step 5: Gateway strips /api prefix → forwards GET http://127.0.0.1:8081/employees
Step 6: Employee-service queries H2 → returns JSON
Step 7: Gateway returns response to Postman
```

---

## 6. Business Domain & Entities

### The EMS domain

An organisation has **Departments**, each department has **Jobs** (roles), **Employees** fill those jobs, and **Projects** are run by departments.

### Entity relationship (logical, not JPA)

```
Department  ──< Job
     │
     └──< Employee  (belongs to one Department, has one Job)
     │
     └──< Project   (owned by one Department)
```

### Employee

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Auto-generated primary key |
| firstName | String | Employee's first name |
| lastName | String | Employee's last name |
| email | String | Unique email address |
| departmentId | Long | FK reference to Department (in dept-service) |
| jobId | Long | FK reference to Job (in dept-service) |

### Department

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Auto-generated primary key |
| name | String | Department name, e.g. "Engineering" |
| location | String | Physical location, e.g. "Building A" |

### Job

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Auto-generated primary key |
| title | String | Job title, e.g. "Software Engineer" |
| description | String | What the job involves |
| minSalary | Double | Salary band minimum |
| maxSalary | Double | Salary band maximum |

### Project

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Auto-generated primary key |
| name | String | Project name |
| description | String | What the project is about |
| startDate | LocalDate | Start date (format: `YYYY-MM-DD`) |
| endDate | LocalDate | End date |
| status | String | PLANNING / ACTIVE / COMPLETED |
| departmentId | Long | FK reference to Department (in dept-service) |

### Seed data (loaded automatically on startup)

**Employees** (employee-service)
| id | firstName | lastName | email | departmentId | jobId |
|----|-----------|----------|-------|--------------|-------|
| 1 | Alice | Johnson | alice@ems.com | 1 | 1 |
| 2 | Bob | Smith | bob@ems.com | 1 | 2 |
| 3 | Carol | Williams | carol@ems.com | 2 | 3 |

**Departments** (department-service)
| id | name | location |
|----|------|----------|
| 1 | Engineering | Building A |
| 2 | Human Resources | Building B |
| 3 | Finance | Building C |

**Jobs** (department-service)
| id | title | minSalary | maxSalary |
|----|-------|-----------|-----------|
| 1 | Software Engineer | 60,000 | 120,000 |
| 2 | HR Manager | 50,000 | 90,000 |
| 3 | Financial Analyst | 55,000 | 100,000 |

**Projects** (project-service)
| id | name | status | departmentId |
|----|------|--------|--------------|
| 1 | EMS Platform | ACTIVE | 1 |
| 2 | HR Automation | PLANNING | 2 |
| 3 | Finance Reporting | COMPLETED | 3 |

---

## 7. How Each Concept Is Implemented

### 7.1 Eureka Server — `@EnableEurekaServer`

```java
// eureka-server/src/main/java/com/ems/eureka/EurekaServerApplication.java

@SpringBootApplication
@EnableEurekaServer          // ← This one annotation turns it into a registry
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```yaml
# eureka-server/src/main/resources/application.yml
eureka:
  client:
    register-with-eureka: false   # The server does NOT register itself
    fetch-registry: false         # The server does NOT fetch from itself
```

### 7.2 Eureka Client — every microservice registers itself

```yaml
# employee-service/src/main/resources/application.yml
spring:
  application:
    name: employee-service      # ← This is the name registered in Eureka

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/   # ← Where to register
```

No Java code needed — just these two YAML properties. Spring Cloud auto-configures the rest.

### 7.3 API Gateway — routing rules

```yaml
# api-gateway/src/main/resources/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: employee-service
          uri: lb://employee-service     # lb:// = Load-Balanced (asks Eureka)
          predicates:
            - Path=/api/employees/**     # Match this URL pattern
          filters:
            - StripPrefix=1              # Remove /api before forwarding
```

What `StripPrefix=1` does:
```
Incoming:  GET /api/employees/1
Forwarded: GET /employees/1        (stripped one path segment)
```

### 7.4 Spring Data REST — zero-controller REST API

```java
// employee-service/src/main/java/com/ems/employee/repository/EmployeeRepository.java

@RepositoryRestResource(collectionResourceRel = "employees", path = "employees")
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Custom search — exposed automatically at:
    // GET /employees/search/findByDepartmentId?departmentId=1
    List<Employee> findByDepartmentId(@Param("departmentId") Long departmentId);
}
```

Spring Data REST automatically generates:
- `GET /employees` — list all (with pagination)
- `GET /employees/{id}` — get one
- `POST /employees` — create
- `PUT /employees/{id}` — full replace
- `PATCH /employees/{id}` — partial update
- `DELETE /employees/{id}` — delete
- `GET /employees/search` — list available custom searches
- `GET /employees/search/findByDepartmentId?departmentId=1` — custom search

### 7.5 H2 In-Memory Database

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:employeedb    # mem: = in-memory (no file)
    username: sa
    password:                      # empty password

  h2:
    console:
      enabled: true                # Browser-based SQL console
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: create-drop        # Drop and recreate schema on every startup
```

### 7.6 Seed Data — `CommandLineRunner`

```java
@Bean
CommandLineRunner seedData(EmployeeRepository repo) {
    return args -> {
        // This runs once, automatically, after the application starts
        repo.save(new Employee("Alice", "Johnson", "alice@ems.com", 1L, 1L));
    };
}
```

### 7.7 Actuator

Just add the dependency in `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

And expose all endpoints in `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

That's it — `/actuator/health`, `/actuator/info`, `/actuator/metrics` and many more are now available.

---

## 8. Prerequisites & Installation

### Check Java version
```bash
java -version
# Should show: openjdk version "17.x.x" or higher
```

If not installed, download from https://adoptium.net (Eclipse Temurin 17 LTS is recommended).

### Check Maven version
```bash
mvn -version
# Should show: Apache Maven 3.8.x or higher
```

If not installed, download from https://maven.apache.org/download.cgi or use your OS package manager:
```bash
# macOS (Homebrew)
brew install maven

# Ubuntu/Debian
sudo apt install maven

# Windows
# Download ZIP from maven.apache.org, extract, add bin/ to PATH
```

### Unzip the project
```bash
unzip ems-microservices.zip
cd ems-microservices
```

### Build all modules (downloads dependencies, compiles)
```bash
mvn clean package -DskipTests
```

This will download Spring Boot, Spring Cloud, and all dependencies from Maven Central (~200 MB on first run). Subsequent builds are fast.

---

## 9. How to Run

> **Critical rule:** Always start Eureka Server first. Other services register with it on boot. If Eureka is not up, services will log connection-refused warnings (they retry every 30s, so they self-heal once Eureka starts, but it is cleaner to start in order).

### Option A — Five terminals (recommended for learning, you see each service's logs)

Open five terminal windows or tabs.

**Terminal 1 — Eureka Server**
```bash
cd ems-microservices/eureka-server
mvn spring-boot:run
# Wait for: "Started EurekaServerApplication in X seconds"
# Then open: http://localhost:8761
```

**Terminal 2 — API Gateway**
```bash
cd ems-microservices/api-gateway
mvn spring-boot:run
# Wait for: "Started ApiGatewayApplication in X seconds"
```

**Terminal 3 — Employee Service**
```bash
cd ems-microservices/employee-service
mvn spring-boot:run
# Look for: "✅ Employee Service: seed data loaded."
```

**Terminal 4 — Department Service**
```bash
cd ems-microservices/department-service
mvn spring-boot:run
# Look for: "✅ Department Service: seed data loaded."
```

**Terminal 5 — Project Service**
```bash
cd ems-microservices/project-service
mvn spring-boot:run
# Look for: "✅ Project Service: seed data loaded."
```

### Option B — JAR files (faster restarts, less terminal noise)

```bash
# Build once from root
cd ems-microservices
mvn clean package -DskipTests

# Then run each JAR (5 terminals still recommended)
java -jar eureka-server/target/eureka-server-1.0.0.jar
java -jar api-gateway/target/api-gateway-1.0.0.jar
java -jar employee-service/target/employee-service-1.0.0.jar
java -jar department-service/target/department-service-1.0.0.jar
java -jar project-service/target/project-service-1.0.0.jar
```

### Stopping a service
Press `Ctrl + C` in its terminal. The H2 data is lost on shutdown (by design).

---

## 10. Verifying Everything Works

### Step 1 — Check Eureka Dashboard

Open http://localhost:8761 in your browser.

Under **"Instances currently registered with Eureka"** you should see:

| Application | Status |
|-------------|--------|
| EMPLOYEE-SERVICE | UP |
| DEPARTMENT-SERVICE | UP |
| PROJECT-SERVICE | UP |
| API-GATEWAY | UP |

If a service is missing, it hasn't finished registering yet — wait 15-30 seconds and refresh.

### Step 2 — Check health endpoints

Open each of these in your browser (or use curl):

```bash
curl http://localhost:8081/actuator/health
# Expected: {"status":"UP"}

curl http://localhost:8082/actuator/health
# Expected: {"status":"UP"}

curl http://localhost:8083/actuator/health
# Expected: {"status":"UP"}

curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### Step 3 — Hit the gateway and get data

```bash
curl http://localhost:8080/api/employees
```

Expected response (HAL+JSON format):
```json
{
  "_embedded" : {
    "employees" : [
      {
        "firstName" : "Alice",
        "lastName" : "Johnson",
        "email" : "alice@ems.com",
        "departmentId" : 1,
        "jobId" : 1,
        "_links" : {
          "self" : { "href" : "http://localhost:8080/api/employees/1" },
          "employee" : { "href" : "http://localhost:8080/api/employees/1" }
        }
      }
    ]
  },
  "_links" : {
    "self" : { "href" : "http://localhost:8080/api/employees" },
    "profile" : { "href" : "http://localhost:8080/api/profile/employees" },
    "search" : { "href" : "http://localhost:8080/api/employees/search" }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 3,
    "totalPages" : 1,
    "number" : 0
  }
}
```

This is **HAL+JSON** (Hypertext Application Language) — a standard where every response includes `_links` so clients can discover what they can do next without hardcoding URLs.

---

## 11. Complete API Reference

All URLs below go through the **API Gateway on port 8080**.

> **Note:** Spring Data REST returns HAL+JSON. Set `Content-Type: application/json` on POST/PUT/PATCH requests in Postman.

---

### Employee Service (`/api/employees`)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/employees` | List all employees (paginated) |
| GET | `/api/employees?page=0&size=10&sort=lastName,asc` | Paginated + sorted |
| GET | `/api/employees/{id}` | Get single employee |
| POST | `/api/employees` | Create new employee |
| PUT | `/api/employees/{id}` | Replace entire employee record |
| PATCH | `/api/employees/{id}` | Update specific fields only |
| DELETE | `/api/employees/{id}` | Delete employee |
| GET | `/api/employees/search` | List available custom searches |
| GET | `/api/employees/search/findByDepartmentId?departmentId=1` | Employees in dept 1 |
| GET | `/api/employees/search/findByJobId?jobId=2` | Employees with job 2 |

**POST body:**
```json
{
  "firstName": "David",
  "lastName": "Brown",
  "email": "david@ems.com",
  "departmentId": 1,
  "jobId": 1
}
```

**PATCH body (partial update — only send fields you want to change):**
```json
{
  "email": "david.brown@ems.com"
}
```

---

### Department Service (`/api/departments`)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/departments` | List all departments |
| GET | `/api/departments/{id}` | Get single department |
| POST | `/api/departments` | Create new department |
| PUT | `/api/departments/{id}` | Replace department |
| PATCH | `/api/departments/{id}` | Update fields |
| DELETE | `/api/departments/{id}` | Delete department |
| GET | `/api/departments/search/findByName?name=Engineering` | Find by name |

**POST body:**
```json
{
  "name": "DevOps",
  "location": "Building D"
}
```

---

### Job endpoints (`/api/jobs` — served by department-service)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/jobs` | List all jobs |
| GET | `/api/jobs/{id}` | Get single job |
| POST | `/api/jobs` | Create new job |
| PUT | `/api/jobs/{id}` | Replace job |
| PATCH | `/api/jobs/{id}` | Update fields |
| DELETE | `/api/jobs/{id}` | Delete job |
| GET | `/api/jobs/search/findByTitle?title=Software Engineer` | Find by title |

**POST body:**
```json
{
  "title": "DevOps Engineer",
  "description": "CI/CD pipelines and infrastructure",
  "minSalary": 70000,
  "maxSalary": 130000
}
```

---

### Project Service (`/api/projects`)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/projects` | List all projects |
| GET | `/api/projects/{id}` | Get single project |
| POST | `/api/projects` | Create new project |
| PUT | `/api/projects/{id}` | Replace project |
| PATCH | `/api/projects/{id}` | Update fields |
| DELETE | `/api/projects/{id}` | Delete project |
| GET | `/api/projects/search/findByStatus?status=ACTIVE` | Filter by status |
| GET | `/api/projects/search/findByDepartmentId?departmentId=1` | Projects of dept 1 |

**POST body:**
```json
{
  "name": "Mobile App",
  "description": "Employee self-service mobile application",
  "startDate": "2024-06-01",
  "endDate": "2024-12-31",
  "status": "PLANNING",
  "departmentId": 1
}
```

---

### Direct-to-service URLs (bypassing the gateway — useful for debugging)

| Service | URL |
|---------|-----|
| Employee | http://localhost:8081/employees |
| Department | http://localhost:8082/departments |
| Job | http://localhost:8082/jobs |
| Project | http://localhost:8083/projects |

---

## 12. Sample Request & Response Walkthrough

### Scenario: Onboard a new employee

**Step 1 — Confirm the department exists**
```
GET http://localhost:8080/api/departments/1
```
Response:
```json
{
  "name": "Engineering",
  "location": "Building A",
  "_links": { ... }
}
```

**Step 2 — Confirm the job exists**
```
GET http://localhost:8080/api/jobs/1
```
Response:
```json
{
  "title": "Software Engineer",
  "description": "Develops software",
  "minSalary": 60000.0,
  "maxSalary": 120000.0,
  "_links": { ... }
}
```

**Step 3 — Create the employee**
```
POST http://localhost:8080/api/employees
Content-Type: application/json

{
  "firstName": "Eve",
  "lastName": "Davis",
  "email": "eve@ems.com",
  "departmentId": 1,
  "jobId": 1
}
```
Response (HTTP 201 Created):
```json
{
  "firstName": "Eve",
  "lastName": "Davis",
  "email": "eve@ems.com",
  "departmentId": 1,
  "jobId": 1,
  "_links": {
    "self": { "href": "http://localhost:8080/api/employees/4" },
    "employee": { "href": "http://localhost:8080/api/employees/4" }
  }
}
```

**Step 4 — Verify all Engineering employees**
```
GET http://localhost:8080/api/employees/search/findByDepartmentId?departmentId=1
```
Should return Alice, Bob, and Eve.

**Step 5 — Correct a typo in Eve's email (partial update)**
```
PATCH http://localhost:8080/api/employees/4
Content-Type: application/json

{
  "email": "eve.davis@ems.com"
}
```
Response (HTTP 200 OK) — only email changes, other fields untouched.

---

## 13. H2 In-Browser SQL Console

Each service has its own H2 console where you can run SQL directly on the in-memory database.

| Service | Console URL | JDBC URL |
|---------|-------------|----------|
| Employee | http://localhost:8081/h2-console | `jdbc:h2:mem:employeedb` |
| Department | http://localhost:8082/h2-console | `jdbc:h2:mem:departmentdb` |
| Project | http://localhost:8083/h2-console | `jdbc:h2:mem:projectdb` |

**Login settings** (same for all):
- Driver Class: `org.h2.Driver`
- JDBC URL: _(as above, must match exactly)_
- User Name: `sa`
- Password: _(leave blank)_

**Useful SQL queries to try:**

```sql
-- See all tables in the database
SHOW TABLES;

-- See employees
SELECT * FROM EMPLOYEES;

-- See the auto-generated schema
SHOW COLUMNS FROM EMPLOYEES;

-- Count records
SELECT COUNT(*) FROM EMPLOYEES;

-- Filter by department
SELECT * FROM EMPLOYEES WHERE DEPARTMENT_ID = 1;
```

> **Tip:** H2 maps camelCase Java fields to `SNAKE_CASE` column names. `departmentId` → `DEPARTMENT_ID`.

---

## 14. Actuator — Health & Monitoring

Spring Boot Actuator exposes information about your running application without writing any code. All endpoints are prefixed with `/actuator/`.

### Key endpoints

| Endpoint | What it shows | Example URL |
|----------|--------------|-------------|
| `/actuator/health` | UP/DOWN status, H2 and disk details | http://localhost:8081/actuator/health |
| `/actuator/info` | App metadata | http://localhost:8081/actuator/info |
| `/actuator/env` | All configuration properties | http://localhost:8081/actuator/env |
| `/actuator/beans` | All Spring beans loaded | http://localhost:8081/actuator/beans |
| `/actuator/mappings` | All URL mappings | http://localhost:8081/actuator/mappings |
| `/actuator/metrics` | JVM and app metrics | http://localhost:8081/actuator/metrics |
| `/actuator/metrics/jvm.memory.used` | Specific metric | http://localhost:8081/actuator/metrics/jvm.memory.used |

### Gateway-specific

| Endpoint | What it shows |
|----------|--------------|
| http://localhost:8080/actuator/gateway/routes | All configured routes |
| http://localhost:8080/actuator/gateway/globalfilters | Active global filters |

### Health response with details

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 100000000000,
        "threshold": 10485760,
        "path": "."
      }
    }
  }
}
```

---

## 15. Common Errors & Fixes

### "Connection refused" to Eureka on service startup

```
com.sun.jersey.api.client.ClientHandlerException: 
java.net.ConnectException: Connection refused
```

**Cause:** Eureka Server is not running yet.
**Fix:** Start `eureka-server` first and wait for it to say "Started". Then start the other services.

---

### Service shows in Eureka but gateway returns 503

```json
{"timestamp":"...","path":"/api/employees","status":503,"error":"Service Unavailable"}
```

**Cause:** Eureka has the registration, but the gateway's load balancer cache hasn't refreshed yet. Also happens if the service started but is still initialising.
**Fix:** Wait 30 seconds and retry. Eureka has a heartbeat interval that causes this delay.

---

### H2 console login fails / "Database not found"

**Cause:** The JDBC URL in the H2 console login page does not match the one in `application.yml`.
**Fix:** Make sure the JDBC URL is exactly `jdbc:h2:mem:employeedb` (not the default `jdbc:h2:~/test`).

---

### `Port 8761 already in use`

**Cause:** A previous Eureka Server is still running.
**Fix:**
```bash
# Find the process using the port
lsof -i :8761        # macOS / Linux
netstat -ano | findstr :8761   # Windows

# Kill it (use the PID shown)
kill -9 <PID>        # macOS / Linux
taskkill /PID <PID> /F         # Windows
```

---

### `java.lang.UnsupportedClassVersionError`

**Cause:** The JAR was compiled with Java 17 but you are running it with Java 11 or older.
**Fix:** `java -version` must show 17+. Set `JAVA_HOME` if you have multiple JDKs installed.

---

### POST returns 405 Method Not Allowed

**Cause:** The URL is wrong — e.g. posting to `/api/employees/1` instead of `/api/employees`.
**Fix:** POST goes to the collection URL (`/api/employees`), not the item URL (`/api/employees/1`).

---

### After service restart, IDs restart from 1

This is expected — H2 is in-memory. The `ddl-auto: create-drop` setting drops and recreates the schema every time the service restarts, and the `CommandLineRunner` re-seeds from ID 1. For persistence, switch to a file-based H2 (`jdbc:h2:file:./data/employeedb`) or a real database.

---

## 16. Design Decisions Worth Understanding

### 16.1 No shared database

Each service has its own completely separate H2 database. `employee-service` cannot query `department-service`'s tables with JPA. This is fundamental to microservices: **database per service** is the pattern.

When `Employee` needs to reference a `Department`, it stores only `departmentId` (a `Long`). To fetch the full department details, a production application would call `department-service`'s REST API — this is called the **API Composition** pattern.

### 16.2 No Lombok

All entity classes have manual getters, setters, and constructors. This makes the code more explicit and easier to understand without IDE plugins. In a real project you would typically use Lombok's `@Data`, `@Getter`, `@Setter`, etc.

### 16.3 Why Spring Data REST and not controllers?

Spring Data REST is chosen here to minimise boilerplate. In a real production application, you would typically write explicit `@RestController` classes because you need full control over:
- Input validation (`@Valid`, `BindingResult`)
- DTO transformation (not exposing your entity structure directly)
- Custom error responses
- Business logic before persistence

For this demo, Spring Data REST lets trainees focus on the infrastructure (Eureka, Gateway) without distraction.

### 16.4 `StripPrefix=1` in the gateway

The gateway listens on `/api/employees/**` but the employee service serves on `/employees/**`. The `StripPrefix=1` filter removes the first path segment (`/api`) before forwarding. Without this, the request would arrive at employee-service as `/api/employees` and produce a 404, because there is no `/api` prefix configured in the service.

### 16.5 `lb://` vs `http://` in gateway routes

```yaml
# With service discovery (this project)
uri: lb://employee-service     # Dynamic — Eureka resolves the address

# Without service discovery (hardcoded)
uri: http://localhost:8081     # Static — breaks if service moves to another host/port
```

The `lb://` prefix tells Spring Cloud Gateway to use the load balancer, which in turn uses Eureka to find the service's current address.

---

## 17. What to Build Next (Learning Path)

Once you are comfortable with this demo, here is a structured learning path:

### Level 1 — Strengthen the foundation

- [ ] Add `@NotBlank`, `@Email` validation on entity fields and observe how Spring Data REST handles it
- [ ] Write a proper `@RestController` for one entity instead of using Spring Data REST
- [ ] Add a DTO layer: separate the API response shape from the JPA entity
- [ ] Replace H2 with PostgreSQL (add Docker, update `application.yml`, change driver dependency)

### Level 2 — Add inter-service communication

- [ ] Add `spring-boot-starter-web` + `RestTemplate` or `WebClient` to `employee-service`
- [ ] When fetching an employee, also call `department-service` to get the department name and enrich the response
- [ ] Try `spring-cloud-openfeign` — a declarative HTTP client that looks like a Java interface

### Level 3 — Resilience & fault tolerance

- [ ] Add `spring-cloud-starter-circuitbreaker-resilience4j`
- [ ] Simulate `department-service` going down — observe the fallback behaviour in employee-service
- [ ] Understand: open circuit → half-open circuit → closed circuit

### Level 4 — Observability

- [ ] Add `spring-cloud-sleuth` (deprecated in Spring Boot 3 — use Micrometer Tracing instead)
- [ ] Add a Zipkin server (`docker run -d -p 9411:9411 openzipkin/zipkin`)
- [ ] See how a single API call traces through gateway → employee-service as a distributed trace

### Level 5 — Containerisation

- [ ] Write a `Dockerfile` for one service
- [ ] Write `docker-compose.yml` to start all five services with one command
- [ ] Add a Postgres container per service in docker-compose
- [ ] Push images to Docker Hub

### Level 6 — Security

- [ ] Add Spring Security to the gateway
- [ ] Issue JWT tokens at login
- [ ] Validate JWT at the gateway before routing to downstream services

---

## 18. Quick-Reference Cheat Sheet

### Ports

| Service | Port | Primary URL |
|---------|------|-------------|
| Eureka Server | 8761 | http://localhost:8761 |
| API Gateway | 8080 | http://localhost:8080 |
| Employee Service | 8081 | http://localhost:8081 |
| Department Service | 8082 | http://localhost:8082 |
| Project Service | 8083 | http://localhost:8083 |

### Maven commands

```bash
mvn clean package -DskipTests    # Build all modules
mvn spring-boot:run              # Run from a module directory
mvn clean                        # Delete target/ directories
mvn dependency:tree              # Show all transitive dependencies
```

### curl quick tests

```bash
# Eureka — check registered services
curl http://localhost:8761/eureka/apps | grep -i '<app>'

# Gateway — get all employees
curl http://localhost:8080/api/employees | python3 -m json.tool

# Direct — bypass gateway
curl http://localhost:8081/employees

# Create employee
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@ems.com","departmentId":1,"jobId":1}'

# Health check
curl http://localhost:8081/actuator/health

# Gateway routes
curl http://localhost:8080/actuator/gateway/routes | python3 -m json.tool
```

### Key annotations

| Annotation | Where | What it does |
|-----------|-------|-------------|
| `@SpringBootApplication` | Main class | Enables component scan + auto-configuration |
| `@EnableEurekaServer` | Eureka main class | Turns this app into a Eureka registry |
| `@Entity` | Domain class | Maps class to a DB table |
| `@Id` + `@GeneratedValue` | Entity field | Auto-generated primary key |
| `@RepositoryRestResource` | Repository interface | Exposes repo as REST API |
| `@Param` | Repository method parameter | Binds URL query param to method parameter |
| `@Bean` | Method in `@SpringBootApplication` | Registers the return value as a Spring bean |

### Spring Cloud version compatibility

| Spring Boot | Spring Cloud |
|-------------|-------------|
| 3.2.x | 2023.0.x |
| 3.1.x | 2022.0.x |
| 2.7.x | 2021.0.x |
| 2.6.x | 2021.0.x |

> **Warning:** Never mix Spring Boot and Spring Cloud versions that are not listed as compatible. It causes subtle, hard-to-debug runtime errors.

---

*Happy learning! The best way to understand this project is to break it intentionally — stop one service, change a port, add a field — and observe what happens.*
