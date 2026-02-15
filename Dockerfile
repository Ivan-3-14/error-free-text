FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
RUN mkdir -p /app/logs
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]