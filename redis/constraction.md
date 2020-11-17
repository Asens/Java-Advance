---
description: redis的各种结构使用和原理

---

# Redis结构

- String
- List
- Set
- Hash
- ZSet



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



### 散列（字典）

