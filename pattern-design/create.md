---
description: 设计模式中创建对象的几种模式介绍
---

# 设计模式-创建对象

设计模式中创建对象的方式比较多,包括简单工厂模式,工厂模式,抽象工厂模式,建造者模式,原型模式,以及之前提到的单例模式,这些设计模式都为创建对象而存在.

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