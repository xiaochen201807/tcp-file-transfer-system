# TCP协议客户端 v2.0

这是一个基于Java Spring Boot和Netty的TCP协议客户端，支持与服务端进行自定义二进制协议通信。

## 🚀 功能特性

### 核心功能
- **TCP协议通信** - 支持自定义二进制协议
- **签到交易** - 用户身份验证
- **业务交易** - 支持查询、更新、删除等业务操作
- **REST API** - 提供HTTP接口操作
- **自动测试** - 启动时自动执行测试用例

### 协议支持
- **请求报文** - 42字节header + 4字节length + data
- **响应报文** - 2字节header + 4字节length + data
- **交易类型** - 签到交易(1) 和 业务交易(0)
- **业务交易码** - QUERY、UPDATE、DELETE

## 🛠️ 快速开始

### 1. 启动服务端
```bash
cd tcp-server
mvn spring-boot:run
```

### 2. 启动客户端
```bash
cd tcp-client
mvn spring-boot:run
```

客户端将在8081端口启动HTTP服务器，并自动连接到服务端进行测试。

### 3. 查看测试结果
客户端启动后会自动执行以下测试：
- 连接到服务器
- 发送签到请求
- 发送查询请求
- 发送更新请求
- 发送删除请求
- 发送错误请求
- 断开连接

## 📊 REST API接口

### 连接管理
- `POST /api/tcp/connect` - 连接到服务器
- `POST /api/tcp/disconnect` - 断开连接
- `GET /api/tcp/status` - 检查连接状态

### 业务操作
- `POST /api/tcp/signin` - 发送签到请求
- `POST /api/tcp/query` - 发送查询请求
- `POST /api/tcp/update` - 发送更新请求
- `POST /api/tcp/delete` - 发送删除请求
- `POST /api/tcp/business` - 发送自定义业务请求
- `POST /api/tcp/save` - 保存响应数据到文件

## 🧪 API使用示例

### 连接到服务器
```bash
curl -X POST http://localhost:8081/api/tcp/connect
```

### 发送签到请求
```bash
curl -X POST http://localhost:8081/api/tcp/signin \
  -H "Content-Type: application/json" \
  -d '{"userData": "user:admin,password:123456"}'
```

### 发送查询请求
```bash
curl -X POST http://localhost:8081/api/tcp/query \
  -H "Content-Type: application/json" \
  -d '{"queryData": "SELECT * FROM users WHERE id=1"}'
```

### 发送更新请求
```bash
curl -X POST http://localhost:8081/api/tcp/update \
  -H "Content-Type: application/json" \
  -d '{"updateData": "UPDATE users SET name='test' WHERE id=1"}'
```

### 发送删除请求
```bash
curl -X POST http://localhost:8081/api/tcp/delete \
  -H "Content-Type: application/json" \
  -d '{"deleteData": "DELETE FROM users WHERE id=1"}'
```

### 发送自定义业务请求
```bash
curl -X POST http://localhost:8081/api/tcp/business \
  -H "Content-Type: application/json" \
  -d '{"transactionCode": "CUSTOM", "data": "Custom transaction data"}'
```

### 保存响应数据
```bash
curl -X POST http://localhost:8081/api/tcp/save \
  -H "Content-Type: application/json" \
  -d '{"fileName": "response.txt", "data": "Response data content"}'
```

## ⚙️ 配置说明

### 客户端配置 (application.yml)
```yaml
server:
  port: 8081

tcp:
  client:
    server:
      host: localhost    # 服务器地址
      port: 8888        # 服务器端口
    connect:
      timeout: 5000     # 连接超时时间
    download:
      directory: downloads  # 下载目录
    sender:
      nodeId: CLIENT    # 发送节点号
```

## 📈 响应格式

所有API接口都返回统一的响应格式：

### 成功响应
```json
{
  "success": true,
  "status": 0,
  "data": "Response data content"
}
```

### 失败响应
```json
{
  "success": false,
  "error": "Error message"
}
```

## 🔧 开发说明

### 项目结构
```
tcp-client/
├── src/main/java/com/example/tcpclient/
│   ├── client/           # TCP客户端连接
│   ├── codec/           # 消息编解码器
│   ├── controller/      # REST控制器
│   ├── handler/         # 消息处理器
│   ├── protocol/        # 协议定义
│   ├── service/         # 业务服务
│   └── test/           # 测试类
├── src/main/resources/
│   └── application.yml  # 配置文件
└── pom.xml             # Maven配置
```

### 核心类说明
- **TcpClient** - TCP客户端连接管理
- **TcpProtocol** - 协议定义和消息处理
- **TcpProtocolDecoder/Encoder** - 消息编解码
- **ClientHandler** - 消息处理器
- **TcpProtocolClientService** - 业务服务
- **TcpProtocolController** - REST API控制器

## 🐛 故障排除

1. **连接失败** - 检查服务端是否启动，端口是否正确
2. **协议解析错误** - 检查报文格式是否符合规范
3. **请求超时** - 调整连接超时时间配置
4. **响应错误** - 检查服务端日志和状态

## 📝 更新日志

### v2.0.0
- 实现新的TCP协议格式（header+length+data）
- 支持签到交易和业务交易
- 提供完整的REST API接口
- 添加自动测试功能
- 支持响应数据保存

### v1.0.0
- 基础文件传输功能
- 简单的二进制协议
- HTTP管理接口

## 🤝 与服务端配合

客户端与服务端使用相同的协议格式，确保通信兼容性：

1. **协议版本** - 双方都使用v2.0协议
2. **消息格式** - header+length+data格式
3. **交易类型** - 支持签到和业务交易
4. **状态码** - 统一的成功/失败状态码

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
