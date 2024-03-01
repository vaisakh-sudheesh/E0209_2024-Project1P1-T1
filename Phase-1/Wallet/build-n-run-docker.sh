docker build -t wallet-service .
docker run -p 8082:8082 --rm --name wallet --add-host=host.docker.internal:172.17.0.1 wallet-service

