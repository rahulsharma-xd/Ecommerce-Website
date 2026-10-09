# E-commerce Application

A Spring Boot-based e-commerce backend application.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Getting Started

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on port **8081**.

### Building the Application

```bash
mvn clean package
```

### Running the JAR

```bash
java -jar target/ecommerce-app-1.0.0.jar
```

## API Endpoints

### Test Endpoint

**GET** `/api/test`

Test endpoint to verify the application is running.

**Response:**
```json
{
  "message": "Hello from E-commerce API!",
  "status": "success",
  "port": "8081"
}
```

**Example:**
```bash
curl http://localhost:8081/api/test
```

## Configuration

The application can be configured through `src/main/resources/application.properties`:

- `server.port` - Server port (default: 8081)
- `spring.application.name` - Application name

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ecommerce/app/
│   │   │       ├── EcommerceApplication.java
│   │   │       └── controller/
│   │   │           └── TestController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
└── pom.xml
```

## Technologies Used

- Spring Boot 3.2.0
- Java 17
- Maven
- Spring Web

## Future Enhancements

- Product management
- Shopping cart functionality
- Order processing
- User authentication
- Payment integration
