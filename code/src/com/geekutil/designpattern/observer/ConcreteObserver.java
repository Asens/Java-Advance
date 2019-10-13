package com.geekutil.designpattern.observer;

/**
 * @author Asens
 * create 2019-10-13 15:06
 **/

public class ConcreteObserver implements Observer {
    private String name;

    public ConcreteObserver(String name) {
        this.name = name;
    }

    @Override
    public void update(String event) {
        System.out.println(name+" handle + "+event);
    }
}
