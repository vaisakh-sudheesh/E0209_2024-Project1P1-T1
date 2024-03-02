docker build -t user-service .
docker run -p 8080:8080 --rm --name user --add-host=host.docker.internal:host-gateway user-service


