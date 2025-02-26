#!/bin/bash

minikube start
eval $(minikube docker-env)


printf "\n\n\t\t ==== Building docker images === "
printf "\n ## Building vaisakhp/user-service:v1  ## "

# Build User docker image
pushd User
docker build -t vaisakhp/user-service:v1  -f Dockerfile.minikube .
popd

printf "\n ## Building vaisakhp/wallet-service:v1  ## "
# Build Wallet docker image
pushd Wallet
docker build -t vaisakhp/wallet-service:v1  -f Dockerfile.minikube .
popd

printf "\n ## Building vaisakhp/booking-database-service:v1  ## "
# Build Booking-Database docker image
pushd Booking-Database
docker build -t vaisakhp/booking-database-service:v1  -f Dockerfile.minikube  .
popd

printf "\n ## Building vaisakhp/booking-service:v1  ## "
# Build Booking docker image
pushd Booking
docker build -t vaisakhp/booking-service:v1 -f Dockerfile.minikube .
popd


printf "\n\n\t\t ==== Starting containers === "
printf "\n ## Starting userservice ## "
# Start Kube deployments and services
minikube kubectl -- create deployment userservice --image=vaisakhp/user-service:v1
minikube kubectl -- expose deployment userservice --type=LoadBalancer --port=8080

printf "\n ## Starting bookingdbservice ## "
minikube kubectl -- create deployment bookingdbservice --image=vaisakhp/booking-database-service:v1
minikube kubectl -- expose deployment bookingdbservice --type=ClusterIP --port=8084

printf "\n ## Starting bookingservice ## "
minikube kubectl -- create deployment bookingservice --image=vaisakhp/booking-service:v1 --replicas=3
minikube kubectl -- expose deployment bookingservice --type=LoadBalancer --port=8080

printf "\n ## Starting walletservice ## "
minikube kubectl -- create deployment walletservice --image=vaisakhp/wallet-service:v1
minikube kubectl -- expose deployment walletservice --type=LoadBalancer --port=8080
##minikube tunnel &

printf "\n\n\t\t ==== Waiting for sometime === "
# Wait for sometime for deployments to be running
sleep 60
printf "\n\n\t\t ==== Setting port fowarding configurations === "
#setup port forwarding
minikube kubectl port-forward service/userservice 8080:8080  & PID_PF1="$!"
minikube kubectl port-forward service/bookingservice 8081:8080  & PID_PF2="$!"
minikube kubectl port-forward service/walletservice 8082:8080 & PID_PF3="$!"

sleep 3


#bash ./teardown.sh