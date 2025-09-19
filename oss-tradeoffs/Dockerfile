FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :oss-tradeoffs:build

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /app/oss-tradeoffs/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
