---
description: 设计模式-结构
---

# 设计模式-结构

本文介绍一些影响程序结构的设计模式，这些设计模式就像是精巧的零件，能够更加优雅的去实现或扩展特定场景下的功能.这些设计模式包括:适配器模式、装饰器模式、代理模式、外观模式、桥接模式、组合模式、享元模式。

## 适配器模式(Adapter)

**简介**

适配器是接口转换器，当一个类不能完全实现接口，存在一种模式，使类和接口可以配合工作，即适配器模式。

**场景**

一个已有的接口，两个方法，一个已有的类，实现了接口的一个方法，在不改变双方主要代码的情况下

**举例**

有一个接口

```java
public interface Ball {
    void come();
    void go();
}
```

一个类

```java
public class SpecialBall{
    public void come(){
        System.out.println("I can only come");
    }
}
```

满足场景中设定的条件

**方法一**

最常见的方式

在接口和实现类之间添加一个抽象类

```java
public abstract class BallAdapter implements Ball{
    @Override
    public void go() {
        throw new RuntimeException("go not support");
    }
}
```

然后使SpecialBall 继承此接口

```java
public class SpecialBall extends BallAdapter{
    public void come(){
        System.out.println("I can only come");
    }
}
```

在抽象类BallAdapter中实现了Ball的接口,并实现了SpecialBall所缺少的go方法,Ball接口的两个方法在BallAdapter和SpecialBall分别实现,BallAdapter中实现的go方法可根据自身系统设计留空或log或报错

**方法二**

类似，但不推荐

新增的适配器继承已有实现类

```java
public class BallAdapter extends SpecialBall implements Ball {
    @Override
    public void go() {
        throw new RuntimeException("go not support");
    }
}
```

调用的时候

```java
public static void main(String[] aa){
    Ball ball=new BallAdapter();
    ball.come();
}
```

**方法三**

将 `SpecialBall` 作为内部类

```java
public class BallAdapter implements Ball{
    private SpecialBall specialBall;

    public BallAdapter(SpecialBall specialBall) {
        this.specialBall = specialBall;
    }

    @Override
    public void come() {
        specialBall.come();
    }

    @Override
    public void go() {
        throw new RuntimeException("go not support");
    }
}
```

调用

```java
public static void main(String[] aa){
    SpecialBall specialBall=new SpecialBall();
    Ball ball=new BallAdapter(specialBall);
    ball.come();
}
```

## 装饰器模式(Decorator)

**简介**

一个类中方法的增强,可以在一个类的继承类中对已有类的方法前后加入代码,可以无限继承并添加代码

**场景**

当需要扩展方法的结构时,比如日志,计时等,或操作带扩展方法的参数或结果

**代码**

最初始的实现类

```java
public class ReaderOne implements Reader{
    @Override
    public void read() {
        System.out.println("original read");
    }
}
```

装饰器一:

实现Reader接口,设置实例变量,在构造方法或set设置被装饰的实体类,在read方法调用被装饰类的read方法

在这个方法前后可以加入自定义的方法

```java
public class ReaderTwo implements Reader{
    private Reader reader;

    public ReaderTwo(Reader reader) {
        this.reader = reader;
    }

    @Override
    public void read() {
        System.out.println(this.getClass()+" after read");
        reader.read();
        System.out.println(this.getClass()+" before read");
    }
}
```

然后在Client中调用

```java
public static void main(String[] aa){
    Reader reader=new ReaderOne();
    reader.read();
    System.out.println("------------------");
    ReaderTwo readerTwo=new ReaderTwo(reader);
    readerTwo.read();
}
```

再次嵌套

```java
public class ReaderThree implements Reader{
    private Reader reader;

    public ReaderThree(Reader reader) {
        this.reader = reader;
    }

    @Override
    public void read() {
        System.out.println(this.getClass()+" after read");
        reader.read();
        System.out.println(this.getClass()+" before read");
    }
}
```

在Client中也同样添加

```
public static void main(String[] aa){
    Reader reader=new ReaderOne();
    reader.read();
    System.out.println("------------------");
    ReaderTwo readerTwo=new ReaderTwo(reader);
    readerTwo.read();
    System.out.println("------------------");
    ReaderThree readerThree=new ReaderThree(readerTwo);
    readerThree.read();
}
```

`ReaderTwo`是对`ReaderOne`的扩展,`ReaderThree`又是对`ReaderTwo`的扩展

## 代理模式



