# Stage 1: Build the native executable with Java 21
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
WORKDIR /code

# Copy Maven wrapper and project files
COPY --chown=quarkus:quarkus mvnw mvnw
COPY --chown=quarkus:quarkus .mvn ./.mvn
COPY --chown=quarkus:quarkus pom.xml pom.xml

# Switch to quarkus user
USER quarkus

# Download dependencies (for better layer caching)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY --chown=quarkus:quarkus src ./src

# Build the native executable
RUN ./mvnw package -Pnative -DskipTests

# Stage 2: Create the runtime image
FROM registry.access.redhat.com/ubi8/ubi-minimal

WORKDIR /work/
COPY --from=build /code/target/*-runner /work/application

RUN chmod 775 /work /work/application

EXPOSE 8080

USER 1001

CMD ["./application"]