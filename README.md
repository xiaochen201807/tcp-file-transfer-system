# TCP文件传输系统

这是一个基于Java Spring Boot和Netty的TCP文件传输系统，包含服务端和客户端两个项目。

[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Netty](https://img.shields.io/badge/Netty-4.1.100-blue.svg)](https://netty.io/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)

## 项目结构

```
tcp-demo/
├── tcp-server/          # TCP服务端项目
│   ├── src/main/java/   # Java源代码
│   ├── files/           # 服务端文件存储目录
│   └── pom.xml         # Maven配置文件
├── tcp-client/          # TCP客户端项目
│   ├── src/main/java/   # Java源代码
│   ├── downloads/       # 客户端下载目录
│   └── pom.xml         # Maven配置文件
└── README.md           # 说明文档
```

## 功能特性

### 服务端功能
- 启动TCP服务器监听指定端口（默认8888）
- 提供文件列表查询功能
- 支持文件下载传输
- 自定义二进制协议通信
- 完善的异常处理和日志记录

### 客户端功能
- 连接到TCP服务器
- 请求服务器文件列表
- 下载指定文件到本地
- 提供REST API接口
- Web界面操作支持

## 通信协议

系统使用自定义的二进制协议进行通信：

```
协议格式: [type(1字节)] + [length(4字节)] + [data(length字节)]
```

消息类型：
- `1` - 请求文件列表
- `2` - 文件列表响应
- `3` - 请求下载文件
- `4` - 文件数据响应
- `5` - 错误响应
- `6` - 成功响应

## 快速开始

### 环境要求
- Java 1.8+
- Maven 3.6+

### JDK 1.8 特别说明
本分支专门为JDK 1.8环境优化，主要调整包括：
- Spring Boot版本：2.7.18（兼容JDK 1.8）
- 使用 `javax.annotation` 替代 `jakarta.annotation`
- 移除JDK 10+的 `var` 关键字
- 使用传统的HashMap构造方式替代 `Map.of()`

### 1. 确认JDK版本

```bash
# 确认Java版本为1.8
java -version
# 应该显示: java version "1.8.0_xxx"
```

### 2. 启动服务端

```bash
cd tcp-server
mvn clean compile
mvn spring-boot:run
```

或使用提供的JDK 1.8启动脚本：
```bash
# Windows
start-server-jdk8.bat

# Linux/Mac
chmod +x start-server-jdk8.sh
./start-server-jdk8.sh
```

服务端将在以下端口启动：
- TCP服务器：8888端口
- HTTP服务器：8080端口

### 3. 启动客户端

```bash
cd tcp-client
mvn clean compile
mvn spring-boot:run
```

或使用提供的JDK 1.8启动脚本：
```bash
# Windows
start-client-jdk8.bat

# Linux/Mac
chmod +x start-client-jdk8.sh
./start-client-jdk8.sh
```

客户端将在8081端口启动HTTP服务器。

### 3. 使用REST API

#### 连接到服务器
```bash
curl -X POST http://localhost:8081/api/files/connect
```

#### 获取文件列表
```bash
curl http://localhost:8081/api/files/list
```

#### 下载文件
```bash
curl -X POST http://localhost:8081/api/files/download \
  -H "Content-Type: application/json" \
  -d '{"fileName": "test1.txt"}'
```

#### 检查连接状态
```bash
curl http://localhost:8081/api/files/status
```

#### 断开连接
```bash
curl -X POST http://localhost:8081/api/files/disconnect
```

## 配置说明

### 服务端配置 (tcp-server/src/main/resources/application.yml)

```yaml
tcp:
  server:
    port: 8888              # TCP服务器端口
    file:
      directory: files      # 文件存储目录
```

### 客户端配置 (tcp-client/src/main/resources/application.yml)

```yaml
tcp:
  client:
    server:
      host: localhost       # 服务器地址
      port: 8888           # 服务器端口
    connect:
      timeout: 5000        # 连接超时时间(毫秒)
    download:
      directory: downloads  # 下载目录
```

## 测试文件

服务端项目包含两个测试文件：
- `tcp-server/files/test1.txt`
- `tcp-server/files/test2.txt`

您可以添加更多文件到 `tcp-server/files/` 目录进行测试。

## 日志配置

两个项目都配置了详细的日志输出，可以通过修改 `application.yml` 中的日志级别来调整：

```yaml
logging:
  level:
    com.example: DEBUG    # 应用日志级别
    io.netty: INFO       # Netty日志级别
```

## 故障排除

### 通用问题
1. **连接失败**：检查服务端是否已启动，端口是否被占用
2. **文件下载失败**：检查文件是否存在于服务端files目录
3. **权限问题**：确保应用有读写文件的权限

### JDK 1.8 特有问题
4. **编译错误 "找不到符号: 类 var"**：
   - 确认使用JDK 1.8环境
   - 检查代码中是否误用了JDK 10+的var关键字

5. **注解错误 "程序包jakarta.annotation不存在"**：
   - 确认使用的是jdk-1.8分支
   - 应该使用javax.annotation而不是jakarta.annotation

6. **Map.of() 方法不存在**：
   - JDK 1.8不支持Map.of()方法
   - 使用new HashMap<>()方式创建Map

7. **内存不足错误**：
   ```bash
   # 设置Maven内存参数
   export MAVEN_OPTS="-Xmx512m -Xms256m"
   # 或在Windows中
   set MAVEN_OPTS=-Xmx512m -Xms256m
   ```

## 版本对比

| 特性 | main分支 (JDK 17+) | jdk-1.8分支 (JDK 1.8+) |
|------|-------------------|----------------------|
| Java版本 | 17+ | 1.8+ |
| Spring Boot | 3.2.0 | 2.7.18 |
| 注解包 | jakarta.* | javax.* |
| 语法特性 | var关键字、Map.of() | 传统语法 |
| 性能 | 更优 | 良好 |
| 兼容性 | 现代环境 | 传统环境 |

## 分支说明

- **[main分支](../../tree/main)**: 使用最新的JDK 17和Spring Boot 3.x，适合现代Java开发环境
- **[jdk-1.8分支](../../tree/jdk-1.8)**: 兼容JDK 1.8的版本，适合传统Java环境或有特定版本要求的项目

## 扩展功能

系统设计支持以下扩展：
- 文件上传功能
- 文件删除功能
- 用户认证和权限控制
- 文件分片传输
- 断点续传
- 文件加密传输

## 贡献指南

1. Fork本仓库
2. 根据您的Java环境选择合适的分支
3. 创建功能分支: `git checkout -b feature/your-feature`
4. 提交更改: `git commit -am 'Add some feature'`
5. 推送分支: `git push origin feature/your-feature`
6. 提交Pull Request

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
