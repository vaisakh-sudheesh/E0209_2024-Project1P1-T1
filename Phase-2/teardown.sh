#!/bin/bash

eval $(minikube docker-env)
pkill -f port-forward
sleep 10

# Remove kube instance - deployment and services
minikube kubectl -- delete -n default service walletservice userservice bookingservice bookingdbservice 
sleep 10
minikube kubectl -- delete -n default deployment walletservice userservice bookingservice bookingdbservice 
sleep 10

# Remove the docker images
docker image rm -f vaisakhp/booking-service:v1 vaisakhp/booking-database-service:v1 vaisakhp/wallet-service:v1  vaisakhp/user-service:v1
