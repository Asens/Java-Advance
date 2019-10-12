package com.geekutil.designpattern.command;

/**
 * @author Asens
 * create 2019-10-12 22:03
 **/

public class UpCommand implements Command {
    private Receiver receiver;

    public UpCommand(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        receiver.up();
    }
}
