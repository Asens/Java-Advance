package com.geekutil.designpattern.memento;

/**
 * @author Asens
 * create 2019-10-13 16:51
 **/

public class Client {
    public static void main(String[] args) {
        Caretaker caretaker = new Caretaker();
        Originator originator = new Originator();
        originator.setState("0");
        //存档
        caretaker.setMemento(originator.createMemento());
        originator.setState("1");
        //回档
        originator.restoreMemento(caretaker.getMemento());
        System.out.println(originator.getState());
    }
}
