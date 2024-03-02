package com.iisc.csa.pods.projects.user.service;

import com.iisc.csa.pods.projects.user.exception.UserOperationException;
import com.iisc.csa.pods.projects.user.model.UserTable;
import com.iisc.csa.pods.projects.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    ////////////////////////////////////// URI Management //////////////////////////////////////
    /**
     * Since each of the microservices that are part of this project have separate in-memory database entities,
     * interaction between these microservices need to be done over HTTP/Rest request.
     * URIs for the doing the same.<br/>
     *
     * Two are maintained, as both docker and non-docker invocation of service will have different URIs.
     */
    @Value("${DOCKER_RUNNING:No}")
    private String dockerStatus;

    /**
     * Helper methods and fields for wallet microservice URI
     */
    final String wallet_uri_docker = "http://host.docker.internal:8082/";
    final String wallet_uri_localdev = "http://localhost:8082/";
    String getWalletUriBase (){
        return (dockerStatus.equals("Yes") ? wallet_uri_docker : wallet_uri_localdev)+"wallets/";
    }

    String getUserWalletDeleteUri(){
        return getWalletUriBase() + "{user_id}";
    }

    /**
     * Helper methods and fields for booking microservice URI
     */
    final String booking_uri_docker = "http://host.docker.internal:8081/";
    final String booking_uri_localdev = "http://localhost:8081/";
    String getBookingUriBase (){
        return dockerStatus.equals("Yes") ? booking_uri_docker : booking_uri_localdev;
    }

    String getUserBookingDeleteUri() {
        return getBookingUriBase()+ "bookings/users/{user_id}";
    }

    ////////////////////////////////////// Service Methods //////////////////////////////////////

    @Transactional
    public UserTable createUser(UserTable postReq) {
        UserTable user = userRepo.save(postReq);
        return user;
    }

    @Transactional
    public UserTable getUser_id (Integer user_id){
        if( !userRepo.existsById(user_id)) {
            throw new UserOperationException("user_id not found");
        }
        return  userRepo.findbyId(user_id);
    }

    @Transactional
    public void deleteUser_id(Integer user_id) {
        if( !userRepo.existsById(user_id)) {
            throw new UserOperationException("user_id not found");
        }
        DeleteUserBookings(user_id);
        DeleteUserWallets(user_id);
        userRepo.deletebyId(user_id);
    }

    @Transactional
    public void deleteAll() {
        List<UserTable> users = this.userRepo.findAll();
        for (UserTable user : users) {
            DeleteUserBookings(user.getId());
            DeleteUserWallets(user.getId());
        }
        userRepo.deleteAll();
    }


    ////////////////////////////////////// Helper methods //////////////////////////////////////

    /**
     *  Helper method to perform deletion of bookings associated with user account.
     *  This will be invoked on user account deletion endpoints.<br/>
     *
     * @param user_id UserID of account to release bookings
     * @return true on success; false otherwise
     */
    void DeleteUserBookings(Integer user_id){
        try {
            Map<String, String> params = new HashMap<String, String>();
            RestTemplate restTemplate = new RestTemplate();
            params.put("user_id", user_id.toString());
            restTemplate.delete (getUserBookingDeleteUri(), params);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                System.out.println("DeleteUserBookings failed" +e.getStatusCode());
            }
        }
    }

    /**
     *  Helper method to perform deletion of wallet associated with user account.
     *  This will be invoked on user account deletion endpoints.<br/><br/>
     *
     * WARN: Due to the nature of operation sequence, bookings need to be freed up prior
     *       to releasing wallet accounts.<br/>
     *
     * @param user_id UserID of account to release wallet
     * @return true on success; false otherwise
     */
    void DeleteUserWallets(Integer user_id){
        try {
            Map<String, String> params = new HashMap<String, String>();
            RestTemplate restTemplate = new RestTemplate();
            params.put("user_id", user_id.toString());
            restTemplate.delete (getUserWalletDeleteUri(), params);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                System.out.println("DeleteUserWallets failed"+e.getStatusCode());
            }
        }
    }

}
