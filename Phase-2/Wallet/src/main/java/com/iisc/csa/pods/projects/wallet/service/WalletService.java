/**
 * Service layer for interfacing between controller and repository.
 * Includes the code serialization and ensuring synchronous database/repository update.
 */
package com.iisc.csa.pods.projects.wallet.service;

import com.iisc.csa.pods.projects.wallet.exception.UserValidationException;
import com.iisc.csa.pods.projects.wallet.exception.WalletOperationException;
import com.iisc.csa.pods.projects.wallet.model.Wallet;
import com.iisc.csa.pods.projects.wallet.model.WalletPutPayload;
import com.iisc.csa.pods.projects.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class WalletService {
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
    @Value("${DOCKER_URL_USER:localhost:8080}")
    private String uriStrngUser;

    /**
     * Helper methods and fields for user microservice URI
     */
    private String getUserCheckUri(){
        return "http://"+uriStrngUser+"/" + "users/{user_id}";
    }

    ////////////////////////////////////// Service Methods //////////////////////////////////////
    public Wallet getUser_id(Integer user_id) {
        if (!walletRepo.existsByUser_id(user_id)) {
            throw new UserValidationException(user_id);
        }
        return walletRepo.findByUser_id(user_id);
    }

    public synchronized Wallet transact(WalletPutPayload payload, Integer user_id){
        //System.out.print("Wallet: transact( "+user_id+","+payload.getAmount()+","+payload.getAction()+")");
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
                    throw new UserValidationException(user_id);
                }
            }
            /* Create wallet account and load the amount and continue with rest of the operation */
            //System.out.println("transact: New wallet account creation ");
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
                throw new WalletOperationException("credit", "Insufficient Balance in Wallet for user_id"+user_id);
            }
            /* Balance amount is sufficient, hence processing transaction */
            existingWallet.setBalance(existingWallet.getBalance() - payload.getAmount());
        }

        /* Update record and return JSON payload with HTTP/OK status */
        //System.out.println("transact: Updating existing wallet ");
        this.walletRepo.save(existingWallet);
        //System.out.println("Wallet: transact( "+user_id+","+payload.getAmount()+","+payload.getAction()+") complete");
        return existingWallet;
    }

    public synchronized void deleteUser_id (Integer user_id) {
        if (!this.walletRepo.existsByUser_id(user_id)){
            throw new UserValidationException(user_id);
        }
        this.walletRepo.deleteByUser_id(user_id);
    }

    public synchronized void deleteAll () {
        this.walletRepo.deleteAll();
    }
}
