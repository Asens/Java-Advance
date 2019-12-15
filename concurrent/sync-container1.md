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

入队操作，当队列满了的时候等待，不满的话执行入队操作。

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

