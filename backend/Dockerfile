# Stage 1: Build React frontend
FROM node:18 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot backend + embed frontend
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY backend/pom.xml ./
COPY backend/src ./src
# Copy React build to Spring Boot static folder
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Run
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
