package com.geekutil.designpattern.chain;

/**
 * @author Asens
 * create 2019-10-11 23:09
 **/

public class HandlerThree implements Handler {
    @Override
    public void handel(String content, Chain chain) {
        content+="three";
        System.out.println(content);
        if(chain!=null){
            chain.handle(content);
        }
    }
}
