package com.geekutil.designpattern.strategy;

/**
 * @author Asens
 * create 2019-10-11 22:13
 **/

public class StrategyMultiply implements Strategy {
    @Override
    public int operation(int one, int two) {
        return one * two;
    }
}
