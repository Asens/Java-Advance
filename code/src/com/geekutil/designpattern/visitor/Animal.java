package com.geekutil.designpattern.visitor;

/**
 * @author Asens
 * create 2019-10-13 14:15
 **/

public interface Animal {
    void accept(Visitor visitor);
}
