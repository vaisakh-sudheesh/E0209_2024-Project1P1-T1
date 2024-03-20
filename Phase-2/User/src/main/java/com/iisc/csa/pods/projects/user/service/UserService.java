/**
 * Service layer for interfacing between controller and repository.
 * Includes the code serialization and ensuring synchronous database/repository update.
 */
package com.iisc.csa.pods.projects.user.service;

import com.iisc.csa.pods.projects.user.exception.UserOperationException;
import com.iisc.csa.pods.projects.user.model.UserTable;
import com.iisc.csa.pods.projects.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
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
    @Value("${DOCKER_URL_WALLET:localhost:8082}")
    private String uriStrngWallet;

    @Value("${DOCKER_URL_BOOKING:localhost:8080}")
    private String uriStrngBooking;

    /**
     * Helper methods and fields for wallet microservice URI
     */
    String getWalletUriBase (){
        return "http://"+uriStrngWallet+"/wallets/";
    }

    String getUserWalletDeleteUri(){
        return getWalletUriBase() + "{user_id}";
    }

    String getBookingUriBase (){
        return "http://"+uriStrngBooking+"/wallets/";
    }

    String getUserBookingDeleteUri(){
        return getBookingUriBase() + "{user_id}";
    }


    ////////////////////////////////////// Service Methods //////////////////////////////////////

    /**
     * Method to create a user account
     * @param postReq user information
     * @return User information of created userid
     */
    public synchronized UserTable createUser(UserTable postReq) {
        UserTable user = userRepo.save(postReq);
        return user;
    }

    /**
     * Get information for a user-id
     * @param user_id
     * @return User information
     */
    public UserTable getUser_id (Integer user_id){
        if( !userRepo.existsById(user_id)) {
            throw new UserOperationException("user_id not found");
        }
        return  userRepo.findbyId(user_id);
    }

    /**
     * Delete a user account for a given id
     * @param user_id
     */
    public synchronized void deleteUser_id(Integer user_id) {
        if( !userRepo.existsById(user_id)) {
            throw new UserOperationException("user_id not found");
        }
        DeleteUserBookings(user_id);
        DeleteUserWallets(user_id);
        userRepo.deletebyId(user_id);
    }

    /**
     * Delete all user accounts.
     */
    public synchronized void deleteAll() {
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
            System.out.println("DeleteUserBookings: Issuing delete: "+ getUserBookingDeleteUri());
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
            System.out.println("DeleteUserWallets: Issuing delete: "+ getUserWalletDeleteUri());
            restTemplate.delete (getUserWalletDeleteUri(), params);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                System.out.println("DeleteUserWallets failed"+e.getStatusCode());
            }
        }
    }

}
