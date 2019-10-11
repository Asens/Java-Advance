---
description: 设计模式中关于行为的
---

# 设计模式-行为

策略模式、责任链模式、命令模式、中介者模式、模板方法模式、迭代器模式、访问者模式、观察者模式、解释器模式、备忘录模式、状态模式。

## 策略模式

**简介**

一个接口的多种实现，每个实现代表一个策略，可以动态替换

**场景**

重构if-else较常见

**代码**

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

