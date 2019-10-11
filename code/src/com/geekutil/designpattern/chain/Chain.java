package com.geekutil.designpattern.chain;

public class Chain {
    private Chain nextChain;
    private Handler handler;

    public Chain() {}

    public Chain(Handler handler) {
        this.handler = handler;
    }

    public void setNextChain(Chain nextChain) {
        this.nextChain = nextChain;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Chain getNextChain() {
        return nextChain;
    }

    public void handle(String content){
        if(handler!=null){
            handler.handel(content,nextChain);
        }
    }
}
