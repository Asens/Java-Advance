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

当` state`小于0时，新加入的线程需要等待直到有线程执行完毕释放为止

当Semaphore初始化时设定同时执行线程的数量，当有线程需要执行时，调用`acquire()`,释放时调用`release()`

```
Semaphore available = new Semaphore(10)
available.acquire()
available.release()
```

`acquire()`执行时，` state`是可用资源量，如果` state-1<0`，需要等待

`release()`执行时，如果` state+1`，只要释放一个资源就会无条件返回true

```
protected final boolean tryReleaseShared(int releases) {
    for (;;) {
        int current = getState();
        int next = current + releases;
        if (next < current) // overflow
            throw new Error("Maximum permit count exceeded");
        if (compareAndSetState(current, next))
            return true;
    }
}
```

也就会执行`doReleaseShared()`

```
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}
```

在`doReleaseShared`中会唤醒等待的节点，因为是共享锁，会在后续触发唤醒传播。

## CyclicBarrier

可循环使用的栅栏。

`CyclicBarrier`并没有使用AQS，而是使用了`ReentrantLock`，算是间接使用吧。

`CyclicBarrier`的功能和`CountDownLatch`功能有些类似，可以使指定数量的线程等待直到线程到齐，然后一起执行下一步。

就好像大家越好一起出去玩，有的人先到了，需要等后来的人，等到齐了大家一起出发一样。

在调用的时候只需要调用`await()`就可以了，等到指定数量的线程调用`await()`，全部线程同时继续执行（确切的说使在极短的时间内依次唤醒）。

因此使用非常简单。

而且不同于`CountDownLatch`的是，`CyclicBarrier`是可循环执行的，当等到指定数量的线程调用`await()`，全部线程同时继续执行后，内部回恢复初始状态，使得可以再次有相同的或不同的线程再次调用`await()`能够和第一次达到完全相同的效果。

下面是`await()`的核心代码，省略部分异常处理等代码

```
private int dowait(boolean timed, long nanos){
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        final Generation g = generation;
        int index = --count;
        if (index == 0) {  // tripped
            boolean ranAction = false;
            final Runnable command = barrierCommand;
            if (command != null)
                command.run();
            ranAction = true;
            nextGeneration();
            return 0;
        }
        for (;;) {
            if (!timed)
            	trip.await();
            else if (nanos > 0L)
                nanos = trip.awaitNanos(nanos);
            if (g != generation)
                return index;
        }
    } finally {
        lock.unlock();
    }
}
```

当线程执行`await()`时，会首先获取`lock`，假设要等待的线程数量为10，第一个线程的index为9，会进入`trip.await()`，`trip`是一个`Condition`，当执行`await()`时会进入等待状态，会释放当前锁以便下一个线程可以获取锁。

当第10个线程获取锁时，index为0，进入`nextGeneration()`,也就是重新初始化`CyclicBarrier`

```
private void nextGeneration() {
	trip.signalAll();
    count = parties;
    generation = new Generation();
}
```

`nextGeneration()`做2件事

- 唤醒全部节点，开始执行
- 将count和generation恢复成初始状态，使其恢复冲再次可以工作的初始状态。

## 总结

` CountDownLatch`是一个一次性的栅栏，比较灵活，可以让任意线程等待，也可以让任意线程执行`countDown()`直到指定的次数，使等待的线程被唤醒。

`Semaphore`可以指定的运行线程的最大数量，适合控制资源访问。

`CyclicBarrier`是一个可循环的栅栏，线程到齐自动执行下一步。

每个组件都有自己比较适合的使用场景，需要按照自己的需求进行选择。同时这些组件本质上也都是依赖AQS。当这些组件都不满足需求时，可以发挥自己的创造力使用AQS开发出更适合的新的组件。