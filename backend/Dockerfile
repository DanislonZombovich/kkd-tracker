# Multi-stage build for Backend Java service
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
ENV SERVER_PORT=8080
ENV DB_URL=jdbc:postgresql://postgres:5432/kkd_db
ENV DB_USER=kkd_user
ENV DB_PASSWORD=kkd_secret
ENTRYPOINT ["java", "-jar", "app.jar"]
