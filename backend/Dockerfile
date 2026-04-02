FROM sbtscala/scala-sbt:eclipse-temurin-alpine-25.0.1_8_1.12.8_3.8.2 AS builder
WORKDIR /app

COPY build.sbt ./
COPY project/ project/

COPY src/ src/

RUN sbt clean assembly

FROM eclipse-temurin:25.0.2_10-jre-alpine as runtime

WORKDIR /app

COPY --from=builder /app/target/scala-*/backend-app.jar /app/backend.jar

ENTRYPOINT ["java", "-jar", "/app/backend.jar"]