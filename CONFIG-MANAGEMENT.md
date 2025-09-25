# 📋 配置文件管理说明

## 🔍 **配置文件优先级**

原生二进制文件支持以下配置文件加载顺序：

### 服务端配置文件
1. **外部配置文件** (最高优先级)
   - `./response-config.json` - 与二进制文件同级目录
   - `./config/response-config.json` - config目录下

2. **内部配置文件** (默认)
   - 打包在二进制文件内的 `response-config.json`

3. **默认配置** (兜底)
   - 代码中硬编码的默认配置

### 客户端配置文件
1. **外部配置文件** (最高优先级)
   - `./client-config.json` - 与二进制文件同级目录
   - `./config/client-config.json` - config目录下

2. **内部配置文件** (默认)
   - 打包在二进制文件内的 `client-config.json`

3. **默认配置** (兜底)
   - 代码中硬编码的默认配置

## 🛠️ **使用方法**

### 服务端配置

#### 方式一：手动创建外部配置文件
```bash
# 1. 复制示例配置文件
cp response-config-external.json response-config.json

# 2. 修改配置文件内容
vim response-config.json

# 3. 启动服务（会自动检测外部配置）
./tcp-server/target/tcp-server-native
```

### 客户端配置

#### 方式一：手动创建外部配置文件
```bash
# 1. 复制示例配置文件
cp client-config-external.json client-config.json

# 2. 修改配置文件内容
vim client-config.json

# 3. 启动客户端（会自动检测外部配置）
./tcp-client/target/tcp-client-native
```

### 方式二：通过管理接口创建

```bash
# 1. 启动服务
./tcp-server/target/tcp-server-native

# 2. 通过API创建外部配置文件
curl -X POST http://localhost:8080/admin/config/create-external \
  -H "Content-Type: application/json" \
  -d '{"content": "{\"signin\":{\"success\":{\"status\":0,\"template\":\"🎉 签到成功！时间：{timestamp}\"}}}"}'
```

### 方式三：使用config目录

```bash
# 1. 创建config目录
mkdir config

# 2. 复制配置文件到config目录
cp response-config-external.json config/response-config.json

# 3. 启动服务
./tcp-server/target/tcp-server-native
```

## 🔄 **热更新支持**

外部配置文件支持热更新：

1. **自动检测** - 每次请求时自动检查文件修改时间
2. **自动重载** - 文件被修改后自动重新加载配置
3. **手动重载** - 通过API手动触发重载

```bash
# 手动重载配置
curl -X POST http://localhost:8080/admin/config/reload
```

## 📊 **配置信息查询**

### 服务端配置管理
```bash
# 查看当前配置信息
curl http://localhost:8080/admin/config/info

# 查看当前响应配置
curl http://localhost:8080/admin/config/response

# 手动重载配置
curl -X POST http://localhost:8080/admin/config/reload
```

### 客户端配置管理
```bash
# 查看当前配置信息
curl http://localhost:8081/api/config/info

# 查看当前配置
curl http://localhost:8081/api/config/current

# 手动重载配置
curl -X POST http://localhost:8081/api/config/reload
```

## 🎯 **配置示例**

### 服务端配置示例

#### 基础配置
```json
{
  "signin": {
    "success": {
      "status": 0,
      "template": "Sign in successful at {timestamp}"
    },
    "failed": {
      "status": 1,
      "template": "Sign in failed: {reason}"
    }
  }
}
```

#### 带表情符号的配置
```json
{
  "signin": {
    "success": {
      "status": 0,
      "template": "🎉 签到成功！时间：{timestamp}"
    },
    "failed": {
      "status": 1,
      "template": "❌ 签到失败：{reason}"
    }
  }
}
```

### 客户端配置示例

#### 基础配置
```json
{
  "tcp": {
    "client": {
      "server": {
        "host": "localhost",
        "port": 8888
      },
      "connect": {
        "timeout": 5000
      },
      "download": {
        "directory": "downloads"
      },
      "sender": {
        "nodeId": "CLIENT"
      }
    }
  }
}
```

#### 生产环境配置
```json
{
  "tcp": {
    "client": {
      "server": {
        "host": "192.168.1.100",
        "port": 8888
      },
      "connect": {
        "timeout": 10000
      },
      "download": {
        "directory": "/opt/tcp-client/downloads"
      },
      "sender": {
        "nodeId": "PROD_CLIENT"
      }
    }
  }
}
```

## 🔧 **变量支持**

配置模板支持以下变量：

- `{timestamp}` - 当前时间戳
- `{data}` - 请求数据
- `{reason}` - 错误原因
- `{code}` - 交易码
- `{type}` - 交易类型

## ⚠️ **注意事项**

1. **文件格式** - 必须是有效的JSON格式
2. **文件权限** - 确保二进制文件有读取权限
3. **文件编码** - 建议使用UTF-8编码
4. **备份配置** - 修改前建议备份原配置
5. **测试验证** - 修改后建议测试验证

## 🚀 **部署建议**

### 生产环境
```bash
# 1. 创建配置目录
mkdir -p /opt/tcp-server/config

# 2. 复制配置文件
cp response-config.json /opt/tcp-server/config/

# 3. 设置权限
chmod 644 /opt/tcp-server/config/response-config.json

# 4. 启动服务
cd /opt/tcp-server
./tcp-server-native
```

### Docker环境
```dockerfile
# Dockerfile
FROM scratch
COPY tcp-server-native /
COPY response-config.json /
CMD ["/tcp-server-native"]
```

### Kubernetes环境
```yaml
# ConfigMap
apiVersion: v1
kind: ConfigMap
metadata:
  name: tcp-server-config
data:
  response-config.json: |
    {
      "signin": {
        "success": {
          "status": 0,
          "template": "🎉 签到成功！时间：{timestamp}"
        }
      }
    }
---
# Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tcp-server
spec:
  template:
    spec:
      containers:
      - name: tcp-server
        image: tcp-server:latest
        volumeMounts:
        - name: config
          mountPath: /config
      volumes:
      - name: config
        configMap:
          name: tcp-server-config
```
