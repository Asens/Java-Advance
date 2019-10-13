package com.geekutil.designpattern.state;

/**
 * @author Asens
 * create 2019-10-13 17:31
 **/

public class StartState implements State {
    @Override
    public void execute(Context context) {
        System.out.println("start execute");
        context.setState(this);
    }
}
