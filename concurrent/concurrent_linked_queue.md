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
        if (q == null) {
            if (p.casNext(null, newNode)) {
                if (p != t)
                    casTail(t, newNode);
                return true;
            }
        }
        else if (p == q)
            p = (t != (t = tail)) ? t : head;
        else
            p = (p != t && t != (t = tail)) ? t : q;
    }
}
```

根据名字可以看到ConcurrentLinkQueue会采用CAS的方式设置下一个节点以及tail节点。



