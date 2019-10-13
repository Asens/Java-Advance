package com.geekutil.designpattern.memento;

/**
 * @author Asens
 * create 2019-10-13 16:51
 **/

public class Originator {
    private String state;

    public Memento createMemento(){
        return new Memento(state);
    }

    public void restoreMemento(Memento m) {
        this.setState(m.getState());
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
