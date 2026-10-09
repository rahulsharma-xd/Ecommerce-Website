# E-commerce Application

A full-stack e-commerce application built with Spring Boot (backend) and React (frontend).

## 🚀 Features

- **User Management**: Registration, authentication, and profile management
- **Product Catalog**: Browse products with categories and search functionality
- **Shopping Cart**: Add, update, and remove items from cart
- **Order Management**: Checkout process and order tracking
- **Admin Panel**: Product and order management for administrators
- **Security**: JWT-based authentication and authorization

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.1
- **Database**: H2 (development), easily configurable for PostgreSQL/MySQL
- **Security**: Spring Security with JWT
- **ORM**: Spring Data JPA
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test

### Frontend (Planned)
- React + Vite
- React Router
- Axios for API calls

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 16+ (for frontend)

## ⚙️ Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>

### 2. Configure Application Properties

The application requires configuration files that are not tracked in git for security reasons.

#### Backend Configuration

Create `backend/src/main/resources/application.properties` from the template:

```bash
cp backend/src/main/resources/application.properties.template backend/src/main/resources/application.properties
```

Then edit the file and update:
- `jwt.secret`: Generate a secure random secret (use `openssl rand -base64 64`)
- Email credentials (optional): Set `EMAIL_USERNAME` and `EMAIL_PASSWORD` environment variables

#### Test Configuration

Create `backend/src/test/resources/application-test.properties` from the template:

```bash
cp backend/src/test/resources/application-test.properties.template backend/src/test/resources/application-test.properties
```

### 3. Build and Run

#### Using Maven

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

#### Using Docker Compose (when available)

```bash
docker-compose up
```

The backend will be available at `http://localhost:8082`

### 4. Access H2 Console (Development)

- URL: `http://localhost:8082/h2-console`
- JDBC URL: `jdbc:h2:file:../data/ecommerce_db`
- Username: `sa`
- Password: (leave empty)

## 🧪 Running Tests

```bash
cd backend
mvn test
```

## 📁 Project Structure

```
Ecommerce-Website/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ecommerce/
│   │   │   │   ├── controller/     # REST API endpoints
│   │   │   │   ├── dto/            # Data Transfer Objects
│   │   │   │   ├── exception/      # Custom exceptions and handlers
│   │   │   │   ├── model/          # JPA entities
│   │   │   │   ├── repository/     # Data access layer
│   │   │   │   ├── security/       # Security configuration
│   │   │   │   └── service/        # Business logic
│   │   │   └── resources/
│   │   │       └── application.properties.template
│   │   └── test/
│   │       └── java/com/ecommerce/
│   │           └── [tests]/        # Unit and integration tests
│   └── pom.xml
├── data/                           # H2 database files (gitignored)
├── docker-compose.yml
└── README.md
```

## 🔐 Security Notes

- **JWT Secret**: The `application.properties` file is gitignored and must be created from the template
- **Default Credentials**: Change all default passwords in production
- **H2 Console**: Disable in production (`spring.h2.console.enabled=false`)
- **Email Credentials**: Use environment variables or secure secret management

## 📝 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user

### Products
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{category}` - Get products by category

### Cart
- `GET /api/cart` - Get user's cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{id}` - Update cart item quantity
- `DELETE /api/cart/items/{id}` - Remove item from cart

### Orders
- `POST /api/orders/checkout` - Create order from cart
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order by ID

### Admin
- `GET /api/admin/orders` - Get all orders (Admin only)
- `PUT /api/admin/orders/{id}/status` - Update order status (Admin only)

## 🧑‍💻 Development

### Test Coverage

The project includes comprehensive test coverage:
- Unit tests for services
- Integration tests for controllers
- Security configuration tests
- Exception handling tests

Current test count: **100+ tests**

## 📄 License

This project is for educational purposes.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## ⚠️ Important Notes

- The `application.properties` file contains sensitive information and is NOT tracked in git
- Always create this file from the `.template` file after cloning
- Never commit files containing secrets, API keys, or passwords
- Use environment variables for production deployments
