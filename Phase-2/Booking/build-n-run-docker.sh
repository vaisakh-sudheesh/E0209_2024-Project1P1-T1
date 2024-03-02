docker build -t booking-service .
docker run -p 8081:8080  --rm --name booking --add-host=host.docker.internal:host-gateway booking-service

