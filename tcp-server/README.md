# TCP协议系统 v2.0

这是一个基于Java Spring Boot和Netty的TCP协议通信系统，支持自定义二进制协议和配置文件驱动的模拟测试。

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

### 核心功能
- **TCP协议通信** - 支持自定义二进制协议
- **签到交易** - 用户身份验证
- **业务交易** - 支持查询、更新、删除等业务操作
- **模拟测试** - 配置文件驱动的场景测试
- **管理接口** - HTTP REST API管理功能
- **实时监控** - 连接状态和统计信息

### 交易类型
- **签到交易** (transactionType=1) - 用户身份验证
- **业务交易** (transactionType=0) - 业务操作

### 业务交易码
- **QUERY** - 查询操作
- **UPDATE** - 更新操作  
- **DELETE** - 删除操作

## 🛠️ 快速开始

### 1. 启动服务端
```bash
cd tcp-server
mvn spring-boot:run
```

服务端将在以下端口启动：
- TCP服务器：8888端口
- HTTP管理服务器：8080端口

### 2. 测试连接
```bash
# 查看服务器状态
curl http://localhost:8080/admin/status

# 查看协议统计
curl http://localhost:8080/admin/protocol/stats

# 查看模拟场景
curl http://localhost:8080/api/simulation/scenarios
```

### 3. 运行测试客户端
```bash
# 编译测试客户端
cd tcp-server
mvn compile exec:java -Dexec.mainClass="com.example.tcpserver.test.TcpProtocolTestClient"
```

## 📊 管理接口

### 服务器管理
- `GET /admin/status` - 服务器状态
- `GET /admin/clients` - 连接客户端
- `GET /admin/protocol/stats` - 协议统计
- `GET /admin/health` - 健康检查
- `GET /admin/system` - 系统信息

### 模拟测试
- `GET /api/simulation/scenarios` - 获取所有场景
- `POST /api/simulation/start/{scenarioName}` - 启动场景
- `POST /api/simulation/stop/{scenarioName}` - 停止场景
- `GET /api/simulation/status` - 场景状态

## ⚙️ 配置说明

### 服务端配置 (application.yml)
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

### 查看场景状态
```bash
# 查看所有场景状态
curl http://localhost:8080/api/simulation/status

# 停止场景
curl -X POST http://localhost:8080/api/simulation/stop/signin_scenario
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
1. 在 `TcpProtocolHandler` 中添加新的处理逻辑
2. 更新配置文件中的场景定义
3. 添加相应的测试用例

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
