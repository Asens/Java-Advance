package com.geekutil.designpattern.strategy;

/**
 * @author Asens
 * create 2019-10-11 22:13
 **/

public class Context {
    private Strategy strategy;

    public Context(Strategy strategy) {
        this.strategy = strategy;
    }

    public int operation(int one,int two){
        return strategy.operation(one,two);
    }
}
