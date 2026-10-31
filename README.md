# Capstone-CS3-backend
Capstone Project# planIT – Backend

Backend REST API for planIT, an IT Curriculum Planner & Course Review Platform for undergraduate students of the Faculty of Information Technology (SIT), King Mongkut's University of Technology Thonburi (KMUTT).

The backend provides APIs for course management, study planning, curriculum tracks, and course reviews. It connects to PostgreSQL for persistent data storage and Redis for caching and other supporting operations.

Frontend repository: Capstone-CS3-frontend

## Tech Stack

* Java 21
* Spring Boot 3.x
* Spring Web (REST API)
* Spring Data JPA / Hibernate
* Spring Security (if enabled)
* PostgreSQL
* Redis
* Maven
* Docker
* Postman (API Testing)

## Services & Ports

| Service | Port | Description |
| --- | --- | --- |
| Backend (Spring Boot) | 8080 | REST API |
| PostgreSQL | 5432 | Primary database |
| Redis | 6379 | Cache / supporting services |

## Prerequisites

* Java 21 (JDK)
* Maven 3.9+ (or Maven Wrapper)
* PostgreSQL
* Redis
* Docker & Docker Compose (for containerized deployment)
* Postman (for API testing)

## Getting Started

**1. Clone the repository**

```bash
git clone https://github.com/<your-org>/Capstone-CS3-backend.git
cd Capstone-CS3-backend

```

**2. Configure Properties and Environment Variables**
The project uses specific configuration profiles for different environments based on your current setup:

* `application-test.properties`: Used for running CI tests.
* `application.properties`: Used for local application development.
* `applications.properties`: Used for the server deployment.

Create a `.env` file or configure environment variables in your local environment.
Example:

```properties
# Application
SERVER_PORT=8080

# PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=planit
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

```

For Docker deployment, use the service/container hostname instead of localhost. Never commit `.env` files or production credentials to GitHub.

**3. Run the Backend**
Using Maven:

```bash
mvn clean install
mvn spring-boot:run

```

Or using Maven Wrapper:

```bash
./mvnw clean install
./mvnw spring-boot:run

```

The backend will run at: `http://localhost:8080`

**4. Test API with Postman**
Use Postman to test and verify the backend REST APIs.

1. Open Postman and create a new HTTP request.
2. Set the request URL to `http://localhost:8080`.
3. Select the appropriate HTTP method (GET, POST, PUT, PATCH, DELETE).
4. Configure headers and request body when required.
5. Send the request and verify the response status and data.

Example: `GET http://localhost:8080/api/courses`
For protected endpoints, include the required authentication token in the `Authorization` header.

## Environment Variables

| Variable | Description | Example |
| --- | --- | --- |
| SERVER_PORT | Backend application port | 8080 |
| DB_HOST | PostgreSQL hostname | db |
| DB_PORT | PostgreSQL port | 5432 |
| DB_NAME | Database name | planit |
| DB_USERNAME | Database username | postgres |
| DB_PASSWORD | Database password | ******** |
| REDIS_HOST | Redis hostname | redis |
| REDIS_PORT | Redis port | 6379 |
| REDIS_PASSWORD | Redis password (optional) | ******** |

## Docker Deployment

The backend uses a multi-stage Docker build to reduce the final image size.

* Build stage: Compiles the application using Maven and JDK.
* Runtime stage: Runs the packaged JAR using a lightweight JRE image.
* Database and Redis configuration are injected through environment variables.

**Build Docker Image**

```bash
docker build -t planit-backend .

```

**Run Docker Container**

```bash
docker run -d \
  --name planit-backend \
  -p 8080:8080 \
  --env-file .env \
  planit-backend

```

Make sure the backend container can access PostgreSQL and Redis through the same Docker network or the appropriate VM network configuration.

## Project Structure

Based on "Screenshot 2569-10-30 at 20.05.51.png", the repository follows this directory layout:

```text
demo/
└── src/
    └── main/
        ├── java/com/example/demo/
        │   ├── controllers/      # REST API controllers
        │   ├── dtos/             # Request and response objects
        │   ├── entity/           # JPA entities
        │   ├── exceptions/       # Custom exceptions and exception handlers
        │   ├── repository/       # Data access layer interfaces
        │   ├── services/         # Business logic implementation
        │   └── CapstoneApplication.java
        └── resources/
            ├── static/
            ├── templates/
            ├── application-test.properties    # Properties for CI testing
            ├── application.properties         # Properties for local application development
            ├── application.properties.example # Example template for configuration
            └── applications.properties        # Properties for Server deployment

```

## Development Conventions

**Branches (GitHub Flow)**

* `main` – Stable branch, ready for deployment.
* `feature/SCRUM-<id>-short-name` – One branch per Jira task.
* Pull Requests must be reviewed and pass CI before merging into main.

**Commit Messages**
Follow Conventional Commits:

* `feat`: New features
* `fix`: Bug fixes
* `chore`: Maintenance tasks
* `refactor`: Code improvements
* `test`: Add or update tests
* `docs`: Documentation updates

**API Development**

* Follow RESTful API conventions.
* Use DTOs for request and response payloads.
* Keep business logic inside the service layer.
* Use repositories for database operations.
* Handle exceptions consistently.
* Validate incoming request data.
* Keep API contracts consistent with the frontend.

## Deployment

The backend is packaged as a Docker image and deployed to the SIT VM.

* Spring Boot application listens on port 8080.
* PostgreSQL is accessed through port 5432.
* Redis is accessed through port 6379.
* Application configuration is injected through environment variables and mapped property files.
* The frontend communicates with the backend through the configured API URL. Spring Boot (Planner &amp; Review System)
