package com.geekutil.designpattern.memento;

/**
 * @author Asens
 * create 2019-10-13 16:51
 **/

public class Memento {
    private String state;

    public Memento(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
