/**
 * Controller implementation for Wallet Service.
 *
 * @author Vaisakh P S <vaisakhp@iisc.ac.in>
 */
package com.iisc.csa.pods.projects.user.controller;

import com.iisc.csa.pods.projects.user.model.UserTable;
import com.iisc.csa.pods.projects.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepo;


    /**
     * Endpoint requirement:
     * <p>
     *      POST /users
     *          Request JSON payload: {“name”: String, “email”: String}
     *          This endpoint creates a new user with the given name and email and an
     *          auto-generated id.<br/>
     *
     *          If a user with the given email address already exists then return HTTP status code
     *          400 (Bad Request), else return HTTP status code 201 (Created) with the JSON
     *          response {“id”: Integer, “name”: String, “email”: String}<br/>
     * </p>
     * @param postReq
     * @return
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
     * Endpoint requirement:
     * <p>
     *     GET /users/{user_id}
     *       This endpoint gets the details of the user with ID user_id.
     *       Response JSON payload: {“id”: Integer, “name”: String, “email”: String} with HTTP
     *       status code 200 (OK).<br/>
     *
     *        If the user doesn’t exist, return HTTP 404 (Not Found).<br/>
     * </p>
     * @param user_id
     * @return
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
     * Endpoint requirement:
     * <p>
     *     DELETE /users/{user_id}
     *       This endpoint deletes the user record with the given user_id and also invokes the
     *       DELETE /bookings/users/{user_id} endpoint on the Booking service and DELETE
     *       /wallets/{user_id} endpoint on the Wallet service.<br/>
     *
     *       Upon successful deletion, return HTTP 200 (OK) status code. If the user doesn’t
     *       exist, return HTTP 404 (Not Found).<br/>
     * </p>
     * @param user_id
     * @return
     */
    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteUser_id(@PathVariable Integer user_id) {
        try {
            if( userRepo.existsById(user_id)) {
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
     * Endpoint requirement:
     * <p>
     *     DELETE /users
     *       This endpoint deletes all user records (and hence all their bookings also). Always
     *       returns HTTP code 200 (OK). Basically, this end-point returns the states of all the
     *       services to their initial states.<br/>
     * </p>
     * @return
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteAll() {
        userRepo.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
