---
description: volatile详解
---
# synchronized详解

在Java中加锁普通的方式就是使用synchronized。

synchronized有3种用法。

- 非静态方法加锁，锁住的是当前方法的对象
- 静态方法加锁，锁住的是该方法所在的类
- 同步块加锁，锁住的是指定的对象

在JVM规范中明确了 synchronized的实现原理。对于代码同步块：

- 当进入同步块时，加入monitorenter指令
- 当代码结束或异常退出，加入monitorexit指令

随着Java语言的不断发展，本来synchronized的性能时要低于ReentrantLock的，但是加入了偏向锁，轻量级锁和重量级锁之后，性能大大提高和ReentrantLock基本相同。

一个Java对象是有对象头的，synchronized的锁信息就存在对象头中。包括偏向锁，轻量级锁和重量级锁。

### 偏向锁

因为有很多情况，当一个线程获取锁时，没有其他线程占用锁，或是竞争锁。这个线程获取锁之后还可能持续获取这个锁。使用重量级锁对于这种情况没有问题，但是效率低。

当一个线程去获取锁的时候，首先判断对象头是否存在偏向锁，如果没有设置偏向锁的话，线程可以获取偏向锁。线程将线程id放入栈帧的锁记录中以及锁对象的对象头中。不需要CAS，不需要等待。偏向锁仅是在没有锁竞争的时候使用，速度很快。

当一个线程持有偏向锁时，其他线程竞争该锁时，持有偏向锁的线程需要撤销偏向锁，等待安全点，查询当前线程状态，线程还在执行则膨胀为轻量级锁，否则设置成无锁状态，可以被其他线程获取偏向锁。

### 轻量级锁

当线程获取轻量级锁时

- 将对象头的信息放入线程栈帧的锁记录空间中
- 将对象头的信息替换为指向锁记录空间的指针
- 使用CAS的方式

当指向完毕时，便是获取了轻量级锁。如果失败的话就尝试自旋获取锁。

释放锁时将对象头的在设置回对象头本身的信息，替换之前的锁记录空间的指针

### 重量级锁

当获取轻量级锁时，如果CAS设置失败，说明当前正在有线程在占用锁，线程会自旋获取锁，如果获取锁失败的话，会膨胀为重量级锁，进行等待。因为synchronized的语义不仅要保证正在占用的线程只有一个线程执行，同时要保证其他的要获取该锁的线程一直等待。

### 比较

其实synchronized和ReentrantLock的逻辑如出一辙。都是先尝试去获取锁，在ReentrantLock中通过state和持有锁的线程id和偏向锁类似。在ReentrantLock内部的AQS同样会使用自旋+CAS线程为头节点，类似轻量级锁，设置成功可以获取锁。设置失败就需要等待，类似重量级锁。

不同点的话ReentrantLock功能更多一点，可以tryLock,可以设置时间，可以被中断，可以设置多个等待队列等。