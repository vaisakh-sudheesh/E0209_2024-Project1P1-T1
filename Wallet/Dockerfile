FROM openjdk:17-jdk
EXPOSE 8082

WORKDIR /app

COPY .mvn /app/.mvn
COPY mvnw /app/mvnw
COPY src  /app/src
COPY pom.xml /app/pom.xml

ENTRYPOINT ["./mvnw", "spring-boot:run"]
