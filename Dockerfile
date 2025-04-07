FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .

RUN ./gradlew clean build -x test --no-daemon

CMD ["./gradlew", "bootRun", "--args=--spring.profiles.active=dev"]
