package com.geekutil.designpattern.visitor;

/**
 * @author Asens
 * create 2019-10-13 14:20
 **/

public class FeedMan implements Visitor {
    @Override
    public void visit(Horse horse) {
        System.out.println("feed horse");
    }

    @Override
    public void visit(Cow cow) {
        System.out.println("feed cow");
    }
}
