#!/bin/bash

minikube start
eval $(minikube docker-env)


# Build User docker image
pushd User
docker build -t vaisakhp/user-service:v1  -f Dockerfile.minikube .
popd

# Build Wallet docker image
pushd Wallet
docker build -t vaisakhp/wallet-service:v1  -f Dockerfile.minikube .
popd

# Build Booking-Database docker image
pushd Booking-Database
docker build -t vaisakhp/booking-database-service:v1  -f Dockerfile.minikube  .
popd

# Build Booking docker image
pushd Booking
docker build -t vaisakhp/booking-service:v1 -f Dockerfile.minikube .
popd


# Start Kube deployments and services
minikube kubectl -- create deployment userservice --image=vaisakhp/user-service:v1
minikube kubectl -- expose deployment userservice --type=LoadBalancer --port=8080

minikube kubectl -- create deployment bookingdbservice --image=vaisakhp/booking-database-service:v1
minikube kubectl -- expose deployment bookingdbservice --type=LoadBalancer --port=8084

minikube kubectl -- create deployment bookingservice --image=vaisakhp/booking-service:v1
minikube kubectl -- expose deployment bookingservice --type=LoadBalancer --port=8080

minikube kubectl -- create deployment walletservice --image=vaisakhp/wallet-service:v1
minikube kubectl -- expose deployment walletservice --type=LoadBalancer --port=8080


# Wait for sometime for deployments to be running 
sleep 20

#setup port forwarding
minikube kubectl port-forward service/userservice 8080:8080  & PID_PF1="$!"
minikube kubectl port-forward service/bookingservice 8081:8080  & PID_PF2="$!"
minikube kubectl port-forward service/walletservice 8082:8080 & PID_PF3="$!"

sleep 3
echo "Running test case.."
# Run the test cases
python tests/test_case_1.py
