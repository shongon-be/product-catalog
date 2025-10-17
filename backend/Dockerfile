# Stage 1: Build
FROM maven:3.9.9-amazoncorretto-17 AS build

WORKDIR /app

# Copy Maven wrapper và dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw package -DskipTests

# Stage 2: Runtime
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy JAR file từ build stage
COPY --from=build /app/target/backend-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]