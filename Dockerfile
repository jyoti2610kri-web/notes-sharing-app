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

# Expose the port
EXPOSE 8080

# Run the application (Exec form is best for Render)
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
