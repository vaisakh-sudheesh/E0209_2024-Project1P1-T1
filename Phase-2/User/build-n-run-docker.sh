docker build -t vaisakhp/user-service .
docker run -p 8080:8080 --rm --name vaisakhp/user --add-host=host.docker.internal:host-gateway vaisakhp/user-service


