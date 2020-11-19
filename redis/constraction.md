---
description: redis的各种结构使用和原理

---

# Redis结构

String，List，Set，Hash，ZSet

### String

字符串类型

get，set，del，incr，decr

Redis使用的不是C语言的默认的字符串实现，是自行编写的SDS（Simple Dynamic String），是可以自动扩容的字符串。

在C语言中，默认的字符串实现是每次增加都扩容，每次所见都回收内存，获取字符串长度是遍历字符串数组，时间复杂度为O(n)

而SDS的实现首先是一个简单的数据结构，包含一个char数组，长度，以及未使用长度free

字符串小于1M时，长度扩容一倍，大于1M时，每次扩容1M，不需要每次都扩容

SDS记录了长度，获取长度时可以直接返回长度

当字符串执行substr或replace等操作时，字符串变短，会修改free，择时回收内存



### List

列表允许用户从序列的两端推入或者弹出元素,获取列表元素,以及执行各种常见的列表操作。

RPUSH，LPUSH，RPOP，LPOP，LINDEX，LRANGE，LTRIM

内部实现为双向链表

可以获取next个prev的指针

无环，头节点的prev和尾节点的next均为null

有头节点和尾节点

有链表长度计数器



### Set集合

集合不包含相同的元素，内部采用散列表的方式

sadd，smembers（返回所有元素），sismember，srem

sadd key-name a



### Hash（字典）

基础用法

HMGET key-name key

HMSET key-name key value

Redis中的散列可以存储多个键值对的映射

内部实现是哈希表，和Java的HashMap实现基本一致

字典包含2个哈希表

哈希表中包含一个Entry数组和长度负载因子等属性

当存入键值时，计算键的Hash值，获得Entry数组的位置，Entry为一个单向链表

没有值则创建链表的头节点，有值时作为节点加入链表的最后面

当size/loadFactor>数组长度时，数组的长度乘2，扩容后的数组的长度为第一个大于数组长度的2的n次幂

因为字典包含2个哈希表，在rehash时，将一个hash表的全部键值对rehash到另一个hash表中

渐进式rehash

当键值对存储量较大，一次性的rehash会影响性能

每次操作字典时，都会同时操作2个hash表，并且每次迁移一个entry链表的全部键值对

最终全部迁移完成



### ZSet

根据score排序的有序集合

zadd，zrange，zrangebyscore，zrem

zadd key score member

