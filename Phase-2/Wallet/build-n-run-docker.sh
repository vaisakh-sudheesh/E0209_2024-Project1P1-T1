docker build -t wallet-service .
docker run -p 8082:8080 --rm --name wallet --add-host=host.docker.internal:host-gateway wallet-service


