docker build -t vaisakhp/wallet-service .
docker run -p 8082:8080 --rm --name vaisakhp/wallet --add-host=host.docker.internal:host-gateway vaisakhp/wallet-service


