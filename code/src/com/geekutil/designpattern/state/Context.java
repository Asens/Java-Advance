package com.geekutil.designpattern.state;

/**
 * @author Asens
 * create 2019-10-13 17:29
 **/

public class Context {
    private State state;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
