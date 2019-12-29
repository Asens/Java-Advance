---
description: JUC杂谈
---
# JUC杂谈

再java.util.concurrent中包含Java中处理并发的类。

## Atomic原子类

Atomic原子类对应着普通的类，比如AtomicInteger对应着普通的Integer，但是是线程安全的。

当有一个成员变量Integer i，对各线程执行i++时，并不会按照指定的逻辑行为正常运行。因为i++不是原子操作。为了解决这个问题，Java提供了原子类。

像AtomicInteger中的incrementAndGet()就是i++的原子操作版本。

再Atomic中的实现原子操作的方法都基本一样。比如incrementAndGet()

```
public final int incrementAndGet() {
    return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
}
```

具体实现

```
public final int getAndAddInt(Object var1, long var2, int var4) {
    int var5;
    do {
        var5 = this.getIntVolatile(var1, var2);
    } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));
	return var5;
}
```

AtomicInteger中的value是volatile类型的，保证读写可见性，但即使是volatile类型的也不能保证i++的原子性。因此Atomic使用循环+CAS的方式实现了i++的原子性。

现代的CPU指令集可以保证CAS操作的原子性的。同一时间保证只执行一个线程的cas操作。通过不断循环直到CAS设置成功为止，因为valueOffset的对应的value是volatile的，每次都可以读取最新的值。因此当CAS设置成功就代表当前只有一个线程获取了最新的值并更新该值成功。

其他的Atomic也是类似的机制，提供一个volatile类型的value，该value的offset，通过不断循环的CAS操作设置value来达到获取并设置value的原子性。