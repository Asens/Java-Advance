package com.geekutil.designpattern.interpreter;

import java.util.Map;

/**
 * @author Asens
 * create 2019-10-13 16:17
 **/

public class AddExpression implements Expression{
    private Expression left;
    private Expression right;

    public AddExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int interpret(Map<String,Integer> map) {
        return left.interpret(map)+right.interpret(map);
    }
}
