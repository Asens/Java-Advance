package com.geekutil.designpattern.chain;

import java.util.Calendar;

/**
 * @author Asens
 * create 2019-10-11 22:40
 **/

public class Client {
    public static void main(String[] args) {
        Handler[] handlers = {new HandlerOne(),new HandlerTwo(),new HandlerThree()};
        Chain first = null, tail = null;
        for (int i = 0; i < handlers.length - 1; i++) {
            if(i==0){
                tail = new Chain();
                tail.setHandler(handlers[i]);
                first = tail;
            }
            tail.setNextChain(new Chain(handlers[i+1]));
            tail = tail.getNextChain();
        }
        first.handle("a");
    }
}
