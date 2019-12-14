---
description:  AQS相关组件
---
# 同步组件CountDownLatch、Semaphore、CyclicBarrier

之前已经介绍过AQS队列同步器，现在介绍一下以AQS为基础的一些同步组件。当你比较深刻的理解AQS时，可以很轻松的理解这3个组件，因此推荐先看一下之前的文章：[AQS详解](https://github.com/Asens/Java-Advance/blob/master/concurrent/aqs.md)

## CountDownLatch

同步计数器。允许一个和多个线程等待，直到由1个多个线程执行指定的操作。

创建` CountDownLatch`可以指定数量，等待的线程执行` CountDownLatch`的`await()`开始等待，然后其他线程执行`countDown()`使指定的数量-1，当`state`为0时，再`await()`等待的线程被唤醒。也就是`countDown()`被执行指定数量的次数时，被唤醒。

` CountDownLatch`是一次性的，不可逆的，除非重新初始化` CountDownLatch`

熟悉AQS的原理的话其实` CountDownLatch`的实现还是比较简单的。

初始化时`public CountDownLatch(int count)`,count对应的就是` state`的值

`await()`的本质是获取共享锁。再获取共享锁时

```
public final void acquireShared(int arg) {
	if (tryAcquireShared(arg) < 0)
		doAcquireShared(arg);
}
```

当`tryAcquireShared(arg)`小于0时，获取共享锁会进入等待状态，再` CountDownLatch`中，` state`定义为指定数量。当未执行`countDown()`时，或者`countDown()`执行的次数小于` state`时，执行`await()`必然进入等待。

因此`int tryAcquireShared(int acquires)`的实现也非常简单

```
protected int tryAcquireShared(int acquires) {
	return (getState() == 0) ? 1 : -1;
}
```

也就是`getState() == 0`时,直接获取共享锁,否则等待

当执行`countDown()`时,` state`-1，` state`为0时，释放

也就是`tryReleaseShared(arg)`为true,进入`doReleaseShared()`

```
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}
```

上一篇文章提到了,当共享的节点被释放时,共享节点被唤醒并传播。

所有`await()`等待的线程被唤醒并开始执行。

## Semaphore

计数信号量。`Semaphore`的本质是实现最多指定数量的线程并发访问。

比如设置数量为10，当前只允许10个线程执行，第11个线程就要等待，直到10个线程中有执行完毕的，释放了，第11个线程才可以执行。

因为是基于AQS的，` state`为可执行线程的数量，当获取锁时` state`-1，执行完毕时` state`+1。

