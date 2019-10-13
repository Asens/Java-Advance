package com.geekutil.designpattern.memento;

/**
 * @author Asens
 * create 2019-10-13 16:51
 **/

public class Caretaker {
    private Memento memento;

    public Memento getMemento() {
        return memento;
    }

    public void setMemento(Memento memento) {
        this.memento = memento;
    }
}
