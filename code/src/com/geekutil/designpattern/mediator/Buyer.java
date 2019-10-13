package com.geekutil.designpattern.mediator;

/**
 * @author Asens
 * create 2019-10-13 9:49
 **/

public class Buyer{
    private Mediator mediator;

    public Buyer(Mediator mediator) {
        this.mediator = mediator;
    }

    public void sendToSeller(String msg){
        mediator.sendToSeller(msg);
    }

    public void receiveMsg(String msg){
        System.out.println("Buyer get message:"+msg);
    }
}
