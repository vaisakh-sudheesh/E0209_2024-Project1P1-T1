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
     * <p>
     *      POST /users
     *          Request JSON payload: {“name”: String, “email”: String}
     *          This endpoint creates a new user with the given name and email and an
     *          auto-generated id.<br/>
     *
     *          If a user with the given email address already exists then return HTTP status code
     *          400 (Bad Request), else return HTTP status code 201 (Created) with the JSON
     *          response {“id”: Integer, “name”: String, “email”: String}
     * </p>
     * @param postReq
     * @return
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<UserTable> postController(@RequestBody UserTable postReq) {
        try {
            System.out.println("Inserting data"+postReq.toString());
            UserTable user = userRepo.save(postReq);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<UserTable> getController (@PathVariable Integer user_id){
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

    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteController(@PathVariable Integer user_id) {
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

    @DeleteMapping()
    public ResponseEntity<?> deleteController() {
        userRepo.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
