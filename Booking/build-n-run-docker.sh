docker build -t booking-service .
docker run -p 8081:8081 --rm --name booking --add-host=host.docker.internal:172.17.0.1 booking-service

