package com.geekutil.designpattern.state;

/**
 * @author Asens
 * create 2019-10-13 17:34
 **/

public class StopState implements State {
    @Override
    public void execute(Context context) {
        System.out.println("stop execute");
        context.setState(this);
    }
}
