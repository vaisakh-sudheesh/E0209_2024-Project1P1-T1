FROM openjdk:17-jdk

# Setup working directory
WORKDIR /app
COPY .mvn /app/.mvn
COPY mvnw /app/mvnw
COPY src  /app/src
COPY pom.xml /app/pom.xml

# Environment variable used in code to detect Docker/Non-docker execution
ENV DOCKER_RUNNING=Yes
ENTRYPOINT ["./mvnw", "-Dspring-boot.run.arguments=\"--spring.profiles.active=docker\"", "spring-boot:run"]
