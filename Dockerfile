# Stage 1 (Builder)
FROM maven:3.9.16-eclipse-temurin-25 AS build

WORKDIR /app

# Copy the pom.xml and src directory
COPY pom.xml .
COPY src ./src

# Run mvn clean package -DskipTests to build the fat JAR
RUN mvn clean package -DskipTests

# Stage 2 (Runner)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built .jar file from the Builder stage into this image as app.jar
COPY --from=build /app/target/*.jar /app.jar

# Set the ENTRYPOINT to ["java", "-jar", "app.jar"]
ENTRYPOINT ["java", "-jar", "/app.jar"]
