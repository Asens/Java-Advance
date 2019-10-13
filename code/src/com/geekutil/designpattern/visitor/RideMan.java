package com.geekutil.designpattern.visitor;

/**
 * @author Asens
 * create 2019-10-13 14:21
 **/

public class RideMan implements Visitor {
    @Override
    public void visit(Horse horse) {
        System.out.println("ride horse");
    }

    @Override
    public void visit(Cow cow) {
        System.out.println("ride cow");
    }
}
