package com.geekutil.designpattern.templatemethod;

/**
 * @author Asens
 * create 2019-10-13 11:37
 **/

public class Farmer extends AbstractPerson {
    @Override
    protected void doSomething() {
        System.out.println("go to farm");
    }
}
