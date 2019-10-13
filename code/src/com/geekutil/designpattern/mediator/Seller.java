package com.geekutil.designpattern.mediator;

/**
 * @author Asens
 * create 2019-10-13 9:51
 **/

public class Seller {
    private Mediator mediator;

    public Seller(Mediator mediator) {
        this.mediator = mediator;
    }

    public void sendToBuyer(String msg){
        mediator.sendToBuyer(msg);
    }

    public void receiveMsg(String msg){
        System.out.println("Seller get message:"+msg);
    }
}
