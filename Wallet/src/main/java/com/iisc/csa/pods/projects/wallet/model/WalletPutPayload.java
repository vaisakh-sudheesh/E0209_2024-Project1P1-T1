package com.iisc.csa.pods.projects.wallet.model;

import lombok.Getter;
import lombok.Setter;

public class WalletPutPayload {
    @Getter
    @Setter
    private String action;

    @Setter
    @Getter
    private Integer amount;

    public boolean isCreditAction(){
        return this.getAction().equals("credit");
    }

    public boolean isDebitAction(){
        return this.getAction().equals("debit");
    }
}
