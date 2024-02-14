/**
 * Controller module for Booking microservice
 */
package com.iisc.csa.pods.projects.booking.controller;

import com.iisc.csa.pods.projects.booking.model.*;
import com.iisc.csa.pods.projects.booking.repository.BookingRepository;
import com.iisc.csa.pods.projects.booking.repository.ShowRepository;
import com.iisc.csa.pods.projects.booking.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@RestController
public class BookingController {
    // Repository instances for accessing theatre, shows and booking entities
    @Autowired
    private TheatreRepository theatreRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private BookingRepository bookingRepository;

    // Since each of the microservices in this project have separate in-memory database entities,
    // interaction between these microservices need to be done over HTTP/Rest request.
    // URIs for the doing the same.
    final String usercheck_uri = "http://localhost:8080/users/{user_id}";
    final String walletaction_uri = "http://localhost:8082/wallets/{user_id}";


    /**
     * Endpoint Requirement:
     * <p>
     *     1. GET /theatres
     *        This endpoint returns the list of all available theatres.
     *        Response JSON payload: [{“id”: Integer, “name”: String, “location”: String}] with
     *        HTTP status code 200 (OK). (Return empty list if no theatres are there.)
     * </p>
     * TODO: Add parameter and return documentation
     */
    @GetMapping("/theatres")
    ResponseEntity<?> getTheatres(){
        List<Theatre> theatres = this.theatreRepository.findAll();
        return ResponseEntity.ok(theatres);
    }

    /**
     * Endpoint Requirement:
     * <p>
     *     2. GET /shows/theatres/{theatre_id}
     *        This endpoint returns the list of all shows being showcased at the theatre with
     *        theatre_id.
     *        Response JSON payload: [{“id”: Integer, ”theatre_id”: Integer, “title”: String, “price”:
     *        Integer, “seats_available”: Integer}] with HTTP status code 200 (OK). (Return empty
     *        list if no shows are there for the given theatre.)
     *        If the theatre does not exist then return HTTP 404 (Not Found).
     * </p>
     * TODO: Add parameter and return documentation
     */
    @GetMapping("/shows/theatres/{theatre_id}")
    ResponseEntity<?> getShowsTheatres(@RequestParam Integer theater_id) {
        if (this.theatreRepository.existsById(theater_id)) {
            // Check for validity of provided theatre_id
            List<Show> theatres = this.showRepository.findByTheatre_id(theater_id);
            return ResponseEntity.ok(theatres);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint Requirement:
     * <p>
     *     3. GET /shows/{show_id}
     *        This endpoint returns the details of the show with ID show_id.
     *        Response JSON payload: {“id”: Integer, ”theatre_id”: Integer, “title”: String, “price”:
     *        Integer, “seats_available”: Integer} with HTTP status code 200 (OK).
     *        If the show doesn’t exist, return HTTP 404 (Not Found).
     * </p>
     * TODO: Add parameter and return documentation
     */
    @GetMapping("/shows/{show_id}")
    ResponseEntity<?> getShows(@RequestParam Integer show_id) {
        if (this.showRepository.existsById(show_id)) {
            // Check for validity of show_id prior to fetching details
            Show show = this.showRepository.findByShowId(show_id);
            return ResponseEntity.ok(show);
        } else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint Requirement:
     * <p>
     *     4. GET /bookings/users/{user_id}
     *        This endpoint returns the list of all bookings made by the user user_id.
     *        Response JSON payload: [{“id”: Integer, “show_id”: Integer, “user_id”: Integer,
     *        “seats_booked”: Integer}] with HTTP status code 200 (OK). To return an empty list if
     *        the user does not have any bookings.
     * </p>
     * TODO: Add parameter and return documentation
     */
    @GetMapping("/bookings/users/{user_id}")
    ResponseEntity<?> getBookingsUsers(@RequestParam Integer user_id) {
        List<Booking> bookings = this.bookingRepository.findByUser_id(user_id);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Endpoint Requirement:
     * <p>
     *     5. POST /bookings
     *        Request JSON payload of the form {“show_id”: Integer, ”user_id”: Integer,
     *        “seats_booked”: Integer}.<br/>
     *
     *        This endpoint is invoked by the user to create a booking for show with ID show_id.<br/>
     *
     *        If the show does not exist, return HTTP 400 (Bad Request).<br/>
     *
     *        If the user does not exist, return HTTP 400 (Bad Request).<br/>
     *
     *        If the number of seats available for the show is less than seats_booked, return HTTP
     *        400 (Bad Request).<br/>
     *
     *        This end point needs to make use of the Wallet service to deduct an amount equal to
     *        the price of the show times seats_booked. If the Wallet deduction does not happen
     *        because of insufficient balance in the user’s wallet, return HTTP 400 (Bad Request),
     *        else reduce the number of seats available in the show by seats_booked and return
     *        HTTP 200 (OK).<br/>
     * </p>
     * TODO: Add parameter and return documentation
     */
    @PostMapping("/bookings")
    ResponseEntity<?> postBookings (@RequestBody BookingPayload bookingreq) {
        // Check for validity of show_id
        if (!this.showRepository.existsById(bookingreq.getShow_id())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Check if the user_id is a valid one by RestAPI call to User service
        try {
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(usercheck_uri, String.class, bookingreq.getUser_id());
            System.out.println("User check passed"+result );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                System.out.println("User check failed");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        // Check for availability of seat
        Show showdetails = this.showRepository.findByShowId(bookingreq.getShow_id());
        if (showdetails.getSeats_available() < bookingreq.getSeats_booked()) {
            System.out.println("Not enough seats available"+showdetails.getSeats_available()+" for a request of "+bookingreq.getSeats_booked());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        //// TODO/BUG: Handle 0 booking request case
        // Wallet transaction
        try {
            System.out.println("Initiating wallet transaction...");
            Integer ticketcost = bookingreq.getSeats_booked() * showdetails.getPrice();
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("action", "debit");
            requestBody.put("amount", ticketcost);

            // Create an HttpEntity object with the request body and headers
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Define URI variables for the user ID
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("user_id", bookingreq.getUser_id().toString());

            // Make the PUT request
            ResponseEntity<String> response = restTemplate.exchange(walletaction_uri, HttpMethod.PUT, entity, String.class, uriVariables);

            // Update Booking table for the successful booking
            this.bookingRepository.save(new Booking(showdetails, bookingreq.getUser_id(), bookingreq.getSeats_booked()));

            // Update available seat counter
            showdetails.setSeats_available(showdetails.getSeats_available() - bookingreq.getSeats_booked());
            this.showRepository.save(showdetails);

        } catch(HttpClientErrorException e) {
            System.out.println("Wallet transaction failed with "+ e.getStatusCode());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);

    }


    /**
     * Endpoint Requirement:
     * <p>
     *     6. DELETE /bookings/users/{user_id}
     *        Deletes all booking records for the user with ID user_id. The seats corresponding to
     *        these bookings are to be returned to the available pool. The amounts that were taken
     *        to make these bookings are returned to the user’s wallet.<br/>
     *
     *        If the user had any bookings then return HTTP Code 200 (OK), else return HTTP
     *        code 404 (Not Found).<br/>
     * </p>
     * TODO: Add parameter and return documentation
     */
    @DeleteMapping("/bookings/users/{user_id}")
    ResponseEntity<?> deleteUsers(@RequestParam Integer user_id) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Endpoint Requirement:
     * <p>
     *      7. DELETE /bookings/users/{user_id}/shows/{show_id}
     *         Deletes all booking records for the user with ID user_id whose show ID is show_id.
     *         The seats corresponding to these bookings are to be returned to the available pool,
     *         and the booking amount is also returned to the wallet.<br/>
     *
     *         If the user had any bookings in the given show then return HTTP Code 200 (OK),
     *         else return HTTP code 404 (Not Found).<br/>
     * </p>
     * TODO: Add parameter and return documentation
     */
    @DeleteMapping("/bookings/users/{user_id}/shows/{show_id}")
    ResponseEntity<?> deleteUsersShows(@RequestParam Integer user_id, @RequestParam Integer show_id) {
        if (!this.bookingRepository.existsByUser_idAndShow_id(user_id, show_id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Now handle the case of return of seats and refund
        List<Booking> bookings = this.bookingRepository.findAllByUser_idAndShow_id(user_id, show_id);
        for (Booking booking : bookings) {
            Integer seats_booked = booking.getSeats_booked();

            Show showinfo = this.showRepository.findByShowId(show_id);
            Integer refund_amount = seats_booked * showinfo.getPrice();

            // Return the booking amount to the wallet.
            // Wallet transaction
            try {
                System.out.println("Initiating wallet refund transaction...");
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("action", "credit");
                requestBody.put("amount", refund_amount);

                // Create an HttpEntity object with the request body and headers
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                // Define URI variables for the user ID
                Map<String, String> uriVariables = new HashMap<>();
                uriVariables.put("user_id", user_id.toString());

                // Make the PUT request
                ResponseEntity<String> response = restTemplate.exchange(walletaction_uri, HttpMethod.PUT, entity, String.class, uriVariables);

            } catch (HttpClientErrorException e) {
                System.out.println("Wallet transaction failed with " + e.getStatusCode());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            // Return seats corresponding to these bookings to the available pool of show
            showinfo.setSeats_available(showinfo.getSeats_available() + seats_booked);
            this.showRepository.save(showinfo);
        }
        this.bookingRepository.deleteAllByUser_idAndShow_id(user_id, show_id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * Endpoint Requirement:
     * <p>
     *      8. DELETE /bookings
     *         This endpoint deletes all bookings of all users in all shows, and returns back the
     *         seats to their available pools and the wallet amounts. Always returns HTTP code 200(OK).<br/>
     * </p>
     * TODO: Add parameter and return documentation
     */
    @DeleteMapping("/bookings")
    ResponseEntity<?> deleteBookings() {
        List<Booking> bookings = this.bookingRepository.findAll();
        for (Booking booking : bookings) {
            System.out.println("Booking Entry: " + booking.toString());
            //TODO: Handle the case of returning seats to available pool and wallet amount
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
