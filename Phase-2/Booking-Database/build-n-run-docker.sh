docker build -t vaisakhp/booking-database-service .
docker run -p 8084:8084 --rm --name vaisakhp-booking-database --add-host=host.docker.internal:host-gateway vaisakhp/booking-database-service

