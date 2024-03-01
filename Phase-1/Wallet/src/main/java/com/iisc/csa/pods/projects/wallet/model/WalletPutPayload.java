/**
 * Model for payload for PUT(Wallet transaction)
 */
package com.iisc.csa.pods.projects.wallet.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletPutPayload {

    private String action;  // credit or debit string
    private Integer amount;

    // Utility methods to check whether associated payload is credit or debit
    public boolean isCreditAction(){
        return this.getAction().equals("credit");
    }

    public boolean isDebitAction(){
        return this.getAction().equals("debit");
    }
}
