---
description: ConcurrentHashMap详解
---
# ConcurrentHashMap详解

ConcurrentHashMap在JDK1.8中进行了重写，不再使用分段锁的方式，而是采用了循环+CAS的方式。获取到对应的Hash节点后使用锁，锁住该节点对应的链表的写。只锁住一个节点相比锁住全部或是锁住一段，效率更高一些。

主要分析put的逻辑

```
final V putVal(K key, V value, boolean onlyIfAbsent) {
    int hash = spread(key.hashCode());
    int binCount = 0;
    //一直循环，直到cas设定成功或达到其他跳出条件
    for (Node<K,V>[] tab = table;;) {
        Node<K,V> f; int n, i, fh;
        //默认的构造函数为空，首次执行时要初始化table，也就是node节点数组
        if (tab == null || (n = tab.length) == 0)
            tab = initTable();
        //当数组的节点是null时，cas设置该节点为新创建的节点    
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
            if (casTabAt(tab, i, null,new Node<K,V>(hash, key, value, null)))
                break;
        }
        //扩容
        else if ((fh = f.hash) == MOVED)
            tab = helpTransfer(tab, f);
        //当存在节点时需要锁住该节点，然后再该节点链表后面添加新的节点
        else {
            V oldVal = null;
            //锁住该节点的后续操作
            synchronized (f) {
                //后面就是常规map的put操作
                if (tabAt(tab, i) == f) {
                    if (fh >= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            if (e.hash == hash &&((ek = e.key) == key ||
                            		(ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {
                                pred.next = new Node<K,V>(hash, key,value, null);
                                break;
                            }
                        }
                    }
                }
            }
            if (binCount != 0) {
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);
    return null;
}
```

其他的操作和HashMap区别不大

