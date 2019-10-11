---
description: 设计模式中关于行为的
---

# 设计模式-行为

策略模式、责任链模式、命令模式、中介者模式、模板方法模式、迭代器模式、访问者模式、观察者模式、解释器模式、备忘录模式、状态模式。

## 策略模式(Strategy)

**简介**

一个接口的多种实现，每个实现代表一个策略，可以动态替换

**场景**

重构if-else较常见

**代码**

> [策略模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/strategy)

策略本身，定义操作行为

```
public interface Strategy {
    int operation(int one,int two);
}
```

实现一，加

```
public class StrategyAdd implements Strategy {
    @Override
    public int operation(int one, int two) {
        return one + two;
    }
}
```

实现二，乘

```
public class StrategyMultiply implements Strategy {
    @Override
    public int operation(int one, int two) {
        return one * two;
    }
}
```

在Context调用策略

```
public class Context {
    private Strategy strategy;

    public Context(Strategy strategy) {
        this.strategy = strategy;
    }

    public int operation(int one,int two){
        return strategy.operation(one,two);
    }
}
```

Client端使用

```
public class Client {
    public static void main(String[] args) {
        Context context = new Context(new StrategyAdd());
        System.out.println(context.operation(1,2));

        context = new Context(new StrategyMultiply());
        System.out.println(context.operation(1,2));
    }
}
```

输出

>3
>2

## 责任链模式(Chain of Responsibility)

**简介**

链条型的结构，每个处理类是一个节点，依次处理

**场景**

需要对数据进行一系列的处理逻辑，典型的有servlet体系的filter拦截器依次对请求进行处理

**代码**

> [责任链模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/chain)

链条处理连接，节点处理逻辑

```
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

    public void handle(String content){
        if(handler!=null){
            handler.handel(content,nextChain);
        }
    }
}
```

处理节点定义

```
public interface Handler {
    void handel(String content,Chain chain);
}
```

实现一

```
public class HandlerOne implements Handler {
    @Override
    public void handel(String content, Chain chain) {
        content+="one";
        System.out.println(content);
        if(chain!=null){
            chain.handle(content);
        }
    }
}
```

实现二，三略

调用，设置处理器数组，初始化链条和节点的结构

```
public class Client {
    public static void main(String[] args) {
        Handler[] handlers = {new HandlerOne(),new HandlerTwo(),new HandlerThree()};
        Chain first = null, tail = null;
        for (int i = 0; i < handlers.length - 1; i++) {
            if(i==0){
                tail = new Chain();
                tail.setHandler(handlers[i]);
                first = tail;
            }
            tail.setNextChain(new Chain(handlers[i+1]));
            tail = tail.getNextChain();
        }
        first.handle("a");
    }
}
```

输出,三个处理器依次处理

> aone
> aonetwo
> aonetwothree













