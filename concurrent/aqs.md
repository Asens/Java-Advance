---
description:  AQS详解
---
# AQS详解

AQS全称为AbstractQueuedSynchronizer，可以叫做队列同步器。

为线程的同步和等待等操作提供一个基础模板类。尽可能多的实现可重入锁，读写锁同步器所有需要的功能。队列同步器内部实现了线程的等待队列，独占或是共享的获取方式等，使其只需要少量的代码便可以实现目标功能。

一般来说，AQS的子类应以其他类的内部类的形式存在，然后使用代理模式调用子类和AQS本身的方法实现线程的同步。

也就是说，使用`ReentrantLock`举例，外界调用`ReentrantLock`，`ReentrantLock`内部定义`Sync`，`Sync`是AQS的子类，在`ReentrantLock`的内部实现中调用`Sync`的方法，最后完成最终的功能，当然`ReentrantLock`内部稍复杂，又加入和公平锁和非公平锁。

AQS内部有一个核心状态为`state`。所有通过AQS实现功能的类都是通过修改state的状态来操作线程的同步状态。比如在`ReentrantLock`中，一个锁中只有一个`state`状态，当`state`为0时，代表所有线程没有获取锁，当`state`为1时，代表有线程获取到了锁。通过是否能把`state`从0设置成1，当然，设置的方式是使用CAS设置，代表一个线程是否获取锁成功。

AQS提供了操作state的方法

```
int getState()
void setState(int newState)
boolean compareAndSetState(int expect, int update)
```

AQS内部维护一个线程的队列。队列由内部的节点组成。

队列的节点为`Node`,节点分为`SHARED`和`EXCLUSIVE`分别时共享模式的节点和独占模式的节点。

节点的等待状态为`waitStatus`

- CANCELLED（1）：取消状态，当线程不再希望获取锁时，设置为取消状态
- SIGNAL（-1）：当前节点的后继者处于等待状态，当前节点的线程如果释放或取消了同步状态，通知后继节点
- CONDITION（-2）：等待队列的等待状态，当调用signal()时，进入同步队列
- PROPAGATE（-3）：共享模式，同步状态的获取的可传播状态
- 0：初始状态

同样需要使用CAS的方式进行设置。

下面通过`ReentrantLock`和`ReentrantReadWriteLock`来解析AQS的独占模式和共享模式。

## 独占模式

`ReentrantLock`和`synchronized`功能类似，zhiyou 使用AQS的独占模式，只有一个线程可以获取锁。

AQS为独占模式提供了如下方法

```
void acquire(int arg)
boolean release(int arg)
```

`ReentrantLock`的最基本的使用方式如下

```
class X {
   private final ReentrantLock lock = new ReentrantLock();
 
   public void m() {
     lock.lock();
     try {
       doSomething();
     } finally {
       lock.unlock()
     }
   }
}
```

当创建`ReentrantLock`时默认使用非公平锁，效率高于公平锁，暂不讨论公平锁。

### 获取锁

当执行`lock()`时,或进行一次简短的获取锁操作

```
final void lock() {
	if (compareAndSetState(0, 1))
		setExclusiveOwnerThread(Thread.currentThread());
	else
		acquire(1);
}
```

其中`compareAndSetState(0, 1)`如果返回true就代表着之前`state`是0，也就是当前无线程获取锁，同时当前线程获取锁成功了，将独占线程设置为当前线程。

如果是false就代表当前有线程占用，当前占用的线程有2个可能

- 当前线程在占用，因为是可重入锁，之后同样会获取锁
- 其他线程在占用，在其他线程占用期间，当前线程需要等待

 进入`acquire(1)`

```
public final void acquire(int arg) {
	if (!tryAcquire(arg) &&
		acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
		selfInterrupt();
}
```

`acquire(1)`包含整个获取锁，如果获取不到就等待的操作，依次执行

```
tryAcquire(arg)
addWaiter(Node.EXCLUSIVE), arg)
acquireQueued(final Node node, int arg)
```

在`tryAcquire(arg)`中是尝试获取锁,是由`ReentrantLock`提供的,逻辑比较简单

- 当前无线程占有锁时,即`state`为0时,获取锁
- 当前有线程占有锁,但当前占有锁的线程是当前线程时,因为`ReentrantLock`是可重入锁,获取锁,并把`state`+1

如果`tryAcquire(arg)`能够成功获取锁,返回true,if条件为false,执行完成

当执行失败时,也就是获取不到锁时,说明有其他线程目前正在占用锁,将当前线程包装成节点放入同步队列

```java
private Node addWaiter(Node mode) {
	Node node = new Node(Thread.currentThread(), mode);
	//快速入队
	Node pred = tail;
	if (pred != null) {
		node.prev = pred;
		if (compareAndSetTail(pred, node)) {
			pred.next = node;
			return node;
		}
	}
	enq(node);
	return node;
}
```

先尝试快速入队，如果入队成功直接返回，如果失败（存在竞态）就使用cas反复入队直到成功为止

入队完成之后再判断一次当前是否有可能获得锁，也就是前一个节点是head的话，前一个线程有可能已经释放了，再获取一次，如果获取成功，设置当前节点为头节点，整个获取过程完成。

```
final boolean acquireQueued(final Node node, int arg) {
	boolean failed = true;
	try {
		boolean interrupted = false;
		for (;;) {
			final Node p = node.predecessor();
			if (p == head && tryAcquire(arg)) {
				setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
            }
    } finally {
         if (failed)
             cancelAcquire(node);
    }
}
```

获取失败的话先将之前的节点等待状态设置为SIGNAL，如果之前的节点取消了就向前一直找

```
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
	int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
		return true;
    if (ws > 0) {
		do {
			node.prev = pred = pred.prev;
		} while (pred.waitStatus > 0);
		pred.next = node;
    } else {
		compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

直到前一个节点不是取消状态，将其之前的节点等待状态设置为SIGNAL，因为再外面是无限循环的，设置SIGNAL成功后，之后就返回true了。

然后一直等待直到被唤醒

```
private final boolean parkAndCheckInterrupt() {
	LockSupport.park(this);
	return Thread.interrupted();
}
```

上面就是获取锁并等待的过程，总结起来就是：

当`lock()`执行的时候：

- 先快速获取锁，当前没有线程执行的时候直接获取锁
- 尝试获取锁，当没有线程执行或是当前线程占用锁，可以直接获取锁
- 将当前线程包装为node放入同步队列，设置为尾节点
- 前一个节点如果为头节点，再次尝试获取一次锁
- 将前一个有效节点设置为SIGNAL
- 然后阻塞直到被唤醒

### 释放锁

当ReentrantLock进行释放锁操作时，调用的是AQS的`release(1)`操作

```
public final boolean release(int arg) {
	if (tryRelease(arg)) {
		Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```

再`tryRelease(arg)`中会将锁释放一次，如果当前state是1，且当前线程是正在占用的线程，释放锁成功，返回true，否则因为是可重入锁，释放一次可能还在占用，应一直释放直到state为0为止

```
private void unparkSuccessor(Node node) {
	int ws = node.waitStatus;
	if (ws < 0)
		compareAndSetWaitStatus(node, ws, 0);
	Node s = node.next;
	if (s == null || s.waitStatus > 0) {
		s = null;
		for (Node t = tail; t != null && t != node; t = t.prev)
			if (t.waitStatus <= 0)
				s = t;
	}
	if (s != null)
		LockSupport.unpark(s.thread);
}
```

然后优先找下一个节点，如果取消了就从尾节点开始找，找到最前面一个可用的节点

将其取消阻塞状态。

阻塞在`acquireQueued`的地方在唤醒之后开始继续执行，当前节点已经是最前面的一个可用（未取消）节点了,经过不断的for循环以及在`shouldParkAfterFailedAcquire`中不断向前寻找可用节点，因此这个被唤醒的节点一定可以使其之前的节点为head。然后获取锁成功。

但是此时节点会与新加入的节点竞争，也就是不公平锁的由来。

在公平锁中，在`tryAcquire`时会判断之前是否有等待的节点`hasQueuedPredecessors()`,如果有就不会再去获取锁了,因此能保证顺序执行。

### 总结

我们可以看到，实现上述的功能，`ReentrantLock`只要实现的`tryAcquire`和`tryRelease`即可实现一个独占锁的获取和释放的功能。

## 共享模式

`ReentrantReadWriteLock`是Java中读写锁的实现，写写互斥，读写互斥，读读共享。读写锁在内部分为读锁和写锁，因为我们要探索共享模式，因此更关注读锁。

```
class X {
   private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
 
   public void m() {
     rwl.readLock().lock();
     try {
       read();
     } finally {
       rwl.readLock().unlock();
     }
   }
}
```

### 获取锁

读锁加锁,先尝试获取共享锁，如果获取不到，在进行其他操作

```
public final void acquireShared(int arg) {
	if (tryAcquireShared(arg) < 0)
		doAcquireShared(arg);
}
```

在tryAcquireShared中如果当前有写锁，返回-1，即未获取共享锁，需要执行下一步`doAcquireShared`。

反之就是可以获取共享锁。

设置共享锁需要修改state的数量，表示获取共享锁的线程的数量，当共享锁的获取存在竞争时，即`compareAndSetState(c, c + SHARED_UNIT))`可能设置失败，此时进入`fullTryAcquireShared(current)`进行获取共享锁的完整版操作。

```
protected final int tryAcquireShared(int unused) {
    Thread current = Thread.currentThread();
    int c = getState();
    if (exclusiveCount(c) != 0 &&
        getExclusiveOwnerThread() != current)
        return -1;
    int r = sharedCount(c);
    if (!readerShouldBlock() &&
        r < MAX_COUNT &&
        compareAndSetState(c, c + SHARED_UNIT)) {
        //设置firstReader，计算数量，略
        return 1;
    }
    return fullTryAcquireShared(current);
}
```

也就是说共享锁获取时：

- 如果当前没有独占锁在占用，AQS根据其实现类的`tryAcquireShared`来实现让一个共享锁直接获取到锁(可以直接执行)
- 当有独占锁在占用是，让共享锁去等待直到独占锁解锁为止，也就是`doAcquireShared(arg)`的逻辑

```
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) {
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

`doAcquireShared(arg)`处了将线程封装成节点入队外还表达了3个思想：

- 什么时候该执行
- 什么时候该传播
- 什么时候该等待（阻塞）

其中入队、执行和等待的逻辑基本和独占锁一样，

- 入队：都是加入等待队列的末尾，成为`tail`节点；
- 执行：判断当前节点的前一个节点是不是头节点，如果是的话尝试获取锁，如果获取到了就执行；
- 等待：获取不到或前一个节点不是头节点就代表该线程需要暂时等待，直到被叫醒为止。设置前一个节点为`SIGNAL`状态，然后进入等待。

其中不同的就是共享锁的传播逻辑：

想象一下，当前有一个写锁正在占用，有多个读锁在等待，当写锁释放时，第二个线程也就是想要获取读锁的线程就可以获取锁了。获取到之后当前正在用的锁就是读锁了，那后面的读锁呢，因为读锁是共享的，后面的读锁应该也能够依次获取读锁，也就是读锁的传播机制。

```
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; 
    setHead(node);
    if (propagate > 0 || h == null || h.waitStatus < 0 ||
        (h = head) == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared();
    }
}
```

将当前的节点设置为头节点，判断如果是共享锁，执行`doReleaseShared()`，唤醒当前节点

```
private void doReleaseShared() {
    for (;;) {
        Node h = head;
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) {
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                    continue;
                unparkSuccessor(h);
            }
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;
        }
        if (h == head)
            break;
    }
}
```

当前节点唤醒之后`doAcquireShared(int arg)`会继续执行,因为之前的节点被设置为头节点,如果后续是获取共享锁的节点会继续执行`setHeadAndPropagate`,一直传播下去直到遇到获取独占锁的节点。

共享锁的获取总结如下：

- 尝试获取共享锁，如果当前是共享锁或无锁，设置共享锁的`state`,获取锁
- 如果当前是写锁，进入等待流程
- 入队，加入等待队列的末尾，成为`tail`节点
- 判断当前节点的前一个节点是不是头节点，如果是的话尝试获取锁，如果获取到了就执行
- 获取不到或前一个节点不是头节点就代表该线程需要暂时等待，直到被叫醒为止。设置前一个节点为`SIGNAL`状态，然后进入等待
- 如果可以获取到锁，设置头节点并进入共享锁节点传播流程

### 释放锁

共享锁使用完毕需要释放锁，分为`tryReleaseShared(arg)`和`doReleaseShared()`2个阶段

```
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}
```

在`tryReleaseShared(arg)`，基本就是`tryAcquireShared(int unused)`的反向操作

将设置的`HoldCounter`减少，`firstReader`设置null，`state`减少,将`tryAcquireShared(int unused)`添加的状态全部反向还原回去

当共享锁全部释放完毕，返回true，否则返回false

然后执行`doReleaseShared()`，刚才已经提及，`doReleaseShared()`将唤醒下一个可用的节点，独占节点将会执行，共享节点执行并传播。

## 总结

AQS共享模式和独占模式的实现上最大的差别就在于共享模式获取锁后的传播。

其他的区别主要还是表现在实现类实现的区别上。通过ReentrantLock和ReentrantReadWriteLock可以了解AQS的独占模式和共享模式，但是要注意将AQS和锁的实现剥离开，弄明白哪些逻辑是AQS实现的，哪些逻辑是锁实现的，同时也思考怎么使用AQS实现其他的特定的线程同步问题。