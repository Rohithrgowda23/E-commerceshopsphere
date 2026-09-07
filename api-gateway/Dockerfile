FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY api-gateway/pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY api-gateway/src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
