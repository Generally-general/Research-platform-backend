# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-22 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app

# Install Tesseract ONLY (Essential for OCR)
RUN apt-get update && \
    apt-get install -y tesseract-ocr \
    tesseract-ocr-eng


COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Basic memory limit for 512MB
ENTRYPOINT ["java", \
    "-Xmx256m", \
    "-Xms128m", \
    "-XX:+UseSerialGC", \
    "-jar", "app.jar"]