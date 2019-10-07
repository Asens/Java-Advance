---
description: 设计模式中创建对象的几种模式介绍
---

# 设计模式-创建对象

设计模式中创建对象的方式比较多,包括单例模式，简单工厂模式,工厂模式,抽象工厂模式,建造者模式,原型模式,以及之前提到的单例模式,这些设计模式都为创建对象而存在.

## 单例模式

常见的单例模式有2种形式,俗称饿汉式和懒汉式

- 一种是`lazy`，也就是懒汉式，或是延迟获取,就是需要的时候才去创建一个
- 另外一个也就是`eager`,加载类之后无论是不是需要这个实例,都要先创建出来

### 懒汉式

```
public class Earth {
    private static Earth earth;
    private Earth(){}
    public static Earth newInstance(){
        if(earth==null){
            earth=new Earth();
        }
        return earth;
    }
}
```

当加载这个类时，什么也不做，只有调用`newInstance()`时，才会真正创建实例

### 饿汉式

```
public class Earth {
    private static Earth earth = new Earth();
    private Earth(){}
    public static Earth newInstance(){
        return earth;
    }
}
```

加载这个类时,直接创建实例,当获取的时候直接返回

通常来说可能并不希望使用饿汉式的方式在程序启动的时候就占用大量的时间去初始化

一般使用懒汉式,然而懒汉式在某种情况下会出现一些问题,比如**多线程**

在多线程环境下,内部还没有进行初始化时,当多个线程同时调用时,是线程不安全的

```
private void start() {
	ExecutorService pool= Executors.newFixedThreadPool(8);
        for(int i=0;i<3;i++){
            pool.execute(new Runnable() {
                public void run() {
                    Earth earth=Earth.newInstance();
                    System.out.println("thread "+earth);
                }
            });
        }
        pool.shutdown();
}
```

输出

> thread cn.asens.patterndesign.single.one.Earth@609754e5
>
> thread cn.asens.patterndesign.single.one.Earth@63db9a8d
>
> thread cn.asens.patterndesign.single.one.Earth@63db9a8d

在多线程一个现在正在创建但是还没创建完成时,又有一个线程执行调用,earth没有被创建完成,依然为null,因此又被创建了一次,不在符合单例模式

### 加锁

线程不安全,最简单的方式就是加锁,于是

```
public class Earth {
    private static Earth earth;
    private Earth(){}
    public synchronized static Earth newInstance(){
        if(earth==null){
            earth=new Earth();
        }
        return earth;
    }
}
```

再用多线程测试已经没问题了,剩下的就是效率问题了

### 双重检查锁定(Double-Checked Locking)

```
public class Earth {
    private static Earth earth;
    private Earth(){}
    public static Earth newInstance(){
        if(earth==null){
            synchronized (Earth.class){
                if(earth==null){
                    earth=new Earth();
                }
            }
        }
        return earth;
    }
}
```

当对象已经创建,无需进入if块内,与第一种方法无异

在并发创建时线程1判断earth为null,获取锁,创建对象,与此同时线程2判断当前earth为null,进入if内,等待锁,当线程1创建对象完成,释放锁的时候,线程2获取锁,再次判断earth是不是为null,已经创建,不满足条件,直接返回,2个if判断均有意义但作用不同

但是会产生**因为重排序问题导致的线程获取了实例,但是实例没有初始化完成的问题**

>  **重排序问题**
>
> 通常来说这种方法已经完美了,但是仍然有问题,在某些JIT编译器会在对象初始化的时候进行重排序,就像count++不是原子操作一样,new Earth()也同样不是原子操作,创建一个对象分为3步:
>
> 1. 开辟内存地址,分配对象的内存空间
> 2. 初始化对象
> 3. 将对象放入对应的内存地址
>
> 重排序:重排序是指处理器或编译器为了优化性能而对指令序列进行重新排序的一种手段
>
> 试想,在程序生成的指令序列里,会存在可以重排序的如
>
> int a=1;
>
> int b=2;
>
> 重排序后,并不会对程序有任何影响,当然也会存在
>
> int a=1;
>
> int b=a+1;
>
> 这种不能重排序的指令,处理器或编译器有一套机制来判断指令能否重排序
>
> 在对象初始化的过程中,2和3恰好是能够重排序的,也就是说,在对象初始化的过程中会先把(对象的句柄|earth|未初始化的对象本身)放入对应的内存地址,然后再执行这个对象的初始化
>
> 也就是说,在线程1初始化的时候重排序,执行1,3时线程2进入锁外的if(earth==null)判断,此时earth有地址但是并没有初始化,线程2获得了一个未初始化的对象

为了解决这个问题需要禁止2,3重排序,只需要进行简单的修改

使用`volatile `修饰earth

在earth前加入volatile,volatile会禁止多线程环境下2和3的重排序

3的操作是 earth=memory; 本质是volatile写,当写一个volatile变量时,Java内存模型会把这个变量的从线程的本地内存刷新回共享内存,也就是说在volatile写之前编译器会生成一个StoreStore屏障,为了保证volatile写之前改动不被重排到volatile写后面

### 类初始化

类初始化时推荐的相对简单且稳定的获取单例且线程安全的方式

原理是JVM会在类的初始化阶段,在class加载之后,线程使用之前执行初始化,在这段时间会获取初始化锁,多个线程进入只有一个线程能够完成初始化,其他线程等待

```
public class Earth {
    private static class Inner{
        public static Earth earth=new Earth();
    }

    private Earth(){}

    public static Earth newInstance(){
        return Inner.earth;
    }
}
```

当线程1,2同时进入newInstance()方法,需要用到Inner类,触发Inner类的初始化

在类初始化的过程中,会执行静态字段的赋值,即earth的创建

线程1会在获得类初始化锁的情况下完成对earth对象的初始化,而这个过程是对线程2不可见的,直到类初始化完成,线程2获得类初始化锁,线程2才能访问到earth对象,无论在线程1中earth初始化的顺序是1,2,3还是1,3,2,对线程2都不会有任何影响

相对于直接的new一个对象,工厂方法增加了更多的功能性和可扩展性,工厂模式依次分为简单工厂模式,工厂模式和抽象工厂模式.

## 简单工厂模式

简单工厂模式如其名,也确实很简单,简单工厂模式通常将一个值传递给工厂来获取实例,这个值可以是字符串,数字,枚举或是class等,将这些值传递给工厂,通过判断这些值来返回对应的实例

最终的效果就像这样,先新建一个工厂,然后传递产品的名字,获取对应的产品

```java
public static void main(String[] args){
    SimpleFactory factory=new SimpleFactory();
    Phone phone=factory.createPhone("XiaoMi");
    System.out.println(phone.getName());
}
```

返回的产品同属一个接口Phone,Phone下面有2个实体类

```java
public interface Phone {
    String getName();
}

public class iPhone implements Phone{
    @Override
    public String getName() {
        return "iPhone";
    }
}

public class XiaoMi implements Phone{
    @Override
    public String getName() {
        return "XiaoMi";
    }
}
```

简单工厂接收参数,返回对应的实体类

```java
public class SimpleFactory {
    public Phone createPhone(String name){
        if(name.equals("XiaoMi")){
            return new XiaoMi();
        }
        if(name.equals("iPhone")){
            return new iPhone();
        }
        return null;
    }
}
```

## 工厂模式

在简单工厂模式中,所有的类生成职能全放入了一个类,不易扩展

于是出现了工厂模式,工厂模式将Factory抽象出来,每个工厂只有一个生成实例的方法,每个产品都会对应一个工厂的实体类

在最外层的调用中,先生成对应的工厂实体类,根据生成的产品不同来选择生成哪种工厂

```java
public static void main(String[] args){
    Factory xiaoMiFactory=new XiaoMiFactory();
    Phone phone1=xiaoMiFactory.createInstance();
    System.out.println(phone1.getName());

    Factory iPhoneFactory=new iPhoneFactory();
    Phone phone2=iPhoneFactory.createInstance();
    System.out.println(phone2.getName());
}
```

定义Factory接口以及对应的实体类

```java
public interface Factory {
    Phone createInstance();
}

public class iPhoneFactory implements Factory{
    @Override
    public Phone createInstance() {
        return new iPhone();
    }
}

public class XiaoMiFactory implements Factory{
    @Override
    public Phone createInstance() {
        return new XiaoMi();
    }
}
```

对应的产品接口保持不变

```java
public interface Phone {
    String getName();
}

public class iPhone implements Phone{
    @Override
    public String getName() {
        return "iPhone";
    }
}

public class XiaoMi implements Phone{

    @Override
    public String getName() {
        return "XiaoMi";
    }
}
```

核心的工厂类不再负责所有的产品的创建，而是将具体创建的工作交给实现类去做

## 抽象工厂模式

如果是工厂模式的工厂实体类是针对一个产品,那么抽象工厂模式的工厂则对应多个产品

最终的调用类如下,添加了Pad接口,在Factory接口中添加了createPad()方法,现在不光可以生成手机还能生成平板

```
public static void main(String[] args){
    Factory xiaoMiFactory=new XiaoMiFactory();
    Phone phone1=xiaoMiFactory.createInstance();
    Pad miPad=xiaoMiFactory.createPad();
    System.out.println(phone1.getName());
    System.out.println(miPad.getName());

    Factory iPhoneFactory=new iPhoneFactory();
    Phone phone2=iPhoneFactory.createInstance();
    Pad iPad=iPhoneFactory.createPad();
    System.out.println(phone2.getName());
    System.out.println(iPad.getName());
}
```

新增Pad接口

```java
public interface Pad {
    String getName();
}

public class iPad implements Pad{
    @Override
    public String getName() {
        return "iPad";
    }
}

public class MiPad implements Pad{
    @Override
    public String getName() {
        return "MiPad";
    }
}
```

修改Factory接口以及对应的实体类

```java
public interface Factory {
    Phone createInstance();

    Pad createPad();
}

public class iPhoneFactory implements Factory{
    @Override
    public Phone createInstance() {
        return new iPhone();
    }

    @Override
    public Pad createPad() {
        return new iPad();
    }
}

public class XiaoMiFactory implements Factory{
    @Override
    public Phone createInstance() {
        return new XiaoMi();
    }

    @Override
    public Pad createPad() {
        return new MiPad();
    }
}
```

包括上述的Phone接口

上述3种模式各自有其特点,随着需求架构的变化相互转化都是很正常的,最终目的都是为了解耦

## 建造者模式

Builder的使用场景是对于那些初始化复杂,需要大量或是必要或是不必要的参数的类的创建

Builder上是一个可读性更强的构建器

如果一个类有多个参数,需要在构建这个类的时候初始化,比如一个零件,需要长度,高度,重量,体积,价格,横截面积等等

正常情况下有2种方式

**构造函数**

```java
public Component(int length,int height,int weight,int capacity,double price,int area){
    this.length=length;
    ...
}
```

参数列表很长,要保证参数顺序还有对应的类型,可读性很差,但是如果只有这一个参数形式,也是可以接受的

如果初始价格需要以后再设置,就可能添加一个新的构造函数

```
public Component(int length,int height,int weight,int capacity,int area){
    this(length,height,weight,capacity,0d,area);
}
```

如果再减少一个参数又需要一个新的构造函数

虽然也能用,但是当参数变的更加复杂的时候,编写的难度和可读性都会变的很差

**Java Bean**

```
public Component(){}

Component component=new Component();
component.setHeight(12);
...
```

对于多个参数,可以先调用无参的构造函数,然后setProperty()

但是又涉及到2个问题,即状态统一问题以及参数是否可变的问题

- 在对象创建完成之后,参数设置完之前,是存在一段时间,这个对象在持续的改变状态的,在单线程的环境下没什么问题,但是多线程的环境下就会出现线程安全问题.
- 对于某些对象希望参数设置完毕就不能再改变,但是遗留的setProperty()方法有可能仍然被外界调用修改变量

**为了解决这些问题,可以使用Builder模式**

在组件Component 中有静态类Builder,builder的参数与Component 相同,更确切的说是有Component 需要初始化的参数

Builder采用链式的调用每一个set函数都会返回builder本身

使用

```java
public class Client {
    public static void main(String[] args){
        new Client().start();
    }

    private void start() {
        Component component=new Component.Builder()
                .setHeight(1)
                .setWeight(1)
                .setWidth(1)
                .setArea(1)
                .setCapacity(1)
                .build();
        component.setPrice(1d);
    }
}
```
结构
```java
public class Component {
    private final int height;
    private final int width;
    private final int weight;
    private final int capacity;
    private double price;
    private int area;

    public Component(Builder builder) {
        this.height = builder.height;
        this.width = builder.width;
        this.weight = builder.weight;
        this.capacity = builder.capacity;
        this.price = builder.price;
        this.area = builder.area;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public static class Builder{
        private int height;
        private int width;
        private int weight;
        private int capacity;
        private double price;
        private int area;

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setWeight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder setCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Builder setArea(int area) {
            this.area = area;
            return this;
        }
        public Component build(){
            return new Component(this);
        }
    }
}
```

## 原型模式

原型模式,它通过复制一个已经存在的实例来返回新的实例

原型模式多用于创建复杂的或者耗时的实例, 因为这种情况下,复制一个已经存在的实例可以使程序运行更高效

或是有时需要一个copy,与原对象值完全相等的对象但是不能使用原对象的引用

原型模式被复制的类需要实现Cloneable接口并重写Object的clone方法

Cloneable接口并不是一个优秀的设计,它缺少一个clone方法

Object的clone方法是受保护的,重写这个方法就必须实现Cloneable接口,否则会出现CloneNotSupportedException异常

clone可以分为浅复制和深复制

**浅复制**

```java
public class Man implements Cloneable{
    private String name;

    public Man(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Man clone(){
        try {
            return (Man)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

client

```
public class Client {
    public static void main(String[] args){
        new Client().start();
    }

    private void start() {
        Man man1=new Man("aa");
        Man man2=man1.clone();
        Man man3=man1.clone();
        System.out.println(man1);
        System.out.println(man2);
        System.out.println(man3);
        System.out.println(man1.getName());
        System.out.println(man2.getName());
    }
}
```

每次复制都会生成一个新的对象

新的对象保留与原对象相同的属性值

对于类的成员变量是基本类型的没有问题,但是原对象的成员变量有引用类型的时候,浅复制的对应的成员变量仍然是原有的引用,如果修改就会对原对象产生不可控的严重后果

对于类用有引用类型的变量需要使用深复制,也就是将对应的引用变量也调用对应的clone方法然后赋值给clone后的对象

**深复制**

Man需要有个Car

```
public class Car implements Cloneable{
    private String name;

    public Car(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Car clone(){
        try {
            return (Car)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

修改之后的Man,Car的clone先注释掉了

```
public class Man implements Cloneable{
    private String name;
    private Car car;

    public Man(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void changeCarName(String newName){
        car.setName(newName);
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Car getCar() {
        return car;
    }

    @Override
    public Man clone(){
        try {
           Man newMan=(Man)super.clone();
         //Car newCar=car.clone();
         //newMan.setCar(newCar);
           return newMan;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

man1的clone man2修改car的name会影响man1的car的name

```
private void start() {
    Man man1=new Man("aa");
    Car car=new Car("benz");
    man1.setCar(car);

    Man man2=man1.clone();
    man2.changeCarName("bmw");

    System.out.println(man1.getCar().getName());
    System.out.println(man2.getCar().getName());

}
```

输出:

bmw

bmw



去掉注释,不再受影响

输出:

benz

bmw