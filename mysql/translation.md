---
description: 介绍事务和事务的隔离级别
---

# 事务隔离与传播

### 事务是什么

事务是一组原子性的SQL查询,是一个独立的工作单元,事务中所有的SQL语句要么全部执行成功,要么全部执行失败,事务包含4个特性

##### 原子性

一个事务必须被视为一个不可分割的最小工作单元,整个事务中的所有操作要么全部提交成功,要么全部失败回滚,对于一个事务来说,不可能只执行其中的一部分操作,这就是事务的原子性。

##### 一致性

数据库总是从一个一致性的状态转换到另外一个一致性的状态。

##### 隔离性

一个事务所做的修改在最终提交以前,对其他事务是不可见的。

##### 持久性

当事务提交,则其所做的修改就会永久保存到数据库中。

### 事务的隔离级别

在SQL标准中定义了四种隔离级别,每一种级别都规定了个事务中所做的修改,哪些在事务内和事务间是可见的,哪些是不可见的。较低级别的隔离通常可以执行更高的并发,系统的开销也更低

#### Read Uncommitted (读未提交/脏读)

一个事务中可以读取其他事务未提交的数据，会导致脏读。

#### Read Committed（读已提交/不可重复读）

在一个事务中，读取到数据一定是其他事务已提交的数据。

当事务A开始时，读取数据A，假设A=3，事务A读取数据A获取的值是3，此时事务B修改了数据A，A=5，事务B提交，此时事务A再次读取，获得到的值A=5，事务A获取到的是已提交的数据，但是这个数据多次读取不一定获取相同的结果

#### Repeatable Read(可重复读/幻读/MySQL默认级别)

在此级别，读取到的值不会修改，但是可能增减，当插入了一条新纪录，查询该范围的记录是，会发现多了一条记录，也就是幻行

#### Serializable（串行化）

最高的隔离级别，会在读取的每一行数据加锁，效率最低

### 事务传播性

事务的传播性一般在事务嵌套时候使用，比如在事务A里面调用了另外一个使用事务的方法

那么这俩个事务是各自作为独立的事务执行提交，还是内层的事务合并到外层的事务一块提交，需要指定策略，也就是事务传播性。

> propagation
>
> 传播; 扩展; 宣传; 培养;

#### 在Spring中使用

```java
 @Transactional(propagation = Propagation.NESTED)
```

#### PROPAGATION_REQUIRED

Spring默认的事务传播机制,如果外层有事务则当前事务加入到外层事务，如果外层没有事务则当前开启一个新事务。

#### PROPAGATION_REQUIRES_NEW

该传播机制是每次新开启一个事务，同时把外层的事务挂起，当前新事务执行完毕后在恢复上层事务的执行。

新的事务失败会回滚，上层事务是否回滚与该事务无关，如果上层事务try，catch了新事务的异常，不继续抛出异常就会正常执行，否则外层事务也会因为异常而回滚。

#### PROPAGATION_SUPPORTS

如果外层有事务则加入，外层没有事务也不开启事务

#### PROPAGATION_NOT_SUPPORTED

如果外层有事务则将事务挂起，以无事务的方式执行完，外层事务在开始

#### PROPAGATION_NEVER

该传播机制不支持事务，如果外层存在事务则直接抛出异常。

#### PROPAGATION_MANDATORY

该传播机制仅支持事务下调用，如果外层不存在事务则直接抛出异常。

#### PROPAGATION_NESTED

事务有一个保存点，允许内部事务和外部事务联合提交，以及当内部事务失败时，外部事务可以提交成功

可以算是结合了PROPAGATION_REQUIRED和PROPAGATION_REQUIRES_NEW
