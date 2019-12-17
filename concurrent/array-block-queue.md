---
description: 同步容器ArrayBlockingQueue
---
# 同步容器ArrayBlockingQueue

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

将首个节点移出队列，并返回。当队列为空时，等待。如果队列有元素，执行出队操作。

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

我们假设目前队列已满，有线程正在等待入队。

当执行出队操作时

```
private E dequeue() {
    final Object[] items = this.items;
    E x = (E) items[takeIndex];
    items[takeIndex] = null;
    if (++takeIndex == items.length)
        takeIndex = 0;
    count--;
    if (itrs != null)
        itrs.elementDequeued();
    notFull.signal();
    return x;
}
```

除了常规的出队操作，需要需要关注出队后的唤醒和解锁。

- notFull.signal()
- lock.unlock()

阻塞队列的语义就是当队列已满，入队的线程等待。当队列有元素出队时，入队的线程被唤醒，然后继续执行。notFull.signal()和lock.unlock()共同完成了等待线程的唤醒。

notFull.signal()会执行AQS中的doSignal(Node first)

```
//AbstractQueuedSynchronizer
private void doSignal(Node first) {
    do {
        if ( (firstWaiter = first.nextWaiter) == null)
            lastWaiter = null;
        first.nextWaiter = null;
    } while (!transferForSignal(first) &&
             (first = firstWaiter) != null);
}
```

除了等待队列的一些头尾节点的设置，signal()的主要操作是把节点（等待入队的线程节点）从等待队列移动到同步队列

```
final boolean transferForSignal(Node node) {
    if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
        return false;

    Node p = enq(node);
    int ws = p.waitStatus;
    if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
        LockSupport.unpark(node.thread);
    return true;
}
```

> 可以理解为Lock下的每个Condition对象，对应一个等待队列，同步队列是等待锁的队列，一个锁只有一个同步队列，可以有多个等待队列。
>
> 同步队列和等待队列都复用AQS内部的Node作为队列的节点，使用不同的指针，同步队列使用prev和next作为前后节点，而等待队列是nextWaiter。
>
> 两种节点的初始化状态都是0，共享节点的会被设置为CONDITION（-2），同步队列为SIGNAL（-1）

因此signal()完成的是设置节点的状态，先从CONDITION设置0，入队，然后设置为SIGNAL（-1），阻塞。外部的while循环会不断的重试直到设置成功为止。

目前等待入队的节点从等待队列被移动到同步队列，然后被阻塞了。

下一部就是lock.unlock()，唤醒同步队列中的阻塞节点了，在[AQS详解](https://github.com/Asens/Java-Advance/blob/master/concurrent/aqs.md)中已经说的足够多了。等待入队的线程被唤醒就可以执行入队操作了。

