---
description: HashMap结构解析
---

# HashMap

HashMap是面试中常见的问题。

#### Q：底层的数据结构是什么

HashMap的底层数据结构由数组+链表组成，

数据根据key的hashCode的（hash函数）选择数据对应的位置,该位置是Map.Entry组成的链表，

当链表达到一定长度时，默认是8，会将链表转化为红黑树。

红黑树是一种平衡树，在查询时如果链表长度过长，能有效减少查询时间。

链表由Entry<K,V>组成，包含键值对以及next的关系以及hash等信息，红黑树的节点treenode维护了左右子节点的关系。

#### Q：HashMap会直接使用key的hashcode来确定在数组中的位置吗

HashMap会对key的hashcode再进行一次hash处理，

防止用户代码不太好的hashcode算法使hashcode集中进入同一个数组位置，会降低hashmap的查找效率

#### Q：你了解过红黑树吗，为什么会转化为红黑树

对于分布平均的hashcode，以及正常的加载因子，转化为红黑树的几率很低，

但是极端情况下，hashcode全部相等，HashMap会转化为链表，查找的时间复杂度会变为O(n),远大于HashMap本身的O(1)，

红黑树是平衡树，查找的效率的O(logn)，即使在极端的情况也能保证一定的效率。

红黑树是二叉查找树的优化，二叉查找树理论上也有变成链表的可能，

红黑树在特定的结构进行特定的翻转，使其终止能达到左右平衡。

#### Q：怎么理解构造函数中初始化容量和加载因子

HashMap根据初始化容量计算出数组的初始化长度，大于等于该数字的最小的2的幂次。加载因子为(size/数组长度)的临界值，当数组的内容超过这个值就需要扩容的

#### Q ：数组的长度为什么一定是2的幂

当容量一定是2^n时，h & (length - 1) == h % length，位运算效率非常高

#### Q：那HashMap是怎么扩容的

当达到条件也就是size/loadFactor>数组长度时，数组的长度乘2，

然后将原有的内容放到扩容后的数组中，比如16扩容至32，

方法是遍历数组，将数组中节点的hash与上扩容之前的长度,为0时在低16位，否则在高16位，做成新的链表并放入新数组的对应位置

(hash & (oldCap-1)) + (hash & oldCap) = (hash & (oldCap*2-1))

#### Q：get的过程是什么样的

根据key的hashcode再进行内部的hash函数，找到对应的数组中的位置，

如果没有节点，返回null，

如果有节点的话，比较第一个节点的key，==或是equals的话返回该节点的值，

如果不等的话判断是否是tree的节点，

是的话通过tree的方式去查找，否则按照链表的方式查找，比较key，直到==或是equals返回对应的值，

没有的话返回null

#### Q：put的过程是什么样的

根据key的hashcode再进行内部的hash函数，找到对应的数组中的位置，

如果没有节点的话，创建一个新的node，

如果有节点的话依次比较每个节点的key是否==或是equals要put的key，

如果存在的话，对应的节点替换成需要put的值，

如果不存在，在末尾添加对应的节点，

如果超过一定的数量，会将链表转化为红黑树。

#### Q：equals()和hashCode()一般怎么使用，在HashMap中的作用？

如果两个对象equals是相等的，那么hashCode一定也需要是相等的。

如果equals不相等，hashCode可以相等。

要做到不相等的对象hashCode尽量不相等，因为在散列时会更加均匀的分布，提交散列表（HashMap）的性能。

#### Q：出现并发问题的场景，怎么解决 

在HashMap进行put和remove操作时，modCount++，在HashMap遍历时会比较每次的modCount，如果改变了会抛出ConcurrentModificationException异常，这个可以起到一定的左右，但是没法保证线程安全。

为了保证HashMap的线程安全有几种方法：

HashTable，最简单的就是使用HashTable，同时性能也是最差的，HashTable中很多方法都是加锁的，多个线程在同一时间只能执行其中一个方法。

Collections的synchronizedMap方法，将map的封装为SynchronizedMap外部调用的函数全部加锁，类似HashTable

加锁，所有访问的方法封装并加锁

同样是加锁，可以使用读写锁，所有的读使用共享锁，可以并发访问，写操作独占锁，能够保证读写安全并提高一定的性能。（面试官基本会引申至读写锁，AQS，CAS等，保重）

ConcurrentHashMap，基本是HashMap并发的终极解决方案，在JDK1.7中采用了分段锁的方式来细粒度化锁来提高性能。在JDK1.8中不再使用分段锁的方式，利用CAS+Synchronized来保证并发更新的安全

#### Q：LinkedHashMap是怎么保持有序的

linkedHashMap继承了HashMap，并保证是按照一定顺序的，插入顺序或是访问顺序。

LinkedHashMap的node节点维护before和after属性，通过双向链表的方式维护数据的顺序

默认插入顺序，当节点数据或是结构变动时，也就是put或remove等操作，会把当前节点放到双向链表的末尾

accessOrder为true时为访问顺序，当发生节点访问的操作时把当前节点放到双向链表的末尾

重写了HashMap的迭代器，按照双向链表维护的顺序遍历