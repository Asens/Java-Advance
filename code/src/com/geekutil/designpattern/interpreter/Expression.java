package com.geekutil.designpattern.interpreter;

import java.util.Map;

public interface Expression {
    int interpret(Map<String,Integer> map);
}
