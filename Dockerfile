FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENV PORT=8080

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -Dserver.port=$PORT -jar app.jar"]