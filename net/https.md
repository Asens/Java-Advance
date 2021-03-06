---
description: https协议解决的问题和方式
---

# Https协议详解

## 概述

Https是安全版本的http协议在网络传输过程中是明文的，密码或是敏感信息在网络中传输会有被监听

## 要解决的问题

- 窃听风险（eavesdropping）：第三方可以获知通信内容。
- 篡改风险（tampering）：第三方可以修改通信内容。
- 冒充风险（pretending）：第三方可以冒充他人身份参与通信。

为了解决这些问题，需要设计协议可以达到：

- 所有的信息加密传输，无法被窃听
- 具有匹配校验机制，无法被篡改
- 具有身份证书，无法冒充

## 加密基础

**密钥**

一种参数，它是在明文转换为密文或将密文转换为明文的算法中输入的参数。

**数字证书**

由一个可信的组织验证和签发的识别信息。

**数字签名**

数字签名是附加在报文后的校验码

在传输过程中将报文摘要使用私钥加密，附加在传输的内容后面，收到消息的解密之后对比解密后的内容和报文摘要来判断传输内容的完整性和是否内容被篡改

**对称加密**

加密和解密使用用一个密钥

流行的对称密钥加密算法包括：DES、Triple-DES、RC2 和 RC4

密钥作为加密和解密的唯一参数，非常重要

速度较快

**非对称加密**

加密和解密使用不同的密钥

RSA算法是最广为使用的非对称加密算法。

为了防止暴力破解，选择1024或2048位

RSA加密比非对称加密要慢

[RAS算法](https://www.jianshu.com/p/fbb8bf7baa97)

## 流程

1. 客户端发起https请求，提出自己支持的协议版本号，算法等
2. 服务器返回加密算法和数字证书，证书包含服务器公钥
3. 客户端收到证书后判断是否信任证书，信任的话继续
4. 客户端生成随机数Pre-master secret，使用公钥加密，同时将之前的通信信息（秘钥算法、证书等）按照约定的hash/摘要算法获取hash值，并使用随机数和协商好的对称加密算法进行签名加密，将公钥加密后的Pre-master secret和加密签名发送到服务端。
5. 服务器端接收到后，使用私钥解密获得随机数，使用约定的对称加密算法解密，对比
6. 此时双方均有Pre-master secret，用于之后信息的对称加密
7. 但凡有一步验证失败，整体流程失败中止

按照此流程解决了之前提出的问题：

- 首先传递的信息一直都是加密的，除了客户端和服务器端都无法获得对称加密的key，因此无法解密
- 存在报文摘要和数字签名机制，篡改会被发现
- 数字证书是由权威机构发布，绑定域名，无法冒充

## 实践
生成数字证书,在Nginx加入如下配置:
```
server {
        listen 443 ssl;
        server_name www.host.com;

        ssl_certificate      /cwjhttps/cwj.crt;
        ssl_certificate_key  /cwjhttps/cwj.key;

        ssl_session_cache    shared:SSL:1m;
        ssl_session_timeout  5m;

        ssl_ciphers  HIGH:!aNULL:!MD5;
        ssl_prefer_server_ciphers  on;

        location / {
            root   /cwjhttps;
            index  home.html index.htm test.html;
        }
    }
```

