# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only pom.xml to download dependencies first (leveraging Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final production image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port (Render will override this with its own PORT env var, but 8080 is common for Java)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
