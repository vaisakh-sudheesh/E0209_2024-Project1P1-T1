docker build -t vaisakhp/booking-service .
docker run -p 8081:8080  --rm --name vaisakhp-booking --add-host=host.docker.internal:host-gateway vaisakhp/booking-service

