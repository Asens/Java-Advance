package com.geekutil.designpattern.visitor;

/**
 * @author Asens
 * create 2019-10-13 14:15
 **/

public class Client {
    public static void main(String[] args) {
        Cow cow = new Cow();
        Horse horse = new Horse();

        Visitor rideMan = new RideMan();
        rideMan.visit(cow);
        rideMan.visit(horse);

        Visitor feedMan = new FeedMan();
        feedMan.visit(cow);
        feedMan.visit(horse);
    }
}
