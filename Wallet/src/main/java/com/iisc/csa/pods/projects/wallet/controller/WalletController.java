/**
 * Controller class for Wallet Service
 *
 */
package com.iisc.csa.pods.projects.wallet.controller;

import com.iisc.csa.pods.projects.wallet.model.Wallet;
import com.iisc.csa.pods.projects.wallet.model.WalletPutPayload;
import com.iisc.csa.pods.projects.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/wallets")
public class WalletController {
    @Autowired
    WalletRepository walletRepo;

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
     * Helper methods and fields for user microservice URI
     */
    private String getUserUriBase(){

        String user_uri_docker = "http://host.docker.internal:8080/";
        String user_uri_localdev = "http://localhost:8080/";
        return (dockerStatus.equals("Yes") ? user_uri_docker : user_uri_localdev) ;
    }
    private String getUserCheckUri(){
        return getUserUriBase() + "users/{user_id}";
    }

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
            if (walletRepo.existsByUser_id(user_id)) {
                Wallet wallet_data = walletRepo.findByUser_id(user_id);
                return new ResponseEntity<>(wallet_data, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
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
            /*
             * Check if wallet exists or no; this may not be necessary but in case some other module or a direct API
             * call comes, a sanity check is desirable.
             */
            if (!walletRepo.existsByUser_id(user_id)) {
                /*
                 * Check if the user_id is a valid one prior to creating wallet entry by RestAPI call to User service
                 */
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    String query_url = getUserCheckUri();
                    restTemplate.getForObject(query_url, String.class, user_id);
                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode().is4xxClientError()) {
                        System.out.println("User check failed");
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }
                /* Create wallet account and load the amount and continue with rest of the operation */
                Wallet newWallet = new Wallet(user_id,0);
                this.walletRepo.save(newWallet);
            }

            /* Fetch the wallet account details and process the transaction */
            Wallet existingWallet = this.walletRepo.findByUser_id(user_id);
            if (payload.isCreditAction()) {
                /* A credit action, hence updating balance amount */
                existingWallet.setBalance(existingWallet.getBalance() + payload.getAmount());
            } else if (payload.isDebitAction()){
                /* Debit action */
                if (existingWallet.getBalance() < payload.getAmount()) {
                    /* Insufficient Wallet balance case */
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                /* Balance amount is sufficient, hence processing transaction */
                existingWallet.setBalance(existingWallet.getBalance() - payload.getAmount());
            }

            /* Update record and return JSON payload with HTTP/OK status */
            this.walletRepo.save(existingWallet);
            return new ResponseEntity<>(existingWallet, HttpStatus.OK);

        } catch (Exception e) {
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
            if (!this.walletRepo.existsByUser_id(user_id)){
                /* Is the wallet existing? */
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                this.walletRepo.deleteByUser_id(user_id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (Exception e) {
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
        this.walletRepo.deleteAll();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
