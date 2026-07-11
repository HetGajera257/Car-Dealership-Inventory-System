# ---- Stage 1: Build the JAR ----
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Run the JAR ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
