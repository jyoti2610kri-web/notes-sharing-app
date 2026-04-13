# Build Stage
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render uses the PORT environment variable
EXPOSE 8080

# This command is more robust for Docker/Render environments
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
