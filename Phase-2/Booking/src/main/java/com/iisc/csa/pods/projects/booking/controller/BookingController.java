/**
 * Controller module for Booking microservice
 */
package com.iisc.csa.pods.projects.booking.controller;

import com.iisc.csa.pods.projects.booking.exception.ShowInfoException;
import com.iisc.csa.pods.projects.booking.exception.TheatreInfoException;
import com.iisc.csa.pods.projects.booking.exception.UserValidationException;
import com.iisc.csa.pods.projects.booking.exception.WalletOperationException;
import com.iisc.csa.pods.projects.booking.model.*;
import com.iisc.csa.pods.projects.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookingController {
    @Autowired
    BookingService bookingService;

    ////////////////////////////////////// Controller Endpoints //////////////////////////////////////
    /**
     * <b><u>Endpoint Requirement:</u> 1. GET /theatres</b>
     * <p>
     *
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
        return ResponseEntity.ok(bookingService.getTheatres());
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
        try {
            return ResponseEntity.ok(bookingService.getShowsTheatres(theater_id));
        } catch (Exception e) {
            System.out.println("getShowsTheatres: Exception "+ e.toString());
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
        try {
            return ResponseEntity.ok(bookingService.getShows(show_id));
        } catch (Exception e) {
            System.out.println("getShows: Exception "+ e.toString());
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
        return ResponseEntity.ok(bookingService.getBookingsUsers(user_id));
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
        try {
            bookingService.transact(bookingreq);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("postBookings: Exception "+ e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
        try {
            bookingService.deleteUsers(user_id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(UserValidationException e) {
            System.out.println("deleteUsers: UserValidationException "+ e.toString());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.out.println("deleteUsers: Exception "+ e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
        try {
            bookingService.deleteUsersShows(user_id,show_id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ShowInfoException e) {
            System.out.println("deleteUsersShows: ShowInfoException "+ e.toString());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.out.println("deleteUsersShows: Exception "+ e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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
        try {
            bookingService.deleteBookings();
            return new ResponseEntity<>(HttpStatus.OK);
        }  catch (WalletOperationException e) {
            System.out.println("deleteBookings: WalletOperationException "+ e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
