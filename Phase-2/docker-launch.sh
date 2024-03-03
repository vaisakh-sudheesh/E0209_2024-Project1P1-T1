#!/bin/bash

printf "\n\n\t\t ==== Building docker images === "
printf "\n ## Building vaisakhp/booking-database-service ## "
pushd Booking-Database
docker build -t vaisakhp/booking-database-service .
popd

printf "\n ## Building vaisakhp/user-service ## "
pushd User
docker build -t vaisakhp/user-service .
popd

printf "\n ## Building vaisakhp/wallet-service ## "
pushd Wallet
docker build -t vaisakhp/wallet-service .
popd

printf "\n ## Building vaisakhp/booking-service ## "
pushd Booking
docker build -t vaisakhp/booking-service .
popd

printf "\n\n\t\t ==== Starting containers === "
printf "\n ## Starting vaisakhp-booking-database ## "
docker run -p 8084:8084 --detach --rm --name vaisakhp-booking-database --add-host=host.docker.internal:host-gateway vaisakhp/booking-database-service

printf "\n ## Starting vaisakhp-user ## "
docker run -p 8080:8080 --detach --rm --name vaisakhp-user --add-host=host.docker.internal:host-gateway vaisakhp/user-service

printf "\n ## Starting vaisakhp-wallet ## "
docker run -p 8082:8080 --detach --rm --name vaisakhp-wallet --add-host=host.docker.internal:host-gateway vaisakhp/wallet-service

printf "\n ## Starting vaisakhp-booking ## "
docker run -p 8081:8080  --detach --rm --name vaisakhp-booking --add-host=host.docker.internal:host-gateway vaisakhp/booking-service

printf "\n\n\t\t ==== Waiting for soemtime === "
sleep 30

printf "\n\n\t\t ==== Running test cases === "

printf "\n ## TC1 ## "
python tests/test_case_1.py
sleep 5
printf "\n ## TC2 ## "
python tests/test_case_1.py
sleep 5

printf "\n ## TC3 - Phase 1 : Serial  test_case_1.py ## "
python tests/phase-1_tcs/test_case_1.py
sleep 5

printf "\n ## TC4 - Phase 1 : Serial  test_case_2.py ## "
python tests/phase-1_tcs/test_case_2.py
sleep 5

printf "\n ## TC5 - Phase 1 : Serial  test_case_3.py ## "
python tests/phase-1_tcs/test_case_3.py
sleep 5

printf "\n ## TC6 - Phase 1 : Serial  custtc_1-userservice.py ## "
python tests/phase-1_tcs/custtc_1-userservice.py
sleep 5


printf "\n ## TC6 - Phase 1 : Serial  custtc_2-walletservice.py ## "
python tests/phase-1_tcs/custtc_2-walletservice.py
sleep 5


printf "\n\n\t\t ==== Cleaning docker files === "
### Tear down
docker stop vaisakhp-booking-database vaisakhp-user vaisakhp-wallet vaisakhp-booking 
docker image rm -f vaisakhp/booking-database-service vaisakhp/user-service vaisakhp/wallet-service vaisakhp/booking-service

