FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/jobconnect-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
