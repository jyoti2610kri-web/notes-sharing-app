# Build Stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from build /app/target/*.jar app.jar

# Expose the port (Render uses PORT env var)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
