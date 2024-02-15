/**
 * Controller module for Booking microservice
 */
package com.iisc.csa.pods.projects.booking.controller;

import com.iisc.csa.pods.projects.booking.model.*;
import com.iisc.csa.pods.projects.booking.repository.BookingRepository;
import com.iisc.csa.pods.projects.booking.repository.ShowRepository;
import com.iisc.csa.pods.projects.booking.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
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

    @Value("${DOCKER_RUNNING:No}")
    private String dockerStatus;


    /**
     * <b><u>Endpoint Requirement:</u></b>
     * <p>
     *     1. GET /theatres
     *        This endpoint returns the list of all available theatres.<br/><br/>
     *
     *        Response JSON payload: [{“id”: Integer, “name”: String, “location”: String}] with
     *        HTTP status code 200 (OK). (Return empty list if no theatres are there.)<br/>
     * </p>
     *
     * @return HTTP/OK (200) on success with JSON Payload of theatre info
     */
    @GetMapping("/theatres")
    ResponseEntity<?> getTheatres(){
        List<Theatre> theatres = this.theatreRepository.findAll();
        return ResponseEntity.ok(theatres);
    }

    /**
     * <b><u>Endpoint Requirement:</u> 2. GET /shows/theatres/{theatre_id}</b><br/><br/>
     * <p>
     *        This endpoint returns the list of all shows being showcased at the theatre with
     *        theatre_id.<br/><br/>
     *
     *        Response JSON payload: [{“id”: Integer, ”theatre_id”: Integer, “title”: String, “price”:
     *        Integer, “seats_available”: Integer}] with HTTP status code 200 (OK). (Return empty
     *        list if no shows are there for the given theatre.)<br/><br/>
     *
     *        If the theatre does not exist then return HTTP 404 (Not Found).<br/>
     * </p>
     *
     * @param theater_id TheatreID to be queried
     * @return HTTP/OK(200) on success with JSON payload of Show info; In case of failure return HTTP/NotFound(404)
     */
    @GetMapping("/shows/theatres/{theater_id}")
    ResponseEntity<?> getShowsTheatres(@PathVariable Integer theater_id) {
        if (this.theatreRepository.existsById(theater_id)) {
            // Check for validity of provided theatre_id
            List<Show> shows = this.showRepository.findByTheatre_id(theater_id);
            return ResponseEntity.ok(shows);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <b><u>Endpoint Requirement:</u> 3. GET /shows/{show_id}</b><br/><br/>
     * <p>
     *        This endpoint returns the details of the show with ID show_id.<br/><br/>
     *
     *        Response JSON payload: {“id”: Integer, ”theatre_id”: Integer, “title”: String, “price”:
     *        Integer, “seats_available”: Integer} with HTTP status code 200 (OK).<br/><br/>
     *
     *        If the show doesn’t exist, return HTTP 404 (Not Found).<br/>
     * </p>
     *
     * @param show_id ShowID to be queried
     * @return HTTP/OK(200) with JSON payload with show information in case of show being present;
     *         HTTP/NotFound(404) otherwise.
     */
    @GetMapping("/shows/{show_id}")
    ResponseEntity<?> getShows(@PathVariable Integer show_id) {
        if (this.showRepository.existsById(show_id)) {
            // Check for validity of show_id prior to fetching details
            Show show = this.showRepository.findByShowId(show_id);
            return ResponseEntity.ok(show);
        } else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <b><u>Endpoint Requirement:</u> 4. GET /bookings/users/{user_id}</b><br/><br/>
     * <p>
     *        This endpoint returns the list of all bookings made by the user user_id.<br/><br/>
     *
     *        Response JSON payload: [{“id”: Integer, “show_id”: Integer, “user_id”: Integer,
     *        “seats_booked”: Integer}] with HTTP status code 200 (OK). To return an empty list if
     *        the user does not have any bookings.<br/><br/>
     * </p>
     *
     * @param user_id UserID for which query is to be performed
     * @return HTTP/OK(200) with JSON payload containing list of show information for supplied UserID.
     */
    @GetMapping("/bookings/users/{user_id}")
    ResponseEntity<?> getBookingsUsers(@PathVariable Integer user_id) {
        List<Booking> bookings = this.bookingRepository.findByUser_id(user_id);
        return ResponseEntity.ok(bookings);
    }

    /**
     * <b><u>Endpoint Requirement:</u> 5. POST /bookings</b><br/><br/>
     * <p>
     *        Request JSON payload of the form {“show_id”: Integer, ”user_id”: Integer,
     *        “seats_booked”: Integer}.<br/><br/>
     *
     *        This endpoint is invoked by the user to create a booking for show with ID show_id.<br/><br/>
     *
     *        If the show does not exist, return HTTP 400 (Bad Request).<br/><br/>
     *
     *        If the user does not exist, return HTTP 400 (Bad Request).<br/><br/>
     *
     *        If the number of seats available for the show is less than seats_booked, return HTTP
     *        400 (Bad Request).<br/><br/>
     *
     *        This end point needs to make use of the Wallet service to deduct an amount equal to
     *        the price of the show times seats_booked. If the Wallet deduction does not happen
     *        because of insufficient balance in the user’s wallet, return HTTP 400 (Bad Request),
     *        else reduce the number of seats available in the show by seats_booked and return
     *        HTTP 200 (OK).<br/>
     * </p>
     *
     * @param bookingreq JSON Payload with booking information to be processed
     * @return HTTP/OK(200) on successful booking with wallet and seat counts in respective entities updated. Returns
     *      HTTP/BadRequest(400) on error otherwise.
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
            // Since each of the microservices in this project have separate in-memory database entities,
            // interaction between these microservices need to be done over HTTP/Rest request.
            // URIs for the doing the same.
            String user_check_uri = dockerStatus.equals("Yes") ?
                    "http://host.docker.internal:8080/users/{user_id}" :
                    "http://localhost:8080/users/{user_id}";
            String result = restTemplate.getForObject(user_check_uri, String.class, bookingreq.getUser_id());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                System.out.println("User check failed");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        // Check for availability of seat
        Show show_details = this.showRepository.findByShowId(bookingreq.getShow_id());
        if (show_details.getSeats_available() < bookingreq.getSeats_booked()) {
            System.out.println("Not enough seats available"+show_details.getSeats_available()+" for a request of "+bookingreq.getSeats_booked());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //// Special case handling: Handle 0 booking request case
        if (bookingreq.getSeats_booked() <= 0 ) {
            System.out.println("Attempting to book for zero or negative number of seats -  "+bookingreq.getSeats_booked());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Perform wallet transaction
        Integer ticket_cost = bookingreq.getSeats_booked() * show_details.getPrice();
        if (!this.WalletTransaction(bookingreq.getUser_id(), true, ticket_cost)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Update Booking table for the successful booking
        this.bookingRepository.save(new Booking(show_details, bookingreq.getUser_id(), bookingreq.getSeats_booked()));

        // Update available seat counter
        show_details.setSeats_available(show_details.getSeats_available() - bookingreq.getSeats_booked());
        this.showRepository.save(show_details);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * <b><u>Endpoint Requirement:</u> 6. DELETE /bookings/users/{user_id}</b><br/><br/>
     * <p>
     *        Deletes all booking records for the user with ID user_id. The seats corresponding to
     *        these bookings are to be returned to the available pool. The amounts that were taken
     *        to make these bookings are returned to the user’s wallet.<br/><br/>
     *
     *        If the user had any bookings then return HTTP Code 200 (OK), else return HTTP
     *        code 404 (Not Found).<br/><br/>
     * </p>
     *
     * @param user_id UserID for which deletion is to be performed
     * @return HTTP/OK(200) with booking deleted along with wallet/seats returned to respective system. Returns
     *      HTTP/NotFound(404) on error otherwise.
     */
    @DeleteMapping("/bookings/users/{user_id}")
    ResponseEntity<?> deleteUsers(@PathVariable Integer user_id) {
        if (!this.bookingRepository.existsByUser_id(user_id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Now handle the case of return of seats and refund
        List<Booking> bookings = this.bookingRepository.findByUser_id(user_id);

        // Process each booking and perform refunds and return of seats
        for (Booking booking : bookings) {
            if (!this.CancelBooking(booking)) {
                /*
                 * TODO/BUG: Possible failure point here, in case one of the transaction failed, it will prevent
                 *           removal of rows. Fix it by removing each row on successful completion rather than
                 *           failing in between.
                 */
                System.out.println("deleteUsers: Cancelling booking failed");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        this.bookingRepository.deleteAllByUser_id(user_id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * <b><u>Endpoint Requirement:</u> 7. DELETE /bookings/users/{user_id}/shows/{show_id}</b><br/><br/>
     * <p>
     *         Deletes all booking records for the user with ID user_id whose show ID is show_id.
     *         The seats corresponding to these bookings are to be returned to the available pool,
     *         and the booking amount is also returned to the wallet.<br/><br/>
     *
     *         If the user had any bookings in the given show then return HTTP Code 200 (OK),
     *         else return HTTP code 404 (Not Found).<br/>
     * </p>
     *
     * @param user_id UserID for which deletion is to be performed
     * @param show_id ShowID for which deleteion is to be performed
     * @return HTTP/OK(200) with booking deleted along with wallet/seats returned to respective system. Returns
     *         HTTP/NotFound(404) on error otherwise.
     */
    @DeleteMapping("/bookings/users/{user_id}/shows/{show_id}")
    ResponseEntity<?> deleteUsersShows(@PathVariable Integer user_id, @PathVariable Integer show_id) {
        Show show = this.showRepository.findByShowId(show_id);
        if (!this.bookingRepository.existsByUser_idAndShow_id(user_id, show)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Now handle the case of return of seats and refund
        List<Booking> bookings = this.bookingRepository.findAllByUser_idAndShow_id(user_id, show);

        // Process each booking and perform refunds and return of seats
        for (Booking booking : bookings) {
            if (!this.CancelBooking(booking)) {
                /*
                 * TODO/BUG: Possible failure point here, in case one of the transaction failed, it will prevent
                 *           removal of rows. Fix it by removing each row on successful completion rather than
                 *           failing in between.
                 */
                System.out.println("deleteUsersShows: Cancelling booking failed");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        this.bookingRepository.deleteAllByUser_idAndShow_id(user_id, show);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * <b><u>Endpoint Requirement:</u> 8. DELETE /bookings</b><br/><br/>
     * <p>
     *         This endpoint deletes all bookings of all users in all shows, and returns back the
     *         seats to their available pools and the wallet amounts. Always returns HTTP code 200(OK).<br/>
     * </p>
     *
     * @return HTTP/OK (200) returned after all bookings are deleted along with wallet/seats refund.
     */
    @DeleteMapping("/bookings")
    ResponseEntity<?> deleteBookings() {
        List<Booking> bookings = this.bookingRepository.findAll();
        for (Booking booking : bookings) {
            if (!this.CancelBooking(booking)) {
                System.out.println("deleteBookings: Cancelling booking failed");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Helper method for Canceling a given booking
     *
     * @param booking Instance of booking to be canceled
     * @return True on successful cancellation; false otherwise
     */
    boolean CancelBooking (Booking booking){
        boolean result = true;
        Integer seats_booked = booking.getSeats_booked();

        Show showinfo = this.showRepository.findByShowId(booking.getShow_id().getId());
        Integer refund_amount = seats_booked * showinfo.getPrice();

        // Return the booking amount to the wallet.
        if (!this.WalletTransaction(booking.getUser_id(), false, refund_amount)) {
            result = false;
        } else {// Return seats corresponding to these bookings to the available pool of show
            //System.out.println("Cancelling booking, returning seat count :"+seats_booked);
            showinfo.setSeats_available(showinfo.getSeats_available() + seats_booked);
            this.showRepository.save(showinfo);
        }

        return result;
    }
    /**
     * Utility method for performing booking refund operation
     * It performs:
     *   - Wallet Credit of amount
     *   - Returning cancelled seats to system.
     *
     * @param user_id_ user id to which wallet transaction is to be performed.
     * @return True on successful completion, false in case of any failure in wallet transactions.
     */
    boolean WalletTransaction(Integer user_id_ , boolean isDebit, Integer amount) {
        // Wallet transaction
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("action", isDebit ? "debit":"credit");
            requestBody.put("amount", amount);

            // Create an HttpEntity object with the request body and headers
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Define URI variables for the user ID
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("user_id", user_id_.toString());

            // Make the HTTP/PUT request
            String wallet_action_uri = dockerStatus.equals("Yes") ?
                    "http://host.docker.internal:8082/wallets/{user_id}" : "http://localhost:8082/wallets/{user_id}";
            ResponseEntity<String> response = restTemplate.exchange(wallet_action_uri, HttpMethod.PUT, entity, String.class, uriVariables);

            if (response.getStatusCode().is2xxSuccessful())
                return true;

        } catch (HttpClientErrorException e) {
            System.out.println("Wallet transaction failed with " + e.getStatusCode());
            return false;
        }
        return false;
    }


}
