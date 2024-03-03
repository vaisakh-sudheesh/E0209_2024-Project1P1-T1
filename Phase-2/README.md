# E0209_2024-Project1P1-Team1
Project-1/Phase-2 directory for E0209-Principles of Distributed Software Vaisakh/Himanshu

## Directory Structure:
- **Booking**: Source code package for Booking service
- **User**: Source code package for User service
- **Wallets**: Source code package for Wallets service
- **Booking-Database**: Source code package for service hosting an H2 In-memory database
- **tests**:
  -  **docker-launch.sh**: shell script to build and run docker based test cases
  -  **launch.sh**: shell script to run minikube base testing
 
## Important points
- Integrated a *service* module in each of the packages to interface with Repositories.
- Applied @Transactional @EnableTransactionManagement annotations for service to repository interactions.
- Addressed feedback from Phase-1 on application.properties having 8081, 8082 ports hardcoded. Instead:
  - Makde use of profiles feature in Springboot where in two properties files are maintained *application.properties*  and *application-docker.properties* which will be picke by build system . Dockerfile will select the appropriate files during build
  - Further more, for URL mapping between developer, docker and minikube environments, two docker files (*Dockerfile.minikube* and *Dockerfile*) are maintained with environment variables of format *DOCKER_URL_* which will be picked in source code for issuing rest API calls between services.

