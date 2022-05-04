## 轻量的透明网关

## 应用场景

可以根据客户端传入节点信息，把客户端连接转发到对应的后端结点上。对于有状态的服务有利于性能优化与安全保护。
例如，游戏中可以进行逻辑分服，而域名只配置一个即可。

## 能力

用于支持 http, websocket 连接类型的透明对等网关。

    基于 io.vertx:vertx-web-proxy 4.2.7 版本做得逻辑业务实现。

|  客户端连接协议   | 网关监听协议  |  后端服务监听协议  |
|  ----  | ----  | ----  |
| http  | http | http |
| websocket  | websocket | websocket |

## 使用

### examples

1. test/java/clent  简单的各协议的客户端实现，可以直接 run 起来.
2. test/java/server 简单的各协议的服务端实现，可以直接 run 起来.

### 后端获取策略

http 与 websocket 是基于连接后的第一个请求指定header **server-identify** 的值来获取后端的 region 信息 。

内置策略

- origin : tcp, kcp 第一个带长度的数据包的值。websocket 的 server-identify 。
    - 假如需要传递的是 192.168.1.1:9090 的值
    - tcp, kcp : 四字节的长度 length 16 + 192.168.1.1:9090 字符串转数组。
    - websocekt : header对应的值 是 HOST:PORT 格式的值
- jwt ：把上述的 host:ip 换成 JWT 的值，传输规则一致。
- redis ：把上述的 host:ip 换成 JWT 的值，传输规则一致。
- zookeeper ：把上述的 host:ip 换成 JWT 的值，传输规则一致。
- 可以自己根据需求添加，基于 例如 etcd 等获取后端 region 的策略。

### 配置文件

config/config.json

```json
{
  "gatePort": 8099,
  "forwardedHost": true,
  "backEndIdentifyMode": "origin",
  "backEndIdentifyHeaderName": "Identify-Value",
  "vertxConfig":{

  },
  "redisConfig": {

  },
  "httpClientConfig": {

  },
  "proxyOptionsConfig": {

  },
  "zookeeperConfig": {

  }
}
```

### http / websocket

#### 对等连接
1. 启动 test/java/server 的后端服务器例子，监听指定端口.
2. 根据配置文件注释，开启网关，并监听网关指定端口，接收到客户端的连接请求，转发到后端。
3. 启动 test/java/client/ 的客户端，连接到网关，发送按照配置策略的后端地址后，网关转发并回应源服务器响应。

## 开发

1. 采用 kotlin 1.6.20 版本实现， JDK 版本 为 11 。
2. vertx 版本 4.2.7 , 底层为 Netty 实现, 核心是非阻塞。
