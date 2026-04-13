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

# Expose the port (Render uses PORT env var)
EXPOSE 8080

# Define environment variables placeholders for Spring Boot to pick up
ENV PORT=8080
ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_DATASOURCE_PASSWORD=""
ENV CLOUDINARY_URL=""

# Run the application
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
