FROM openjdk:17-jdk
EXPOSE 8080

WORKDIR /app

COPY .mvn /app/.mvn
COPY mvnw /app/mvnw
COPY src  /app/src
COPY pom.xml /app/pom.xml

CMD ["./mvnw", "spring-boot:run"]