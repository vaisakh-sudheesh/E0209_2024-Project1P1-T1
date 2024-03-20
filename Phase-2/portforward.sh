minikube kubectl port-forward service/userservice 8080:8080  & PID_PF1="$!"
minikube kubectl port-forward service/bookingservice 8081:8080  & PID_PF2="$!"
minikube kubectl port-forward service/walletservice 8082:8080 & PID_PF3="$!"

sleep 3
