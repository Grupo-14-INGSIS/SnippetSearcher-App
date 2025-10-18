# Multi-stage build

# Stage 1: build
FROM gradle:8.8-jdk21 AS build
WORKDIR /app
COPY . .
# Compile source code and generate .jar, except for task "test"
# RUN gradle build -x test
# As for now, add everything until tasks are implemented
RUN gradle bootJar -x test

# This generates a first image, containing the compiled .jar file

# Stage 2: runtime
FROM openjdk:21-jdk-slim
WORKDIR /app
# Copy .jar file from first image
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Stage 2 does not use Gradle, so it is not necessary to run gradle build