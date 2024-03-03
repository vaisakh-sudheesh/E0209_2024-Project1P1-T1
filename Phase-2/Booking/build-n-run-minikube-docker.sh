docker build -t vaisakhp/booking-service:v1 -f Dockerfile.minikube .
minikube kubectl -- create deployment bookingservice --image=vaisakhp/booking-service:v1
minikube kubectl -- expose deployment bookingservice --type=LoadBalancer --port=8080

