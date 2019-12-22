---
description: ConcurrentLinkQueue详解
---
# 同步容器ConcurrentLinkQueue

JDK提供了一系列线程安全的容器，在多线程的环境中可以提供稳定的操作。这里说一下线程安全的队列中的ConcurrentLinkQueue。

对于普通的队列Queue提供以下基本操作。

```
Queue
#入队成功返回true，否则抛异常
boolean add(E e);
#入队成功返回true，失败返回false
boolean offer(E e);
#出队，队列空时抛异常
E remove();
#出队，空时返回null
E poll();
#返回头节点，但是不出队，空时抛异常
E element();
#返回头节点，但是不出队，空时返回null
E peek();
```

这里我们主要分析一下入队和出队操作，探索一下ConcurrentLinkQueue是怎么高效的保证线程安全的。

ConcurrentLinkQueue首先是个链表。在链表的基础上实现了队列的功能。

## 入队

首先先看一下入队的代码。

```
public boolean offer(E e) {
    checkNotNull(e);
    final Node<E> newNode = new Node<E>(e);
    for (Node<E> t = tail, p = t;;) {
        Node<E> q = p.next;
        //(1)
        if (q == null) { 
            if (p.casNext(null, newNode)) {
                if (p != t)
                    casTail(t, newNode);
                return true;
            }
        }
        //(2)
        else if (p == q)
            p = (t != (t = tail)) ? t : head;
        //(3)
        else
            p = (p != t && t != (t = tail)) ? t : q;
    }
}
```

根据名字可以看到ConcurrentLinkQueue会采用CAS的方式设置下一个节点以及tail节点。

初始状态，head和tail同时指向一个节点，没有内容

```
public ConcurrentLinkedQueue() {
	head = tail = new Node<E>(null);
}
```

![初始化状态](.gitbook/assets/image-20191222091001473.png)

### 单线程入队

我们先讨论单线程的情况下ConcurrentLinkQueue的基础入队情况

**第一次入队**

- 会进入条件（1）
- 单线程无线程竞争，p cas设置next会设置成功
- p==t，不设置tail，返回true

![image-20191222091820498](.gitbook/assets/image-20191222091820498.png)

然后队列就变成了如下状态

![image-20191222093913927](.gitbook/assets/image-20191222093913927.png)

**第二次入队**

![image-20191222094233797](.gitbook/assets/image-20191222094233797.png)

- q!=null ，q!=p 进入条件（3）
- p==t，所以p=q

![image-20191222094727384](.gitbook/assets/image-20191222094727384.png)

进入下一次循环，q指向p的next节点

![image-20191222095802948](.gitbook/assets/image-20191222095802948.png)

- q==null，进入条件1
- cas设置next节点
- p!=t，设置tail节点

![image-20191222100112276](.gitbook/assets/image-20191222100112276.png)

第三次入队就和第一次入队一样，也就是，奇数次的入队和偶数次的入队逻辑保持一致。

- 奇数次入队时，tail并不是最后一个节点，是倒数第二个节点
- 偶数次入队时，tail是最后一个节点

### 多线程入队









