package com.geekutil.designpattern.strategy;

/**
 * @author Asens
 * create 2019-10-11 22:07
 **/

public class Client {
    public static void main(String[] args) {
        Context context = new Context(new StrategyAdd());
        System.out.println(context.operation(1,2));

        context = new Context(new StrategyMultiply());
        System.out.println(context.operation(1,2));
    }
}
