package com.geekutil.designpattern.visitor;

/**
 * @author Asens
 * create 2019-10-13 14:16
 **/

public class Horse implements Animal {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
