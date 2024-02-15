FROM openjdk:17-jdk
EXPOSE 8082

WORKDIR /app

COPY .mvn /app/.mvn
COPY mvnw /app/mvnw
COPY src  /app/src
COPY pom.xml /app/pom.xml

ENV DOCKER_RUNNING=Yes
ENTRYPOINT ["./mvnw", "spring-boot:run"]
