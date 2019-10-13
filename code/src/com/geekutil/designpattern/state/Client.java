package com.geekutil.designpattern.state;

import javafx.scene.paint.Stop;

/**
 * @author Asens
 * create 2019-10-13 17:28
 **/

public class Client {
    public static void main(String[] args) {
        Context context = new Context();
        State start = new StartState();
        start.execute(context);

        State stop = new StopState();
        stop.execute(context);
    }
}
