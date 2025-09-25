# TCP协议通信系统 v2.0

这是一个基于Java Spring Boot和Netty的TCP协议通信系统，包含服务端和客户端两个项目，支持自定义二进制协议和配置文件驱动的模拟测试。

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Netty](https://img.shields.io/badge/Netty-4.1.100-blue.svg)](https://netty.io/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)

## 🚀 新协议特性

### 协议格式
```
header + length + data
```

### 请求报文header（42字节）
- 第1-6字节：发送系统节点号
- 第7-12字节：接收系统节点号  
- 第13字节：交易类型标识（0-业务类交易，1-签到交易）
- 第14-20字节：7位交易类型码（不足7位后补'\0'）
- 第21-40字节：20位交易流水号
- 第41-42字节：预留

### 响应报文header（2字节）
- 第1字节：返回状态（00-成功，其他-失败）
- 第2字节：预留

### 长度字段（4字节）
32bit网络字节序整数，标识整个报文的长度

## 📋 功能特性

### 服务端功能
- **TCP协议服务器** - 监听8888端口，处理客户端连接
- **签到交易** - 用户身份验证
- **业务交易** - 支持查询、更新、删除等业务操作
- **模拟测试** - 配置文件驱动的场景测试
- **HTTP管理接口** - 8080端口提供REST API管理功能
- **实时监控** - 连接状态和统计信息

### 客户端功能
- **TCP协议客户端** - 连接到服务端进行协议通信
- **REST API** - 8081端口提供HTTP接口操作
- **自动测试** - 启动时自动执行测试用例
- **响应保存** - 支持将响应数据保存到文件

## 🏗️ 项目结构

```
tcp-demo/
├── tcp-server/          # TCP服务端项目
│   ├── src/main/java/   # Java源代码
│   ├── files/           # 服务端文件存储目录
│   ├── README.md        # 服务端说明文档
│   └── pom.xml         # Maven配置文件
├── tcp-client/          # TCP客户端项目
│   ├── src/main/java/   # Java源代码
│   ├── downloads/       # 客户端下载目录
│   ├── README.md        # 客户端说明文档
│   └── pom.xml         # Maven配置文件
├── test-system.sh       # Linux/Mac测试脚本
├── test-system.bat      # Windows测试脚本
└── README.md           # 项目说明文档
```

## 🛠️ 快速开始

### 环境要求
- Java 17+ (传统运行)
- GraalVM JDK 17+ (原生二进制运行)
- Maven 3.6+

### 方式一：传统 JAR 运行
```bash
# 1. 启动服务端
cd tcp-server
mvn spring-boot:run

# 2. 启动客户端 (新终端)
cd tcp-client
mvn spring-boot:run
```

### 方式二：原生二进制运行 (推荐)
```bash
# 1. 构建原生二进制文件
./build-native.sh    # Linux/Mac
# 或
build-native.bat     # Windows

# 2. 运行原生版本
./run-native.sh      # Linux/Mac
# 或
run-native.bat       # Windows
```

### 方式三：一键测试脚本
```bash
# Linux/Mac
chmod +x test-system.sh
./test-system.sh

# Windows
test-system.bat
```

## 🚀 原生二进制优势

### 性能对比
| 指标 | 传统 JAR | 原生二进制 | 提升 |
|------|----------|------------|------|
| 启动时间 | 3-5秒 | 100-200ms | **15-25倍** |
| 内存占用 | 200-300MB | 50-80MB | **60-75%** |
| 文件大小 | 50-100MB | 20-40MB | **50-60%** |
| CPU使用 | 较高 | 较低 | **30-50%** |

### 部署优势
- ✅ **无需 JVM** - 直接运行二进制文件
- ✅ **单文件部署** - 一个文件包含所有依赖
- ✅ **快速启动** - 毫秒级启动时间
- ✅ **低资源消耗** - 适合容器化部署
- ✅ **跨平台** - 支持 Linux、Windows、macOS

### 适用场景
- 🏢 **生产环境** - 高并发、低延迟要求
- 🐳 **容器化** - Docker、Kubernetes 部署
- ☁️ **云服务** - Serverless、微服务架构
- 📱 **边缘计算** - 资源受限环境

## 📊 管理接口

### 服务端管理接口 (8080端口)
- `GET /` - 服务器首页
- `GET /admin/status` - 服务器状态
- `GET /admin/clients` - 连接客户端
- `GET /admin/protocol/stats` - 协议统计
- `GET /admin/files/stats` - 文件统计
- `GET /admin/health` - 健康检查
- `GET /admin/system` - 系统信息
- `GET /api/simulation/scenarios` - 模拟场景
- `POST /api/simulation/start/{scenarioName}` - 启动场景

### 客户端操作接口 (8081端口)
- `POST /api/tcp/connect` - 连接服务器
- `POST /api/tcp/disconnect` - 断开连接
- `GET /api/tcp/status` - 连接状态
- `POST /api/tcp/signin` - 发送签到请求
- `POST /api/tcp/query` - 发送查询请求
- `POST /api/tcp/update` - 发送更新请求
- `POST /api/tcp/delete` - 发送删除请求
- `POST /api/tcp/business` - 发送自定义业务请求
- `POST /api/tcp/save` - 保存响应数据到文件

## 🧪 模拟测试

### 预定义场景
1. **signin_scenario** - 用户签到场景
2. **query_scenario** - 查询业务场景
3. **update_scenario** - 更新业务场景
4. **delete_scenario** - 删除业务场景
5. **error_scenario** - 错误处理场景
6. **complex_scenario** - 复合业务场景

### 启动场景测试
```bash
# 启动签到场景
curl -X POST http://localhost:8080/api/simulation/start/signin_scenario

# 启动查询场景
curl -X POST http://localhost:8080/api/simulation/start/query_scenario

# 启动复合场景
curl -X POST http://localhost:8080/api/simulation/start/complex_scenario
```

## ⚙️ 配置说明

### 服务端配置 (tcp-server/src/main/resources/application.yml)
```yaml
server:
  port: 8080

tcp:
  server:
    port: 8888
    file:
      directory: files
  
  # TCP协议模拟配置
  simulation:
    enabled: true
    mode: "server"
    scenarios:
      - name: "signin_scenario"
        description: "用户签到场景"
        steps:
          - step: 1
            type: "send"
            senderNodeId: "CLIENT"
            receiverNodeId: "SERVER"
            transactionType: 1
            transactionCode: "SIGNIN"
            transactionSerial: "20240101000000000001"
            data: "user:admin,password:123456"
            delay: 1000
          - step: 2
            type: "receive"
            expectedStatus: 0
            timeout: 5000
```

### 客户端配置 (tcp-client/src/main/resources/application.yml)
```yaml
server:
  port: 8081

tcp:
  client:
    server:
      host: localhost
      port: 8888
    connect:
      timeout: 5000
    download:
      directory: downloads
    sender:
      nodeId: CLIENT
```

## 📈 监控和统计

### 实时统计
- 连接客户端数量
- 总请求数
- 总响应数
- 交易流水号计数器

### 协议信息
- 请求header长度：42字节
- 响应header长度：2字节
- 长度字段长度：4字节
- 支持的交易类型和状态码

## 🔧 扩展开发

### 添加新的交易类型
1. 在服务端 `TcpProtocolHandler` 中添加新的处理逻辑
2. 在客户端 `TcpClient` 中添加新的请求方法
3. 更新配置文件中的场景定义
4. 添加相应的测试用例

### 自定义场景
1. 在 `application.yml` 中添加新的场景配置
2. 定义步骤序列（发送/接收）
3. 设置延迟和超时参数

### 协议扩展
1. 修改 `TcpProtocol` 类添加新字段
2. 更新编解码器处理新格式
3. 修改处理器支持新功能

## 🐛 故障排除

1. **连接失败** - 检查服务端是否启动，端口是否被占用
2. **协议解析错误** - 检查报文格式是否符合规范
3. **场景执行失败** - 检查配置文件格式和参数设置
4. **内存问题** - 调整JVM参数和Netty缓冲区大小

## 📝 更新日志

### v2.0.0
- 实现新的TCP协议格式（header+length+data）
- 支持签到交易和业务交易
- 添加配置文件驱动的模拟测试
- 完善的管理接口和监控功能
- 支持多种业务交易码
- 客户端自动测试功能
- 完整的REST API接口

### v1.0.0
- 基础文件传输功能
- 简单的二进制协议
- HTTP管理接口

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
