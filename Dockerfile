# Stage 1: Build the JAR
FROM maven:3.9.8-eclipse-temurin-22 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime Environment
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app

# Install Tesseract OCR and English data
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/*

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Important: Expose the port Koyeb uses
EXPOSE 8080

# Run the app with optimized memory for the Nano instance
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]