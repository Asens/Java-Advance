---
description: 同步容器ArrayBlockingQueue、ConcurrentLinkedQueue
---
# 同步容器ArrayBlockingQueue、ConcurrentLinkedQueue

JDK提供了一系列线程安全的容器，在多线程的环境中可以提供稳定的操作。这里说一下线程安全的队列。

队列可以分为2种。一种是普通的队列，一种是阻塞队列。

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

阻塞队列

```
BlockingQueue
#除了Queue中的方法外
#队列满时阻塞，直到入队为止
void put(E e) throws InterruptedException;
#队列空时阻塞，直到出队为止
E take() throws InterruptedException;
```

## ArrayBlockingQueue

### 入队

当队列满了的时候等待，不满的话执行入队操作。

```
public void put(E e) throws InterruptedException {
    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == items.length)
            notFull.await();
        enqueue(e);
    } finally {
        lock.unlock();
    }
}
```

其中等待的机制是使用的ReentrantLock的Condition。

```
在构造函数中初始化：
ReentrantLock lock = new ReentrantLock(fair);
Condition notEmpty = lock.newCondition();
Condition notFull =  lock.newCondition();
```

当队列已满，后续所有的线程调用put的话都会进入lock的等待队列。

因为ReentrantLock是基于AQS实现的，AQS内部维护的除了同步队列之外还有等待队列。

当队列已满执行await()时，根据代码可以大概看懂逻辑。

```java
//AbstractQueuedSynchronizer
public final void await() throws InterruptedException {
    //省略了一些中断和异常代码
    Node node = addConditionWaiter();
    int savedState = fullyRelease(node);
    int interruptMode = 0;
    while (!isOnSyncQueue(node)) {
        LockSupport.park(this);
        if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
}
```

- 生成一个节点并将其添加进入等待队列
- 完全释放该节点，释放线程持有的锁
- 判断该线程是否在同步队列，第一次执行是不在的，使用LockSupport阻塞该线程

也就是说**新的线程加入时会获取锁，当队列已满，该线程进入notFull的等待队列，释放锁，然后阻塞。**

当再有新的线程加入时，可以完美的再次执行同样的逻辑，进入等待队列进行等待。

如果队列没满或有其他出队操作，会唤醒阻塞的等待队列的节点，开始执行入队操作

```
private void enqueue(E x) {
    final Object[] items = this.items;
    items[putIndex] = x;
    if (++putIndex == items.length)
		putIndex = 0;
	count++;
	notEmpty.signal();
}
```

队列入队的常规操作，不再详述，执行完毕后会执行非空notEmpty等待队列的唤醒操作signal()，稍后再说。

### 出队

将首个节点移出队列，并返回。当队列为空时，等待。

```
public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == 0)
            notEmpty.await();
        return dequeue();
    } finally {
        lock.unlock();
    }
}
```

