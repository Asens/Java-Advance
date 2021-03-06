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

``` java
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

**简介**

一个类对另一个类方法的扩展或增强

**场景**

和装饰器模式类似，AOP的典型使用场景，日志，计时，事务等。

### 静态代理

静态代理简单易用，但是必须和代理类实现相同接口

```java
public interface Person {
    void say();
}
```

默认实现类

```java
public class PersonImpl implements Person{
    public void say() {
        System.out.println("default person impl");
    }
}
```

 静态代理类,实现Person接口,传入一个Person实现类,在接口的实现方法中调用被代理类的say()方法,同时在前后加入自定义代码

```java
public class PersonProxy implements Person{
    private Person person;

    public PersonProxy(Person person) {
        this.person = person;
    }

    public void say() {
        System.out.println("before");
        person.say();
        System.out.println("after");
    }
}
```

### 动态代理

使用动态代理，代理类可以代理任何符合条件的类，不必和被代理类实现相同接口

动态代理的实现方法有2种,分别是JDK的动态代理和CGlib动态代理

#### JDK动态代理

JDK提供了动态代理的方案，代理类需要实现InvocationHandler，在`invoke`方法中对内代理方法进行增强

被代理类必须实现接口

```java
public class DynamicProxy implements InvocationHandler{
   private Object target;

   public DynamicProxy(Object target) {
       this.target = target;
   }

    public Object invoke(Object proxy, Method method,
                         Object[] args) throws Throwable {
        System.out.println("after");
        Object result=method.invoke(target,args);
        System.out.println("before");
        return result;
   }
   
   public <T> T getProxy(){
        return (T)Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            this);
    }
}
```

新建PersonImpl,DynamicProxy的实例然后获取PersonImpl的代理,调用对应的方法

```java
public static void main(String[] aa){
   DynamicProxy proxy=new DynamicProxy(new PersonImpl());
   Person personProxy=proxy.getProxy();
   personProxy.say();
}
```

#### CGLIB代理

通过字节码(ASM)为一个类创建子类,并在子类中采用方法拦截的技术拦截所有父类方法的调用

直接使用Person实体类,不再实现接口

```java
public class Person {
    public void say(){
        System.out.println("say");
    }
}
```

CGlibProxy实现MethodInterceptor接口,需要导入对应的jar包,maven:

```xml
<dependency>
    <groupId>cglib</groupId>
    <artifactId>cglib</artifactId>
    <version>3.2.5</version>
</dependency>
```

 与intercept和JDK的invoke方法类似,调用并前后加强

```java
public class CGlibProxy implements MethodInterceptor {
    public <T> T getProxy(Class<T> clz){
        return (T)Enhancer.create(clz,this);
    }

    public Object intercept(Object o, Method method, Object[] objects, 
                            MethodProxy methodProxy) throws Throwable {
        System.out.println("cglib before");
        Object result=methodProxy.invokeSuper(o,objects);
        System.out.println("cglib after");
        return result;
    }
}
```

新建CGlibProxy 实例,生成对应的代理类并调用

```java
public static void main(String[] aa){
    CGlibProxy proxy=new CGlibProxy();
    Person person=proxy.getProxy(Person.class);
    person.say();
}
```

#### 二者区别

区别：

- JDK动态代理代理的类，必须实现接口，CGLIB对指定的目标类生成一个子类，类不能时final
- JDK动态代理基于拦截反射机制，CGLIB基于字节码生成
- JDK8时，JDK动态代理效率高，Spring中实现接口，默认采用JDK动态代理，没有则用CGLIB

## 外观模式（Facade）

**简介**

为一系列动作提供一个统一的接口，方便外部去调用

**场景**

定义一个入口，可以执行复杂的逻辑。比如处理一个数据，内部可能需要读取文件，筛选，排序，最终获得结果，在外观模式就提供一个入口，调用直接获取结果

**代码**

定义一个洗衣机，可以执行3个动作

```java
public class OldWasher{
    public void addWater() {
        System.out.println("old addWater");
    }
    public void wash() {
        System.out.println("old wash");
    }
    public void dry() {
        System.out.println("old dry");
    }
}
```

当我们实际使用时，需要分别调用

但是当提供一个外观模式，就可以变成全自动洗衣机

```java
public class AutoWasher{
    public void start() {
        addWater();
        wash();
        dry();
    }

    private void addWater() {
        System.out.println("new addWater");
    }

    private void wash() {
        System.out.println("new wash");
    }

    private void dry() {
        System.out.println("new dry");
    }
}
```

只需调用start，就可以获得和之前相同的效果，方便易用

```java
public static void main(String[] args){
    OldWasher oldWasher=new OldWasher();
    oldWasher.addWater();
    oldWasher.wash();
    oldWasher.dry();

    AutoWasher autoWasher=new AutoWasher();
    autoWasher.start();
}
```

## 桥接模式（Bridge）

**简介**

两种可变实现类的组合，它们的抽象就是连接的桥梁

**场景**

调用者和被调用者都有不同的实现的时候，比如不同的人可以使用不同的笔写字

**代码**

 人的抽象

```java
public abstract class AbstractPerson {
    protected Pen pen;
    public AbstractPerson(Pen pen){
        this.pen=pen;
    }
    public abstract void write();
}
```

笔的抽象

```java
public abstract class Pen {
    public abstract void write();
}
```

笔的实现一

```java
public class PenOne extends Pen{
    public void write() {
        System.out.println("pen one write");
    }
}
```

笔的实现二

```java
public class PenTwo extends Pen{
    public void write() {
        System.out.println("pen two write");
    }
}
```

人的实现一

```java
public class PersonOne extends AbstractPerson{
    public PersonOne(Pen pen) {
        super(pen);
    }

    public void write() {
        pen.write();
    }
}
```

然后调用

```java
public class Client {
    public static void main(String[] args){
        Pen pen=new PenOne();
        AbstractPerson person=new PersonOne(pen);
        person.write();

        Pen penTwo=new PenTwo();
        person=new PersonOne(penTwo);
        person.write();
    }
}
```

## 组合模式(Composite)

**介绍**

组合模式是将对象以树型的结构表示出来，来表达对象的部分和整体的关系

**场景**

在树形结构,像分类-子分类,国家-地区等存在多级父子关系的都可以用到组合模式

这个模式也是Java的二叉树的实现方式

**代码**

```java
public class TreeNode {
    private TreeNode parent;
    private String name;
    private List<TreeNode> list=new ArrayList<>();

    public TreeNode(String name) {
        this.name=name;
    }

    public void add(TreeNode node){
        list.add(node);
        node.setParent(this);
    }

    public List<TreeNode> children(){
        return list;
    }

    private void setParent(TreeNode parent){
        this.parent=parent;
    }

    public void display() {
        System.out.println();
        TreeNode p=parent;
        while(p!=null){
            p=p.parent;
            System.out.print("--");
        }
        System.out.print(name);
        list.forEach(TreeNode::display);
    }
}
```

在main中设置其关系,并调用,在display中判断这个节点到root节点的中间节点的个数,显示对应层数,顺便递归遍历所有节点并显示输出

```java
public class Client {
    public static void main(String[] args){
        TreeNode root=new TreeNode("中国");

        TreeNode node1=new TreeNode("北京");
        TreeNode node2=new TreeNode("河北");
        root.add(node1);
        root.add(node2);

        TreeNode node21=new TreeNode("秦皇岛");
        node2.add(node21);

        root.display();
    }
}
```

> 输出
> 中国
> --北京
> --河北
> ----秦皇岛

## 享元模式(FlyWeight)

**简介**

享元模式是为了降低重复创建对象开销的一种设计模式,即对象池

**场景**

大量创建复杂的对象，可以缓存起来

**代码**

假设我们需要造车,但是创建一辆车的时间有点长,车的以name作为唯一标识:

```java
public class Car {
    private String name;
    public Car(String name){
        this.name=name;
        //假设创建时间有点长
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
}
```

造车的工厂模式,当我们创建过这辆车,直接从内存中把这辆车拿出来,否则就新创建一辆并把它放入内存

```java
public class CarFactory {
    private static Map<String,Car> map=new HashMap<String, Car>();

    public static Car createCar(String name){
        if(map.containsKey(name)){
            return map.get(name);
        }

        Car car=new Car(name);
        map.put(name,car);
        return car;
    }
}
```

 在main方法中调用对应的创建车辆,car3获取的是car1,car1和car2开始创建时需要调用构造函数,而再需要car1时只需要从内存中拿出来即可,典型的空间换时间的思想

```java
public static void main(String[] arhge){
    Car car1=CarFactory.createCar("car1");
    System.out.println(car1.getName()+" "+System.currentTimeMillis());
    Car car2=CarFactory.createCar("car2");
    System.out.println(car2.getName()+" "+System.currentTimeMillis());
    Car car3=CarFactory.createCar("car1");
    System.out.println(car3.getName()+" "+System.currentTimeMillis());
}
```