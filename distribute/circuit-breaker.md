---
description: 什么是断路器模式
---
# 什么是断路器模式

在分布式的系统中,要进行大量的远程网络调用。对于本都调用和远程调用最大的区别就是远程调用可能会失败。可能会超时或是连接失败。对于超时的失败，会等到直到超时为止。本来毫秒级别的调用可能达到超时时间设置的几秒。尤其是对于多个系统的调用，当一个系统A崩溃，其他系统B调用该系统超时，调用系统B的系统C同样会超时，就会在分布式系统中发生严重的级联超时错误。而断路器模式就是及时切除病灶，以一种返回错误的方式避免整个系统因为一个系统崩溃而产生大规模级联崩溃问题。

断路器的基础原理比较简单。在断路器本身封装远程函数调用，同时监控调用的各种失败情况。当失败达到一个指定的阈值时，断路器切换状态。再次进行远程调用时，直接返回失败。当断路器打开时，就说明系统中某个服务发生了比较严重的问题，应该报警通知运维人员进行处理。断路器的流程如下图所示：

![image-20200313112521717](D:\project\Java-Advance\distribute\.gitbook\assets\image-20200313112521717.png)



这是一个简单的断路器的例子。

断路器的初始化，设置断路器超时时间，阈值以及监控通知装置。

```java
CircuitBreaker circuitBreaker = CircuitBreaker.create(timeout,threshold,monitor);
```

然后是断路器本身，断路器有2种状态：

- 一种是关闭:CLOSED，断路器默认状态，远程调用正常
- 一种是打开:OPEN，当错误超过阈值时，断路器生效，远程调用直接返回失败

所有的远程调用都是通过断路器,类似拦截器，如果超时或是发生其他错误，记录错误次数，当错误次数超过阈值时，切换断路器状态为OPEN，然后报警器报警。当调用成功后，失败次数清零。伪代码如下:

```java
public class CircuitBreaker{
	private int timeout,threshold,fail;
	private Monitor monitor;
	private int state;
	
	public static CircuitBreaker create(){
		timeout = 10,threshold=5;
		monitor = new Monitor();
		state = OPEN;
	}
	
	public void call(function,args){
		switch(state){
			case CLOSED: 
				try{
					doCall(function,args);
				}catch(TimeoutException e){
					fail++;
					if(fail>=threshold){
						this.state = OPEN;
                        monitor.alert();
					}
					throw new TimeoutException();
				}
				return;
			case OPEN:
            	throw new Exception("CircuitBreaker is OPEN");
            default:
            	throw new Exception("Unreachable Code");
		}
	}
	
	/**
	 * 执行调用,外面包装一层,检测timeout,超时则抛出异常
	 * 在Java中可以使用线程池配合Future,future可以在指定时间内获取
	 * 或者仅记录调用时间,当时间超过timeout时,fail++
	 */
	public void doCall(function,args){
		Task task = new Task(function,args);
		Future future = Executor.submit(task);
        future.get(timeout);
        fail = 0;
	}
}
```

这个简单的断路器可以良好的工作，但是当断路器断开之后就永远无法继续调用了，因此需要有个机制能够在服务恢复正常时，断路器自动切换回CLOSED状态。

断路器增加了HALF_OPEN状态，在状态为OPEN后可以在经过一段时间之后进入HALF_OPEN状态，在HALF_OPEN状态下会进行尝试性调用，当目标服务可用之后，切换回CLOSED状态。

![image-20200313112541949](D:\project\Java-Advance\distribute\.gitbook\assets\image-20200313112541949.png)

伪代码如下:

```java
public class ResetCircuitBreaker{
	private int timeout,threshold,fail,resetTimeout;
	private Date lastFailDate;
	private Monitor monitor;
	private int state;
	
	public static CircuitBreaker create(){
		timeout = 10,threshold=5,resetTimeout=10*1000;
		monitor = new Monitor();
		state = OPEN;
	}
	
	public void call(function,args){
		switch(state()){
			case CLOSED,HALF_OPEN: 
				try{
					doCall(function,args);
				}catch(TimeoutException e){
					fail++;
					if(fail>=threshold){
						this.state = OPEN;
                        monitor.alert();
                        lastFailDate = new Date();
					}
					throw new TimeoutException();
				}
				return;
			case OPEN:
            	throw new Exception("CircuitBreaker is OPEN");
            default:
            	throw new Exception("Unreachable Code");
		}
	}
    
    public int state(){
        if(fail>=threshold && (now()-lastFailDate)>resetTimeout){
            return HALF_OPEN;
        }
        if(fail>=threshold){
            return OPEN;
        }
        return CLOSED;
    }
	
	public void doCall(function,args){
		//同上
        fail = 0;
        lastFailDate = null;
	}
}
```
这是一个解释性的代码,实际代码要复杂的多。可以用于生产环境的断路器会有更多的特性和配置。断路器也能够处理各种各样的问题，但是，值得注意的是，并不是所有的问题都应该使用断路器，应该认识清楚断路器的使用场景，有的问题只需要报错或抛异常等一般的处理方法即可。

对于不同的问题（异常），可以使用不同的阈值，比如超时设置的大一点，网络连接错误设置的小一点。

断路器本身可以作为一种设计模式，用于保护需要受到保护的资源，防止产生更大的问题。

断路器是一个非常值得监控的组件。任何断路器的状态的改变





##### 参考文档

https://martinfowler.com/bliki/CircuitBreaker.html