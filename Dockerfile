FROM eclipse-temurin:17-jdk
LABEL authors="boski"
ARG JAR_FILE=todo-list/target/*.jar
COPY ${JAR_FILE} api.jar
ENTRYPOINT ["java", "-jar", "api.jar"]