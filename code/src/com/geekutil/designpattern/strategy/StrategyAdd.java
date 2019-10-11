package com.geekutil.designpattern.strategy;

/**
 * @author Asens
 */

public class StrategyAdd implements Strategy {
    @Override
    public int operation(int one, int two) {
        return one + two;
    }
}
