FROM openjdk:17-jdk-slim

LABEL authors="seungyong20"

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

EXPOSE 8080