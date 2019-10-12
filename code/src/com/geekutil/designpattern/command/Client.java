package com.geekutil.designpattern.command;

/**
 * @author Asens
 * create 2019-10-12 21:36
 **/

public class Client {
    public static void main(String[] args) {
        Receiver receiver = new Receiver();

        UpCommand upCommand = new UpCommand(receiver);
        DownCommand downCommand = new DownCommand(receiver);

        Invoker invoker = new Invoker();
        invoker.setCommand(upCommand);
        invoker.action();

        invoker.setCommand(downCommand);
        invoker.action();
    }
}
