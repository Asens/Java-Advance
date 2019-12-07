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
}}
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



