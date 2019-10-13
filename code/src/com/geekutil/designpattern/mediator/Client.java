package com.geekutil.designpattern.mediator;

/**
 * @author Asens
 * create 2019-10-13 9:34
 **/

public class Client {
    public static void main(String[] args) {
        Mediator mediator = new Mediator();
        Buyer buyer = new Buyer(mediator);
        Seller seller = new Seller(mediator);
        mediator.setBuyer(buyer);
        mediator.setSeller(seller);

        seller.sendToBuyer("buy something?");
        buyer.sendToSeller("yes");

        mediator.sendToAll("market message");
    }
}
