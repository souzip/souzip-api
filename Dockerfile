FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY build/libs/*.jar /souzip-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/souzip-api.jar"]
