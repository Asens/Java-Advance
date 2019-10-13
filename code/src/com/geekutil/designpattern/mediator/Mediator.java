package com.geekutil.designpattern.mediator;

/**
 * @author Asens
 * create 2019-10-13 9:47
 **/

public class Mediator {
    private Buyer buyer;
    private Seller seller;

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public void sendToSeller(String msg){
        seller.receiveMsg(msg);
    }

    public void sendToBuyer(String msg) {
        buyer.receiveMsg(msg);
    }

    public void sendToAll(String msg){
        seller.receiveMsg(msg);
        buyer.receiveMsg(msg);
    }
}
