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
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy .jar file from first image
COPY --from=build /app/build/libs/*.jar app.jar

# Copiar artefactos de New Relic
RUN mkdir -p /usr/local/newrelic
COPY newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
COPY newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

#Exponer puerto
EXPOSE 8080

#Usar el agente en el arranque
ENTRYPOINT ["java", "-javaagent:/usr/local/newrelic/newrelic.jar", "-jar", "app.jar"]

# Stage 2 does not use Gradle, so it is not necessary to run gradle build