package com.geekutil.designpattern.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Asens
 * create 2019-10-13 16:09
 **/

public class Client {
    public static void main(String[] args) {
        Map<String,Integer> map = new HashMap<>();
        map.put("a",1);
        map.put("b",2);
        //a+b
        Expression one = new VarExpression("a");
        Expression two = new VarExpression("b");
        System.out.println(new AddExpression(one,two).interpret(map));
    }
}
