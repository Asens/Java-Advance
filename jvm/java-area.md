---
description: 运行时内存区域介绍
---

# Java中的内存区域

Java运行时内存区域和内存模型时2个概念，存在一定的联系，JMM是一种实现的规范和标准，实际的内存区域是JVM实际执行时在内存的占用空间。

内存区域分为程序计数器，堆，虚拟机栈，方法区，直接内存，本地方法栈

## 程序计数器

程序计数器记录着正在执行的字节码指令地址，并取下一条需要执行的字节码指令

每个线程都有一个程序计数器

## 虚拟机栈

线程私有，生命周期与线程相同

在方法执行时会创建栈帧，存放着：局部变量表，操作数栈，动态链接，方法出口

局部变量表存放着基本数据类型，对象引用和返回信息

- 当请求深度过大，一般是递归，会出现StackOverflowError
- 扩展时无法申请足够的内存时OutOfMemoryError

## 本地方法栈

与虚拟机栈类似，但是是属于Native方法的

同样会抛出StackOverflowError和OutOfMemoryError

## Java堆

虚拟机使用的最主要的内存区域。存放着对象实例和数组，各线程共享区域。

对于分代收集算法，可以分为

当堆无法扩展时抛出OutOfMemoryError

## 方法区

存放类信息，常量，静态常量，即时编译的代码，各线程共享区域。

注意在JDK8中已废弃永久代，改为元空间（Metaspace）

**运行时常量池**

方法区的一部分，用于保存类的字段，方法，接口等。以及常量池，保存字面量和符号引用

## 直接内存

在NIO中可以分配堆外内存，能够在某些场景提高性能

可能导致超过物理总内存的问题

扩展会出现OutOfMemoryError








