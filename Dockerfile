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

# This command FORCES the app to read the Render variables
ENTRYPOINT ["java", "-Xmx512m", "-Dserver.port=${PORT}", "-Dspring.datasource.url=${SPRING_DATASOURCE_URL}", "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME}", "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD}", "-Dcloudinary.url=${CLOUDINARY_URL}", "-jar", "app.jar"]
