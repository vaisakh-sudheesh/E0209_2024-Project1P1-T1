# E0209_2024-Project1P1-Team1
Project-1/Phase-1 repository for E0209-Principles of Distributed Software Vaisakh/Himanshu

## Directory Structure:
- **Booking**: Source code package for Booking service
- **User**: Source code package for User service
- **Wallets**: Source code package for Wallets service
- **tests**: Python test cases
  -  **test_case_<x>.py**: Test cases published from academic team
  -  **custtc_<x>-<modulename>.py**: Additional test cases added to check functions are entry point level
  -  **common**: utility/helper code for **custtc_** test cases
 
## Important points
- A shell script **build-n-run-docker.sh** is added to build and launch docker for each service.
- An environment variable **DOCKER_RUNNING** is added in Dockerfile and read in code to differentiate between docker and non-docker execution.
