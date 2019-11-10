---
description: 调优的策略和工具
---

# JVM调优策略和工具

在生产环境中经常会遇到一些问题，比如CPU过高、内存溢出、内存泄漏、内存占用过高、进程莫名消失等。遇到这些问题时需要相应的工具检测对应的问题。

JDK提供了很多负责监控和故障处理的工具。

## JDK内置工具

### **jps**

虚拟机进程查看工具。可以查看本机虚拟机进程

```
jps -l
24234 test.jar
1084 sun.tools.jps.Jps
```

作用基本等同于ps，使用`ps -ef | grep java`可以达到类似的效果

### **jstat**

内存使用情况和GC的查看和监控工具，使用减少

比如

```
jstat -gc 24234 500 2
 S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT   
91136.0 93696.0  0.0   41534.6 1909760.0 1505772.9 4194304.0  3969043.0  120140.0 114168.4 13952.0 13011.6   9772  346.931  46     41.729  388.660
91136.0 93696.0  0.0   41534.6 1909760.0 1505772.9 4194304.0  3969043.0  120140.0 114168.4 13952.0 13011.6   9772  346.931  46     41.729  388.660

```

使用-gc参数查看Java堆的状况，每500ms执行一次，执行2次

jstat还提供了其他参数，可以查看新生代或是老年代，或是百分比等数据。

### **jinfo**

jinfo可以产看进程的系统环境变量，JVM参数设置和启动参数设置

直接使用`jinfo 进程号`即可获得最全的信息

```
jinfo 24234
Attaching to process ID 24234, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 25.171-b11
Java System Properties:

java.runtime.name = Java(TM) SE Runtime Environment
java.vm.version = 25.171-b11
...

VM Flags:
Non-default VM flags: -XX:CICompilerCount=4 -XX:InitialHeapSize=6442450944 -XX:MaxHeapSize=6442450944 -XX:MaxNewSize=2147483648 -XX:MinHeapDeltaBytes=524288 -XX:NewSize=2147483648 -XX:OldSize=4294967296 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC 
Command line:  -Xms6g -Xmx6g
```

### **jmap**

用于生产堆转储快照，当发生内存泄露或内存过高，或在内存占用过高时想要了解当前进程的内存分布都可以使用jmap。是非常有用的工具，使用分析转储快照的二进制文件的分析工具可以看到各个类的个数，占用内存大小等，可以对进程运行时的内存占用做出判断。

```
jmap -dump:format=b,file=/home/heap.bin 24234
Dumping heap to /home/heap.bin ...
Heap dump file created
```

导出的是二进制的文件，需要解析工具对其进行解析，处理jdk自带的jhat，还可以使用Eclipse Memory Analyzer、IBM HeapAnalyzer 、VisualVM

### **jhat**

jdk内置的堆转储文件分析工具，使用

```
jhat /home/heap.bin
```

可以开始分析堆转储文件，并开启一个服务器，可以使用浏览器访问`http://localhost:7000`来查看

除非特别情况，一般把转储文件复制出来用更好的工具进行分析

### **jstack**

生成当前时刻虚拟机线程快照。当程序死锁、死循环或长时间等待外部资源时，使用线程快照可以定位程序当前执行的位置。

```
jstack 24234
```

## JVM调优策略

JVM调优的基础原则：

- 每次Minor GC应尽可能多的收集垃圾对象，以减少Full GC的频率。因为Full GC的停顿时间是最长的。
- 处理吞吐量和延迟时，Java堆空间越大，垃圾收集效果越好。
- 吞吐量、延迟（因GC导致的停顿）、内存占用选择2个作为调优方向。
- JVM的使用的总内存大小**一定不能**超过总物理内存，否则进程会被系统直接关闭。

### 确定内存占用

计算活跃数据大小，活跃数据是程序运行到稳定态时程序进行Full GC后老年代占用的堆大小以及永久代占用的堆大小。

活跃数据的值可作为一个基数，指导其他参数的设置。

- 将Java堆的初始值-Xms和最大值-Xmx设置为老年代活跃数据大小的3-4倍
- 永久代（方法区）设置为永久代活跃数据大小的1.5倍
- 新生代设置为活跃数据的1-1.5倍，老年代设置为活跃数据大小的2-3倍

### 调优延迟

优化Java堆大小的配置，评估GC的时间和频率，判断是否切换垃圾收集器以及参数调优

首先确定可接受的平均停滞时间、Minor GC频率，Full GC的最大停顿时间、Full GC的频率。

**新生代**

对于新生代，空间越小，持续的时间越短，频率越高。

新生代只是为Java堆大小的10%

比较程序的延迟性要求和当前新生代大小参数的数据进行对比，增大或是减小新生代空间的大小。

**老年代**

收集老年代垃圾收集的频率和停顿时间，调整老年代大小，如果无法达到目标可以切换为CMS收集器。

