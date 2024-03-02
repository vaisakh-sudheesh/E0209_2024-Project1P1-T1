docker build -t booking-database-service .
docker run -p 8084:8084 --rm --name booking-database --add-host=host.docker.internal:host-gateway booking-database-service

