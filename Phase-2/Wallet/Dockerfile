FROM openjdk:17-jdk
EXPOSE 8080

# Setup working directory
WORKDIR /app
COPY .mvn /app/.mvn
COPY mvnw /app/mvnw
COPY src  /app/src
COPY pom.xml /app/pom.xml

# Environment variable used in code to detect Docker/Non-docker execution
ENV DOCKER_RUNNING=Yes
ENV DOCKER_URL_BOOKINGDB="host.docker.internal"
ENV DOCKER_URL_USER="host.docker.internal"
ENV DOCKER_URL_WALLET="host.docker.internal"
ENTRYPOINT ["./mvnw", "-Dspring-boot.run.arguments=\"--spring.profiles.active=docker\"", "spring-boot:run"]
