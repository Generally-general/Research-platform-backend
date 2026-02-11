# Stage 1: Build the JAR
FROM maven:3.9.8-eclipse-temurin-22 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app

# Install Tesseract
RUN apt-get update && apt-get install -y tesseract-ocr tesseract-ocr-eng && rm -rf /var/lib/apt/lists/*

# PRE-DOWNLOAD THE MODEL (The Fix)
# This directory matches what Spring AI expects
RUN mkdir -p /tmp/spring-ai-onnx-generative
ADD https://github.com/spring-projects/spring-ai/raw/main/models/spring-ai-transformers/src/main/resources/onnx/all-MiniLM-L6-v2/model.onnx /tmp/spring-ai-onnx-generative/model.onnx
ADD https://raw.githubusercontent.com/spring-projects/spring-ai/main/models/spring-ai-transformers/src/main/resources/onnx/all-MiniLM-L6-v2/tokenizer.json /tmp/spring-ai-onnx-generative/tokenizer.json

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# TUNED JVM FLAGS FOR 512MB (The Fix)
ENTRYPOINT ["java", \
    "-Xmx300m", \
    "-Xss512k", \
    "-XX:+UseSerialGC", \
    "-Dspring.ai.embedding.transformer.cache.directory=/tmp/spring-ai-onnx-generative", \
    "-jar", "app.jar"]