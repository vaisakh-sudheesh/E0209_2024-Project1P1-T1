/**
 * Service layer for interfacing between controller and repository.
 * Includes the code serialization and ensuring synchronous database/repository update.
 */
package com.iisc.csa.pods.projects.booking.service;

import com.iisc.csa.pods.projects.booking.exception.*;
import com.iisc.csa.pods.projects.booking.model.Booking;
import com.iisc.csa.pods.projects.booking.model.BookingPayload;
import com.iisc.csa.pods.projects.booking.model.Show;
import com.iisc.csa.pods.projects.booking.model.Theatre;
import com.iisc.csa.pods.projects.booking.repository.BookingRepository;
import com.iisc.csa.pods.projects.booking.repository.ShowRepository;
import com.iisc.csa.pods.projects.booking.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
public class BookingService {
    // Repository instances for accessing theatre, shows and booking entities
    @Autowired
    private TheatreRepository theatreRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private BookingRepository bookingRepository;

    ////////////////////////////////////// URI Management //////////////////////////////////////
    /**
     * Since each of the microservices that are part of this project have separate in-memory database entities,
     * interaction between these microservices need to be done over HTTP/Rest request.
     * URIs for the doing the same.<br/>
     *
     * Two are maintained, as both docker and non-docker invocation of service will have different URIs.
     */
    @Value("${DOCKER_URL_USER:localhost:8080}")
    private String uriStrngUsers;

    @Value("${DOCKER_URL_WALLET:localhost:8082}")
    private String uriStrngWallet;

    /**
     * Helper methods and fields for user microservice URI
     */
    private String getUserCheckUri() {
        return "http://"+uriStrngUsers+"/users/{user_id}";
    }

    /**
     * Helper methods and fields for wallet microservice URI
     */
    private String getWalletUserCheckUri () {
        return "http://"+uriStrngWallet+"/wallets/{user_id}";
    }

    ////////////////////////////////////// Service methods //////////////////////////////////////

    /**
     * Method to obtain list of theatres
     * @return List of theatres
     */
    @Transactional
    public List<Theatre> getTheatres() {
        List<Theatre> theatres = this.theatreRepository.findAll();
        return theatres;
    }

    /**
     * Method to obtain list of shows from theatre
     * @param theater_id theatre id
     * @return list of shows
     */
    public List<Show> getShowsTheatres (Integer theater_id) {
        // Check for validity of provided theatre_id
        if (!this.theatreRepository.existsById(theater_id)) {
            throw new TheatreInfoException(theater_id);
        }
        return this.showRepository.findByTheatre_id(theater_id);
    }

    /**
     * Method to obtain show information
     * @param show_id show id
     * @return Show information
     */
    public Show getShows (Integer show_id) {
        // Check for validity of show_id prior to fetching details
        if (!this.showRepository.existsById(show_id)) {
            throw new ShowInfoException(show_id);
        }
        return this.showRepository.findByShowId(show_id);
    }

    /**
     * Method to obtain Booking information of a user
     *
     * @param user_id user id to be queried
     * @return List of booking
     */
    public List<Booking> getBookingsUsers(Integer user_id) {
        return this.bookingRepository.findByUser_id(user_id);
    }

    /**
     * Transaction method to inssue booking request and communicate to other services too
     * @param bookingreq Booking request information
     */
    public synchronized void transact(BookingPayload bookingreq){

        // Check for validity of show_id
        if (!this.showRepository.existsById(bookingreq.getShow_id())) {
            throw new ShowInfoException(bookingreq.getShow_id());
        }

        // Check if the user_id is a valid one by RestAPI call to User service
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Since each of the microservices in this project have separate in-memory database entities,
            // interaction between these microservices need to be done over HTTP/Rest request.
            // URIs for the doing the same.
            String user_check_uri = getUserCheckUri();
            restTemplate.getForObject(user_check_uri, String.class, bookingreq.getUser_id());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                System.out.println("User check failed");
                throw new UserValidationException(bookingreq.getUser_id());
            }
        }

        // Check for availability of seat
        Show show_details = this.showRepository.findByShowId(bookingreq.getShow_id());
        if (show_details.getSeats_available() < bookingreq.getSeats_booked()) {
            throw new UnmetBookingRequirementException("Not enough seats available"+show_details.getSeats_available()+
                    " for a request of "+bookingreq.getSeats_booked());
        }

        // Special case handling: Handle 0 booking request case
        if (bookingreq.getSeats_booked() <= 0 ) {
            throw new UnmetBookingRequirementException("Attempting to book for zero or negative number of seats -  "+
                    bookingreq.getSeats_booked());
        }

        // Perform wallet transaction
        Integer ticket_cost = bookingreq.getSeats_booked() * show_details.getPrice();
        if (!this.WalletTransaction(bookingreq.getUser_id(), true, ticket_cost)) {
            throw new WalletOperationException("Wallet operation for booking from "+bookingreq.getUser_id() +
                    "failed for ticket cost"+ticket_cost);
        }

        // Update Booking table for the successful booking
        this.bookingRepository.save(new Booking(show_details, bookingreq.getUser_id(), bookingreq.getSeats_booked()));

        // Update available seat counter
        show_details.setSeats_available(show_details.getSeats_available() - bookingreq.getSeats_booked());
        this.showRepository.save(show_details);
    }

    /**
     * Method to delete bookings from a user
     * @param user_id
     */
    public synchronized void deleteUsers(Integer user_id) {
        // Sanity check for the user_id parameter
        if (!this.bookingRepository.existsByUser_id(user_id)){
            throw new UserValidationException(user_id);
        }

        // Now handle the case of return of seats and refund
        List<Booking> bookings = this.bookingRepository.findByUser_id(user_id);

        // Process each booking and perform refunds and return of seats
        for (Booking booking : bookings) {
            if (!this.CancelBooking(booking)) {
                System.out.println("deleteUsers: Cancelling booking failed");
                throw new WalletOperationException("Cancellation failed for booking "+booking.toString()+
                        " due to wallet error");
            }
            this.bookingRepository.deleteById(booking.getId());
        }
        // this delete may not be required, as each rows are removed iteratively
        //this.bookingRepository.deleteAllByUser_id(user_id);
    }

    /**
     * Method to delete booking from user for a given show
     * @param user_id
     * @param show_id
     */
    public synchronized void deleteUsersShows(Integer user_id, Integer show_id){
        Show show = this.showRepository.findByShowId(show_id);

        // Sanity check of arguments prior to processing
        if (!this.bookingRepository.existsByUser_idAndShow_id(user_id, show)){
            throw new ShowInfoException(show_id);

        }

        // Now handle the case of return of seats and refund
        List<Booking> bookings = this.bookingRepository.findAllByUser_idAndShow_id(user_id, show);

        // Process each booking and perform refunds and return of seats
        for (Booking booking : bookings) {
            if (!this.CancelBooking(booking)) {
                System.out.println("deleteUsersShows: Cancelling booking failed");
                throw new WalletOperationException("Cancellation failed for booking "+booking.toString()+
                        " due to wallet error");
            }
            this.bookingRepository.deleteById(booking.getId());
        }
        // this delete may not be required, as each rows are removed iteratively
        //this.bookingRepository.deleteAllByUser_idAndShow_id(user_id, show);

    }

    /**
     * Delete all the bookings
     */
    public synchronized void deleteBookings (){
        List<Booking> bookings = this.bookingRepository.findAll();
        for (Booking booking : bookings) {
            if (!this.CancelBooking(booking)) {
                System.out.println("deleteBookings: Cancelling booking failed");
                throw new WalletOperationException("Cancellation failed for booking "+booking.toString()+
                        " due to wallet error");
            }
            this.bookingRepository.deleteById(booking.getId());
        }
    }


    ////////////////////////////////////// Helper methods //////////////////////////////////////
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
            String wallet_action_uri = getWalletUserCheckUri();
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
