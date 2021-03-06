---
description: 对象的可达性分析和垃圾回收算法
---
# JVM垃圾回收

Java中，内存由虚拟机管理，控制着回收什么，什么时候回收，怎么回收。

在栈中内存的随线程产生和分配，销毁而回收，在堆中，需要制定一系列策略来判断该回收哪些区域，以及何时回收。

## 可达性分析

主流的做法是通过可达性分析来判断对象是否存活。存在一些根节点（GC Roots）作为起始点，从这些节点向下搜索，通过的路径为引用链，当对象与GC Roots间不存在任何引用链时，该对象不再被需要。

GC Root包括：

- 虚拟机栈中引用的对象
- 方法区中类静态属性引用的对象
- 方法区常量引用的对象
- Native方法引用的对象

**引用**

Java为引用提供了四种级别，除了强引用之外，还有其他引用，在垃圾回收时有着不同的表现

- 强引用：默认的引用方式，引用还在，对象就不会被回收
- 软引用（SoftReference）：垃圾回收后内存不够用会在软引用中再回收一次，还没有足够内存，则内存溢出
- 弱引用（WeekReference）：垃圾收集时，回收
- 虚引用：最弱的关系，当对象被收集时，可以获得通知

**标记**

在对象进行可达性分析后如果没有GC引用链，则被第一次标记

之后有线程调用`finalize()`方法，如果重新进入引用链，则不会回收，否则，被回收

## 垃圾回收算法

### 标记-清除算法

最基础的算法，标记就是上述过程，完成标记之后清除对应的内存空间，它的不足有2点：

- 效率：标记和清除效率都不高
- 空间：清楚之后会产生大量的空间碎片，在有大的对象时，可能会提前触发GC

### 复制算法

将内存分为2块，一块内存用完了，就把存活的对象移动到另一块，然后全部清除之前的一块。另一块内存用完之后，同理。

优点是不用考虑内存碎片，但是缺点就是内存只有原来的一半

### 标记-整理算法

和标记-清除类似，但是清除有所不同，将以存活对象移动到一端，然后对其他区域进行清除。

### 分代收集算法

把堆分为新生代和老年代，根据分代的特点使用不同的收集算法。

## 内存分配和回收策略

**新生代**

在新生代每次垃圾收集时都有大量的对象死去,少量的对象存活，比较适合复制算法。

但是并不是上面所说的1:1，通常采用的是1个Eden区，2个Survivor，默认8:1:1的比例，新的对象默认进入Eden区，当内存不足时，进行新生代GC，存活的对象进入1个Survivor区，完成之后新的对象继续分配进入Eden区，再次发生GC时，Eden区的和Survivor区的存活对象进入另一个Survivor区

当对象很大时，会直接进入老年代，包括分配阶段和回收阶段

当对象存活很多轮时，也会进入老年代 -XX:MaxTenuringThreshold=15

**动态年龄判断**

当低龄对象很多时，超过Survivor一半时，大于该年龄的将直接进入老年代，而不需要等到默认的15

**空间分配担保**

当发生Minor GC时，虚拟机会检查老年代的最大可用的连续空间是否大于新生代的所有对象的总空间。如果成立，Minor GC是安全的。

否则，虚拟机会查看HandlePromotionFailure是否允许担保失败。

如果允许，那么检查老年代的最大连续可用空间是否大于历次晋升老年代的平均大小。

如果大于，进行Minor GC。此时有可能这次存活的对象比较多，大于之前的平均水平，因此会担保失败，重新Full GC

如果小于或不允许HandlePromotionFailure，进行Full GC

**老年代**

老年代中对象存活率高，没有额外的空间担保，一般采用标记-清理或标记整理算法

**枚举根节点**

虚拟机需要分析全局性的引用和执行上下文，在GC停顿时，必须分析到某个暂停的时间点上。检查HotSpot中的OopMap。OopMap记录着对象的偏移量和引用的位置。

**安全点**

如果在每个改变引用关系的指令都生产OopMap，那么就占用大量的空间。因此只在特定的位置记录，称为安全点。

当开始GC时，设定一个标志，线程去轮询这个标志，当发现标志时中断挂起

**安全区域**

对于不在执行时的线程，比如sleep，无法响应中断，GC不可能等待这些线程

因此有了安全区域机制，对于不改变对象引用关系的区域，可以被称为安全区域

线程进入安全区域时，无需理会GC

当线程离开安全区域时，如果GC枚举根节点或全部GC过程已结束，则继续执行

如果没结束，需要等待GC可安全离开安全区域的信号

## 垃圾回收器

### **Serial收集器**

最早的垃圾收集器，单线程收集器，当工作时，需要暂停全部工作线程

简单而高效

### ParNew收集器

Serial收集器的多线程版本，GC时同样需要暂停全部线程

可以和老年代收集器CMS配合使用

### Parallel Scavenge收集器

新生代收集器，使用复制算法，对现场

与ParNew不同的是，其目标是达到可控制的吞吐量。

提供了2个参数可控制吞吐量

MaxGCPauseMillis：最大GC暂停时间，收集器将尽可能不超过该时间，并不一定是越小越好，GC时间的缩短是以牺牲吞吐量和新生代空间换取的

GCTimeRatio：垃圾收集时间占总时间的比率，默认99，即 1/(1+99) = 1%， 基于运行时的测量，JVM将会尝试修改堆和GC设置以期达到目标吞吐量。 

 Parallel Scavenge收集器提供 -XX:_UseAdaotiveSizePolicy来自适应的调节新生代的大小，以及新生代中各区的大小。

### Serial Old收集器

Serial收集器的老年代版本，单线程收集器，标记-整理算法

### Parallel Old收集器

Parallel Scavenge收集器的老年代版本，可以配合使用，吞吐量优先

### CMS收集器

以最短回收停顿时间为目标的收集器。希望停顿时间最短，提高相应速度。

CMS收集器的垃圾收集线程可以和工作线程同时工作

CMS基于**标记-清除**算法，分为4个步骤

- 初始标记：标记GC Roots关联对象，停止全部工作线程，速度很快
- 并发标记：GC Roots Tracing，和工作线程一起执行
- 重新标记：修改并发标记期间用户程序修改导致的标记变动的对象标记记录，停止全部工作线程，速度较快
- 并发清除：并发清理

**缺点**：

- 因为是和工作线程并发执行，会占用线程导致应用程序变慢
- 无法清除浮动垃圾（并发清理阶段工作线程产生的垃圾），因此并不能等到老年代完全满了再`Full GC`，而是选择一个阈值`-XX:CMSInitiatingOccupancyFraction`，启动`Full GC`，当预留的内存满了的时候，会出现 `Concurrent Mode failure` 错误，使用Serial Old收集器收集器收集，从而停顿
- 基于**标记-清除**算法，会产生碎片，提供`-XX:+UseCMSCompactAtFullCollection`参数用于开启碎片的合并整理过程。每次都碎片整理也会慢，因此提供`-XX:CMSFullGCsBeforeCompaction `,用于每过多少次进行一次碎片整理

### G1收集器

G1是最新的JDK上默认的垃圾回收器，技术更加先进。目标是替代CMS收集器。

之前的垃圾回收器的范围都是整个新生代或老年代，而G1不是。它将整个Java堆分为多个大小相等的独立区域，新生代和老年代都是一部分不需要连续的区域集合

G1可预测停顿，能够在执行时间内，垃圾回收时间不超过一定时间。G1有计划的避免整个Java堆中进行全区域的垃圾回收。G1会根据每个区域的垃圾回收价值的大小维护优先列表，优先回收价值最大（回收所获得的内存大小以及对应的时间的自适应值）的区域

G1的收集过程，和CMS类似：

- 初始标记：标记GC Roots关联对象，停止全部工作线程，速度很快
- 并发标记：GC Roots Tracing，和工作线程一起执行
- 最终标记：修改并发标记期间用户程序修改导致的标记变动的对象标记记录，停止全部工作线程，速度较快
- 筛选回收：对区域进行排序，然后回收，因为只回收一部分区域，因而是可控的