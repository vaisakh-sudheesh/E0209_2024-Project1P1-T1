/**
 * Controller implementation for Wallet Service.
 *
 * @author Vaisakh P S <vaisakhp@iisc.ac.in>
 */
package com.iisc.csa.pods.projects.user.controller;

import com.iisc.csa.pods.projects.user.exception.UserOperationException;
import com.iisc.csa.pods.projects.user.model.UserTable;
import com.iisc.csa.pods.projects.user.repository.UserRepository;
import com.iisc.csa.pods.projects.user.service.UserService;
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
    UserService userService;

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
            UserTable user = userService.createUser(postReq);
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
            UserTable userdata = userService.getUser_id((user_id));
            return ResponseEntity.ok(userdata);
        } catch (UserOperationException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
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
            userService.deleteUser_id(user_id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserOperationException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
        userService.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
