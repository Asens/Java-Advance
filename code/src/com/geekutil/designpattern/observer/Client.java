package com.geekutil.designpattern.observer;

/**
 * @author Asens
 * create 2019-10-13 14:54
 **/

public class Client {
    public static void main(String[] args) {
        Subject subject = new Subject();
        Observer observerOne = new ConcreteObserver("one");
        Observer observerTwo = new ConcreteObserver("two");
        subject.registerObserver(observerOne);
        subject.registerObserver(observerTwo);
        subject.notifyObservers("aa");
    }
}
