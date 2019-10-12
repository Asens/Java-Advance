package com.geekutil.designpattern.command;

/**
 * @author Asens
 * create 2019-10-12 22:04
 **/

public class Invoker {
    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void action(){
        command.execute();
    }
}
