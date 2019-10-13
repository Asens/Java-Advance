package com.geekutil.designpattern.visitor;

/**
 * @author Asens
 * create 2019-10-13 14:17
 **/

public class Cow implements Animal {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
