package com.geekutil.designpattern.visitor;

/**
 * @author Asens
 * create 2019-10-13 14:16
 **/

public interface Visitor {
    void visit(Horse horse);

    void visit(Cow cow);
}
