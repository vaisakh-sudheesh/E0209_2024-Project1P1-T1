/**
 * Controller class for Wallet Service
 *
 */
package com.iisc.csa.pods.projects.wallet.controller;

import com.iisc.csa.pods.projects.wallet.exception.UserValidationException;
import com.iisc.csa.pods.projects.wallet.exception.WalletOperationException;
import com.iisc.csa.pods.projects.wallet.model.Wallet;
import com.iisc.csa.pods.projects.wallet.model.WalletPutPayload;
import com.iisc.csa.pods.projects.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/wallets")
public class WalletController {
    @Autowired
    WalletService walletService;
    ////////////////////////////////////// Controller Endpoints //////////////////////////////////////
    /**
     * <b><u>Endpoint requirement:</u>  1. GET /wallets/{user_id}</b>
     * <p>
     *    This endpoint gets the wallet details for the user with ID user_id. <br/><br/>
     *
     *    Response JSON payload is {“user_id:” Integer, “balance”: Integer} with HTTP status
     *    code 200 (OK) if the user has a wallet.<br/><br/>
     *
     *    If the user doesn’t have a wallet, return HTTP status code 404 (Not Found).<br/>
     * </p>
     *
     * @param user_id user_id for associated wallet.
     * @return JSON Payload with user information as stated in requirement and HTTP status code 200 (OK) on success.
     *         HTTP status code 404 (Not Found), if wallet is not present for user.
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<Wallet> getUser_id(@PathVariable Integer user_id){
        try {
            Wallet wallet_data = walletService.getUser_id(user_id);
            return new ResponseEntity<>(wallet_data, HttpStatus.OK);
        } catch (UserValidationException e) {
            System.out.println("getUser_id: UserValidationException "+ e.toString());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.out.println("getUser_id: Exception "+ e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * <b><u>Endpoint requirement:</u>  2. PUT /wallets/{user_id}</b>
     * <p>
     *    Request JSON payload of the form {“action”: “debit”/“credit”, "amount": Integer}<br/><br/>
     *
     *    If the mentioned user does not currently have a wallet, then create a wallet for them,
     *    initialize its balance to zero, and proceed with the remaining steps below.<br/><br/>
     *
     *    This endpoint debits/credits the specified amount to the user’s wallet balance.<br/><br/>
     *
     *    If the action requested is a debit, and there is insufficient balance, then do not update
     *    the balance and return HTTP status code 400 (Bad Request).<br/><br/>
     *
     *    In all other cases, return response JSON payload {“user_id”: Integer, “balance”:
     *    Integer}, where “balance” is the updated balance, with HTTP status code 200 (OK).<br/>
     * </p>
     *
     * @param payload Payload of WalletPutPayload type to share action(debit/credit) and amount
     * @param user_id user_id for associated wallet.
     * @return HTTP status code 200 (OK) on success and 400(Bad Request) on insufficient wallet balance.
     */
    @PutMapping("/{user_id}")
    public ResponseEntity<Wallet> putUser_id(@RequestBody WalletPutPayload payload, @PathVariable Integer user_id) {
        try {
            Wallet walletInfo = walletService.transact(payload, user_id);
            return new ResponseEntity<>(walletInfo, HttpStatus.OK);
        } catch (UserValidationException | WalletOperationException e) {
            System.out.println("putUser_id: UserValidationException | WalletOperationException  "+ e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println("putUser_id: Exception "+ e.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <b><u>Endpoint requirement:</u>  3. DELETE /wallets/{user_id}</b>
     * <p>
     *       Deletes the wallet of the user with ID user_id.<br/><br/>
     *
     *       Upon successful deletion send a HTTP status code 200 (OK).<br/><br/>
     *
     *       If the user does not have a wallet, return an HTTP status code 404 (Not Found).<br/>
     * </p>
     *
     * @param user_id User identifier for associated wallet.
     * @return HTTP status code 200 (OK) on success and 404(Not Found) on no existing wallet.
     */
    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteUser_id(@PathVariable Integer user_id) {
        try {
            walletService.deleteUser_id(user_id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserValidationException e) {
            System.out.println("deleteUser_id: UserValidationException "+ e.toString());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.out.println("deleteUser_id: Exception "+ e.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <b><u>Endpoint requirement:</u>  4. DELETE /wallets</b>
     * <p>
     *       This endpoint deletes the wallets of all users, and returns HTTP status code 200 (OK).<br/>
     * </p>
     *
     * @return return HTTP status code 200 (OK) always
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteAll() {
        walletService.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
