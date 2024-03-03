docker build -t vaisakhp/wallet-service:v1  -f Dockerfile.minikube .
minikube kubectl -- create deployment walletservice --image=vaisakhp/wallet-service:v1
minikube kubectl -- expose deployment walletservice --type=LoadBalancer --port=8080
