# Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy as builder

# Set working directory
WORKDIR /app

# Copy pom.xml separately to leverage Docker cache
COPY pom.xml .
COPY src ./src
# Download dependencies
RUN mvn dependency:go-offline

# Build the application
RUN mvn clean package -DskipTests

# Final stage - use JRE instead of JDK
FROM eclipse-temurin:21-jre-jammy

# Set working directory
WORKDIR /app

# Copy the built application
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]