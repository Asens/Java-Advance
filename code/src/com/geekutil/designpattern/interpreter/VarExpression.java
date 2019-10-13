package com.geekutil.designpattern.interpreter;

import java.util.Map;

/**
 * @author Asens
 * create 2019-10-13 16:21
 **/

public class VarExpression implements Expression {
    private String name;

    public VarExpression(String name) {
        this.name = name;
    }

    @Override
    public int interpret(Map<String,Integer> map) {
        return map.get(name);
    }
}
