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

类加载的过程包括加载、验证、准备、解析、初始化、使用、卸载。

其中验证、准备、解析属于连接阶段。

### 加载

在记载阶段，虚拟机需要完成

- 通过类的全限定名来获取类的二进制字节流，可以通过文件，网络，或动态生成
- 把字节流转化为方法区的运行时数据结构
- 在内存中生存这个类的Class对象

加载阶段可以定义类加载器去控制字节流的加载方式

### 验证

为了确保Class文件符合虚拟机要求，且不会危害虚拟机本身。

验证操作包括：文件格式验证、元数据验证、字节码验证、符号引用验证。

**文件格式验证**

判断Class字节流是否符合Class文件格式的规范，保证Class能够正确的解析并存储在方法区内。比如：

- 是否以0xCAFEBABE开头
- Java的主次版本号格式，以及是否在虚拟机的处理范围之内
- 常量池的常量是否有不支持的类型
- ...

**元数据验证**

对字节码进行语义分析，保证信息符合Java语言规范的要求

- 这个类是否有父类
- 这个类的父类是否继承了final类
- 如果不是实现类，是否实现了父类或接口的全部需要实现的方法
- ...

**字节码验证**

通过数据流和控制流分析，确定程序语义是正确的

- 保证操作数栈的数据类型和指令代码系列可以配合，比如不会放入一个int类型，然后以long型加载
- 保证跳转指令不会跳到方法体外面
- 保证类型转换是有效的
- ...

**符号引用验证**

在解析阶段，虚拟机将符号引用转化为直接引用，此时需要符号引用验证。主要是对类自身以外的信息进行验证。

- 根据全限定名是否能找到对应的类
- 类、字段、方法的访问性（public、private等）是否允许内当前类访问。
- ...

### 准备

为类变量(仅static)设置初始值。

```java
public static int value = 123;
```

在准备阶段会初始化为类型对应0值而不是123，除了final。

### 解析

解析阶段是将符号引用替换为直接引用的过程。

- 符号引用：用符号来描述引用的目标，能够定位到目标即可。
- 直接引用：指向目标的指针、便宜量或间接定位到目标的句柄。

虚拟机指定了特定的指令操作字节码时需要对操作符号进行解析

> 16个特定指令
>
>  anewarray、checkcast、getfield、getstatic、instanceof、invokedynamic、invokeinterface、invokespecial、invokestatic、invokevirtual、ldc、ldw、multianewarray、new、putfield、putstatic 

 解析动作主要针对类或接口、字段、类方法、接口方法、方法类型、方法句柄和调用点限定符7类符号引用。

**类或接口**

如果要解析一个类A的符号引用为类或接口，如果目标不是数组，则使用类A的类加载器去加载目标类。

如果是数组，先加载数组内的类型，在生产数组对象。

**字段解析**

解析字段首先判断字段对应的类A是否被解析过，没有解析需要先解析类A。

如果类A本身包含目标字段，返回

查找类A的父类或祖先类，直到找到该字段会返回NoSuchFieldError

**类,接口方法解析**

与字段解析类似，判断指定类是否被解析过，查找类的方法，查找父类及祖先类，找到该方法或返货NoSuchMethodError

### 初始化

在初始化阶段，开始执行程序代码。初始化的阶段就是执行`<cinit>`的过程。

而`<cinit>`就是将静态类变量赋值和静态代码块结合的方法

虚拟机会保证子类执行`<cinit>`时，父类的`<cinit>`已经执行完毕

因此父类的静态变量赋值和静态代码块优先执行

如果一个类没有静态类变量和静态代码块，不会生成`<cinit>`

虚拟机会保证`<cinit>`在多线程环境下执行可以正确的加锁，同步。这也是单例模式内部静态类实现方式在多线程环境下可以正常的前提。

## 类加载器

对于任意一个类，都由它的类加载器和这个类本身确定它在虚拟机的唯一性。

### **双亲委派模型**

类加载器可以分为3类：

- 启动类加载器（Bootstrap ClassLoader）：是虚拟机本身的一部分，负责加载存在JavaHome的lib目录，并且为虚拟机识别的类加载进内存中。
- 扩展类加载器：加载JavaHome的lib/ext目录或其他指定。
- 应用程序类加载器：系统类加载器，用于加载用户类路径上所指定的类库。

其中启动类加载器是扩展类加载器的父类

扩展类加载器是应用程序类加载器的父类

应用程序类加载器是自定义的类加载器的父类

当一个类加载器要加载一个类，会先委托父类判断是否加载过此类，依次向上判断，直到有加载器加载过此类为止，返回该类，如果没有加载器加载过该类，有顶层启动类加载器到该类加载器，依次判断能否加载此类，能则加载。

这种机制保证了在该体系的类加载器要加载一个类，可以保证该类只被一个类加载器加载过。

### 非双亲委派模型

JNDI服务是对资源进行集中管理和查找，由启动类加载器去加载

但是JNDI需要调用第三方提供的JNDI接口提供者，位于应用程序的ClassPath下

因此引入了`ThreadContextClassLoader`

SPI机制本质是父类加载器请求子类加载器去完成类的加载，使用双亲委派模型逆向使用类加载器。

还有就是OSGi的模块化的机制，同样破坏了双亲委派模型,目前使用场景不多，就不详述了。




























