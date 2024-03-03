docker build -t vaisakhp/user-service:v1  -f Dockerfile.minikube .
minikube kubectl -- create deployment userservice --image=vaisakhp/user-service:v1
minikube kubectl -- expose deployment userservice --type=LoadBalancer --port=8080

