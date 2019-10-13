package com.geekutil.designpattern.templatemethod;

/**
 * @author Asens
 * create 2019-10-13 11:34
 **/

public abstract class AbstractPerson {
    public void happyDay(){
        wakeUp();
        doSomething();
        sleep();
    }

    private void wakeUp(){
        System.out.println("wake up in the morning");
    }

    private void sleep(){
        System.out.println("sleep in the night");
    }

    protected abstract void doSomething();
}
