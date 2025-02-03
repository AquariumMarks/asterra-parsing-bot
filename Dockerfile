# Stage 1: Build
FROM maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17

WORKDIR /app

COPY --from=builder /app/target/asterra-parsing-bot-0.0.1-SNAPSHOT.jar .

CMD ["java", "-jar", "asterra-parsing-bot-0.0.1-SNAPSHOT.jar"]