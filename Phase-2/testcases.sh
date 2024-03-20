
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
python tests/phase-1_tcs/test_case_1.py
sleep 5

printf "\n ## TC4 - Phase 1 : Serial  test_case_2.py ## "
python tests/phase-1_tcs/test_case_2.py
sleep 5
python tests/phase-1_tcs/test_case_2.py
sleep 5

printf "\n ## TC5 - Phase 1 : Serial  test_case_3.py ## "
python tests/phase-1_tcs/test_case_3.py
sleep 5
python tests/phase-1_tcs/test_case_3.py
sleep 5

printf "\n ## TC6 - Phase 1 : Serial  custtc_1-userservice.py ## "
python tests/phase-1_tcs/custtc_1-userservice.py
sleep 5
python tests/phase-1_tcs/custtc_1-userservice.py
sleep 5


printf "\n ## TC6 - Phase 1 : Serial  custtc_2-walletservice.py ## "
python tests/phase-1_tcs/custtc_2-walletservice.py
sleep 5
python tests/phase-1_tcs/custtc_2-walletservice.py
sleep 5