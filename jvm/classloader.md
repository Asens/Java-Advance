---
description: 虚拟机加载类以及双亲委派机制
---

# Java虚拟机加载机制

虚拟机需要加载Class文件到内存才可以运行，这边文章将描述这个过程。

## 触发

- 遇到new，getstatic，putstatic，invokestatic4条字节码指令时，会触发初始化。即用`new`创建对象时，读取或设置一个类的静态字段时，或调用一个类的静态方法时。
- 对一个类进行反射调用时
- 当初始化一个类，但是其父类未初始化时，初始化父类
- 执行一个类的main函数时，初始化该类
-  当使用JDK1.7动态语言支持时，如果一个 java.lang.invoke.MethodHandle实例最
  后的解析结果为REF_getStatic,REF_putStatic,REF_invokestatic的方法句柄 ，初始化

## 过程

### 加载

