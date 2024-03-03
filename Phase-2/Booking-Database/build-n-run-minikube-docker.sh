docker build -t vaisakhp/booking-database-service:v1  -f Dockerfile.minikube  .
minikube kubectl -- create deployment bookingdbservice --image=vaisakhp/booking-database-service:v1
minikube kubectl -- expose deployment bookingdbservice --type=LoadBalancer --port=8084
