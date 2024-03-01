/**
 * Controller implementation for Wallet Service.
 *
 * @author Vaisakh P S <vaisakhp@iisc.ac.in>
 */
package com.iisc.csa.pods.projects.user.controller;

import com.iisc.csa.pods.projects.user.model.UserTable;
import com.iisc.csa.pods.projects.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
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

    ////////////////////////////////////// Controller Endpoints //////////////////////////////////////
    /**
     * <b><u>Endpoint requirement:</u>  1. POST /users</b>
     * <p>
     *          Request JSON payload: {“name”: String, “email”: String}
     *          This endpoint creates a new user with the given name and email and an
     *          auto-generated id.<br/><br/>
     *
     *          If a user with the given email address already exists then return HTTP status code
     *          400 (Bad Request), else return HTTP status code 201 (Created) with the JSON
     *          response {“id”: Integer, “name”: String, “email”: String}<br/>
     * </p>
     *
     * @param postReq JSON payload with user info for account creation
     * @return HTTP/Created(201) with JSON payload of user info in case of success; HTTP/BadRequest(400) on error
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<UserTable> postUsers(@RequestBody UserTable postReq) {
        try {
            UserTable user = userRepo.save(postReq);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * <b><u>Endpoint requirement:</u>  2. GET /users/{user_id}</b>
     * <p>
     *       This endpoint gets the details of the user with ID user_id. <br/><br/>
     *
     *       Response JSON payload: {“id”: Integer, “name”: String, “email”: String} with HTTP
     *       status code 200 (OK).<br/><br/>
     *
     *        If the user doesn’t exist, return HTTP 404 (Not Found).<br/>
     * </p>
     *
     * @param user_id UserID to be queried
     * @return HTTP/OK(200) with JSON payload of user info; HTTP/NotFound(404) on account not existing.
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<UserTable> getUser_id (@PathVariable Integer user_id){
        try {
            if( userRepo.existsById(user_id)) {
                UserTable userdata = userRepo.findbyId(user_id);
                return ResponseEntity.ok(userdata);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * <b><u>Endpoint requirement:</u>  3. DELETE /users/{user_id}</b>
     * <p>
     *       This endpoint deletes the user record with the given user_id and also invokes the
     *       DELETE /bookings/users/{user_id} endpoint on the Booking service and DELETE
     *       /wallets/{user_id} endpoint on the Wallet service.<br/><br/>
     *
     *       Upon successful deletion, return HTTP 200 (OK) status code. If the user doesn’t
     *       exist, return HTTP 404 (Not Found).<br/>
     * </p>
     *
     * @param user_id UserID to be deleted
     * @return HTTP/OK(200) on successful deletion; HTTP/NotFound(404) on account not existing.
     */
    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteUser_id(@PathVariable Integer user_id) {
        try {
            if( userRepo.existsById(user_id)) {
                DeleteUserBookings(user_id);
                DeleteUserWallets(user_id);
                userRepo.deletebyId(user_id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * <b><u>Endpoint requirement:</u>  4. DELETE /users</b>
     * <p>
     *       This endpoint deletes all user records (and hence all their bookings also). Always
     *       returns HTTP code 200 (OK). Basically, this end-point returns the states of all the
     *       services to their initial states.<br/>
     * </p>
     *
     * @return HTTP/OK(200) always
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteAll() {
        List<UserTable> users = this.userRepo.findAll();
        for (UserTable user : users) {
            DeleteUserBookings(user.getId());
            DeleteUserWallets(user.getId());
        }
        userRepo.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
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
