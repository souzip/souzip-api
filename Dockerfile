FROM eclipse-temurin:21-jdk

WORKDIR /app

ARG JAR_FILE=build/libs/souzip-api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /souzip-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/souzip-api.jar"]