FROM openjdk:17-jdk
ENV PODSPROJECT_BOOKING_SERV_PORT=8080

# Setup working directory
WORKDIR /app
COPY .mvn /app/.mvn
COPY mvnw /app/mvnw
COPY src  /app/src
COPY pom.xml /app/pom.xml

# Environment variable used in code to detect Docker/Non-docker execution
ENV DOCKER_RUNNING=Yes
ENTRYPOINT ["./mvnw", "spring-boot:run"]
