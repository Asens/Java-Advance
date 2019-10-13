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

```java
public interface Strategy {
    int operation(int one,int two);
}
```

实现一，加

```java
public class StrategyAdd implements Strategy {
    @Override
    public int operation(int one, int two) {
        return one + two;
    }
}
```

实现二，乘

```java
public class StrategyMultiply implements Strategy {
    @Override
    public int operation(int one, int two) {
        return one * two;
    }
}
```

在Context调用策略

```java
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

```java
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

```
3
2
```



## 责任链模式(Chain of Responsibility)

**简介**

链条型的结构，每个处理类是一个节点，依次处理

**场景**

需要对数据进行一系列的处理逻辑，典型的有servlet体系的filter拦截器依次对请求进行处理

**代码**

> [责任链模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/chain)

链条处理连接，节点处理逻辑

```java
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

```java
public interface Handler {
    void handel(String content,Chain chain);
}
```

实现一

```java
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

```java
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

```java
aone
aonetwo
aonetwothree
```



## 命令模式(Command)

**简介**

命令模式有三个角色，命令发布者，命令本身，命令执行者。 命令模式的本质是对命令进行封装，将发出命令的责任和执行命令的责任分割开 

**场景**

当调用者和执行者需要完全解耦时，将命令封装为请求

**代码**

> [ 命令模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/command)

命令接口，定义行为

```java
public interface Command {
    void execute();
}
```

实现类，内部包含接收者

```java
public class DownCommand implements Command {
    private Receiver receiver;

    public DownCommand(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        receiver.down();
    }
}
```

`UpCommand`略

接收者可以是任何类，处理最终的逻辑

```java
public class Client {
    public static void main(String[] args) {
        Receiver receiver = new Receiver();

        UpCommand upCommand = new UpCommand(receiver);
        DownCommand downCommand = new DownCommand(receiver);

        Invoker invoker = new Invoker();
        invoker.setCommand(upCommand);
        invoker.action();

        invoker.setCommand(downCommand);
        invoker.action();
    }
}
```

## 中介者模式(Mediator)

**简介**

很多对象需要互相交互，但是直接调用的话，会分散，会乱，会很耦合，因此引入中介者的机制，所有对象通过中介者进行交互。

中介者可以封装多个对象的多个行为，供外界统一调用

**场景**

比如市场是买家和卖家的中介者，

**代码**

> [中介者模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/mediator)

市场作为中介者，买家和卖家交流都需要经过市场

```java
public class Mediator {
    private Buyer buyer;
    private Seller seller;

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public void sendToSeller(String msg){
        seller.receiveMsg(msg);
    }

    public void sendToBuyer(String msg) {
        buyer.receiveMsg(msg);
    }

    public void sendToAll(String msg){
        seller.receiveMsg(msg);
        buyer.receiveMsg(msg);
    }
}
```

买家可以发送和接收消息

```java
public class Buyer{
    private Mediator mediator;

    public Buyer(Mediator mediator) {
        this.mediator = mediator;
    }

    public void sendToSeller(String msg){
        mediator.sendToSeller(msg);
    }

    public void receiveMsg(String msg){
        System.out.println("Buyer get message:"+msg);
    }
}
```

卖家一样

```java
public class Seller {
    private Mediator mediator;

    public Seller(Mediator mediator) {
        this.mediator = mediator;
    }

    public void sendToBuyer(String msg){
        mediator.sendToBuyer(msg);
    }

    public void receiveMsg(String msg){
        System.out.println("Seller get message:"+msg);
    }
}
```

在Client中调用,市场本身可以可以调用，发生全部消息，可以被市场管理者调用

```java
public class Client {
    public static void main(String[] args) {
        Mediator mediator = new Mediator();
        Buyer buyer = new Buyer(mediator);
        Seller seller = new Seller(mediator);
        mediator.setBuyer(buyer);
        mediator.setSeller(seller);

        seller.sendToBuyer("buy something?");
        buyer.sendToSeller("yes");

        mediator.sendToAll("market message");
    }
}
```

## 模板方法模式（TemplateMethod）

**简介**

当一个接口有多种实现，多种实现方式大同小异时，适合使用模板方法，各个实现继承抽象类，抽象类在公共方法中调用实现类方法

**场景**

应用场景广泛，对于实现差异不大的实现类时，可以有效合并逻辑，减少冗余

**代码**

> [模板方法模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/templatemethod)

工人和农民开心的一天

抽象人一天的生活

```java
public abstract class AbstractPerson {
    public void happyDay(){
        wakeUp();
        doSomething();
        sleep();
    }

    private void wakeUp(){
        System.out.println("wake up in the morning");
    }

    private void sleep(){
        System.out.println("sleep in the night");
    }

    protected abstract void doSomething();
}
```

工人，只需实现特定的方法，无需（但是可以）修改公共的方法

```java
public class Worker extends AbstractPerson{
    @Override
    protected void doSomething() {
        System.out.println("go to work");
    }
}
```

农民，同上

```java
public class Farmer extends AbstractPerson {
    @Override
    protected void doSomething() {
        System.out.println("go to farm");
    }
}
```

调用

```java
public class Client {
    public static void main(String[] args) {
        Farmer farmer = new Farmer();
        Worker worker = new Worker();
        farmer.happyDay();
        worker.happyDay();
    }
}
```

输出：

```java
wake up in the morning
go to farm
sleep in the night
wake up in the morning
go to work
sleep in the night
```



## 迭代器模式（Iterator）

**简介**

提供一种方法顺序访问一个集合对象中的各种元素

**场景**

JDK中的集合类使用了此模式用于遍历集合

**代码**

> [迭代器模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/iterator)

迭代器定义行为,判断是否有下一条数据，以及获取当前数据并指针后移一位

```java
public interface Iterator {
    boolean hasNext();
    Object next();
}
```

聚合（集合）接口，实现此接口者提供迭代器

```java
public interface Aggregate {
    Iterator createIterator();
}
```

集合的实现，实现了一个非常简单的只支持添加和遍历的定长列表

迭代器的实现时在列表内部的，用于方便的数据获取和判断

```java
public class NormalList implements Aggregate{
    private String[] arr = new String[10];
    private int size = 0;

    public void add(String data){
        if(size>9){
            throw new IllegalStateException("max length 10");
        }
        arr[size++] = data;
    }

    @Override
    public Iterator createIterator() {
        return new ConcreteIterator();
    }

    private class ConcreteIterator implements Iterator{
        private int current = 0;

        @Override
        public boolean hasNext() {
            return current<size;
        }

        @Override
        public Object next() {
            return arr[current++];
        }
    }
}

```

调用

```java
public class Client {
    public static void main(String[] args) {
        NormalList list = new NormalList();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");

        Iterator iterator = list.createIterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
```

输出

```java
a
b
c
d
```

## 访问者模式（Visitor）

 **简介**

封装某些作用于某种数据结构中各元素的操作 ，元素可能有多种实现，访问者模式添加对多种元素类型的不同访问模式很方便

**场景**

 对象结构比较稳定，但经常需要在此对象结构上定义新的操作。 

 要对一个对象结构中的对象进行很多不同的并且不相关的操作 。

**代码**

> [访问者模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/visitor)

在牧场，有人喂牛喂马，有人骑牛骑马，有人骑马挤牛奶

被访问对象Animal（Element）

```java
public interface Animal {
    void accept(Visitor visitor);
}
```

不同的Animal类型

```java
public class Cow implements Animal {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

public class Horse implements Animal {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
```

访问者，可以操作2种类型的元素

```java
public interface Visitor {
    void visit(Horse horse);
    void visit(Cow cow);
}
```

不同的实现可以对已有的元素添加新的操作

```java
public class FeedMan implements Visitor {
    @Override
    public void visit(Horse horse) {
        System.out.println("feed horse");
    }

    @Override
    public void visit(Cow cow) {
        System.out.println("feed cow");
    }
}

public class RideMan implements Visitor {
    @Override
    public void visit(Horse horse) {
        System.out.println("ride horse");
    }

    @Override
    public void visit(Cow cow) {
        System.out.println("ride cow");
    }
}
```

调用

```java
public class Client {
    public static void main(String[] args) {
        Cow cow = new Cow();
        Horse horse = new Horse();

        Visitor rideMan = new RideMan();
        rideMan.visit(cow);
        rideMan.visit(horse);

        Visitor feedMan = new FeedMan();
        feedMan.visit(cow);
        feedMan.visit(horse);
    }
}
```

输出

```java
ride cow
ride horse
feed cow
feed horse
```

## 观察者模式（ Observer ）

**简介**

观察者模式使用十分广泛，通过事件注册，当事件触发时，所有注册的组件都将被通知并执行相应的逻辑

**场景**

当需要事件需要批量触发某些组件的逻辑时，可以使用观察者模式

**代码**

> [观察者模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/observer)

注册中心，可以注册组件，触发组件

```java
public class Subject {
    private List<Observer> list = new ArrayList<>();

    void registerObserver(Observer observer){
        list.add(observer);
    }

    void notifyObservers(String event){
        list.forEach(observer -> observer.update(event));
    }
}
```

组件，当事件触发时执行逻辑

```java
public interface Observer {
    void update(String event);
}
```

组件实现

```java
public class ConcreteObserver implements Observer {
    private String name;

    public ConcreteObserver(String name) {
        this.name = name;
    }

    @Override
    public void update(String event) {
        System.out.println(name+" handle + "+event);
    }
}
```

调用

```java
public class Client {
    public static void main(String[] args) {
        Subject subject = new Subject();
        Observer observerOne = new ConcreteObserver("one");
        Observer observerTwo = new ConcreteObserver("two");
        subject.registerObserver(observerOne);
        subject.registerObserver(observerTwo);
        subject.notifyObservers("aa");
    }
}
```

输出

```java
one handle + aa
two handle + aa
```

## 解释器模式（Interpreter）

**简介**

解释器模式是一种按照规定语法进行解析的方案

给定一个语言, 定义它的文法的一种表示，并定义一个解释器，该解释器使用该表示来解释语言中的句子

**场景**

编程语言的解释器，特定语法的的处理方案

**代码**

> [解释器模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/interpreter)

表达式

```java
public interface Expression {
    int interpret(Map<String,Integer> map);
}
```

基础类型，返回数值

```java
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
```

解析加法

```java
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
```

调用，省略了字符串的解析

```java
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
```

## 备忘录模式（ Memento ）

**简介**

保存对象状态，在需要时恢复状态

**场景**

当有撤销操作时,比较适合

**代码**

> [备忘录模式代码参考](https://github.com/Asens/Java-Advance/tree/master/code/src/com/geekutil/designpattern/memento)

被存储内容携带者

```java
public class Originator {
    private String state;

    public Memento createMemento(){
        return new Memento(state);
    }

    public void restoreMemento(Memento m) {
        this.setState(m.getState());
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
```

备忘录保存着内容本身（state）

```java
public class Memento {
    private String state;

    public Memento(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
```

备忘录管理者

```java
public class Caretaker {
    private Memento memento;

    public Memento getMemento() {
        return memento;
    }

    public void setMemento(Memento memento) {
        this.memento = memento;
    }
}
```

调用，包含一次存档和回档操作

```java
public class Client {
    public static void main(String[] args) {
        Caretaker caretaker = new Caretaker();
        Originator originator = new Originator();
        originator.setState("0");
        //存档
        caretaker.setMemento(originator.createMemento());
        originator.setState("1");
        //回档
        originator.restoreMemento(caretaker.getMemento());
        System.out.println(originator.getState());
    }
}
```

## 状态模式（ State ）

**简介**

 类的行为是基于它的状态改变的 , 对象的行为依赖于它的状态（属性），并且可以根据它的状态改变而改变它的相关行为。 

**场景**

 代码中包含大量与对象状态有关的条件语句。 

**代码**

状态

```java
public interface State {
    void execute(Context context);
}
```

包含状态的类

```java
public class Context {
    private State state;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
```

不同状态

```java
public class StartState implements State {
    @Override
    public void execute(Context context) {
        System.out.println("start execute");
        context.setState(this);
    }
}

public class StopState implements State {
    @Override
    public void execute(Context context) {
        System.out.println("stop execute");
        context.setState(this);
    }
}
```

调用，改变状态，改变行为

```java
public class Client {
    public static void main(String[] args) {
        Context context = new Context();
        State start = new StartState();
        start.execute(context);

        State stop = new StopState();
        stop.execute(context);
    }
}
```

