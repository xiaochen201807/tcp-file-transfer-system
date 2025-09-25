# Podman Native Build Guide

## 🐳 使用Podman构建原生二进制文件

本项目支持使用Podman Desktop构建GraalVM原生二进制文件，无需安装Visual Studio Build Tools。

### 📋 前置条件

1. **安装Podman Desktop**
   - 下载地址：https://podman-desktop.io/
   - 安装完成后确保Podman命令可用

2. **验证安装**
   ```bash
   podman --version
   ```

### 🚀 快速开始

#### 方法1：使用批处理脚本（推荐）

```bash
# Windows
build-podman-native.bat
```

#### 方法2：手动命令

```bash
# 构建镜像
podman build -f Dockerfile.native -t tcp-demo-native .

# 运行容器
podman run -it --rm -p 8080:8080 -p 8081:8081 tcp-demo-native
```

### 🔧 构建过程

1. **下载GraalVM基础镜像**
   - 使用 `ghcr.io/graalvm/graalvm-community:17`
   - 自动安装 `native-image` 工具

2. **构建服务端**
   - 在容器内执行 `mvn clean package -Pnative -DskipTests`
   - 生成 `tcp-server-native` 二进制文件

3. **构建客户端**
   - 在容器内执行 `mvn clean package -Pnative -DskipTests`
   - 生成 `tcp-client-native` 二进制文件

4. **启动服务**
   - 自动启动服务端和客户端
   - 暴露端口：8080（服务端）、8081（客户端）

### 🌐 访问地址

- **服务端管理界面**：http://localhost:8080/admin/status
- **客户端Web界面**：http://localhost:8081

### 💡 Podman vs Docker

| 特性 | Podman | Docker |
|------|--------|--------|
| 安全性 | 无守护进程，更安全 | 需要守护进程 |
| 权限 | 无需root权限 | 需要root权限 |
| 兼容性 | 与Docker命令兼容 | 原生支持 |
| 资源占用 | 更低 | 相对较高 |

### 🛠️ 故障排除

#### 问题1：Podman命令未找到
```bash
# 解决方案：确保Podman Desktop已正确安装并添加到PATH
podman --version
```

#### 问题2：端口冲突
```bash
# 解决方案：修改端口映射
podman run -it --rm -p 9080:8080 -p 9081:8081 tcp-demo-native
```

#### 问题3：构建失败
```bash
# 解决方案：清理并重新构建
podman rmi tcp-demo-native
podman build -f Dockerfile.native -t tcp-demo-native . --no-cache
```

### 📁 文件说明

- `Dockerfile.native` - Podman构建文件
- `build-podman-native.bat` - Windows批处理脚本
- `docker-run.sh` - 容器内启动脚本
- `.mvn/` - Maven Wrapper文件

### 🎯 优势

1. **无需Visual Studio** - 避免安装复杂的C++构建工具
2. **环境一致性** - 容器化构建确保环境一致
3. **快速部署** - 构建完成后可直接运行
4. **资源隔离** - 容器化运行，不影响宿主机环境

### 🔄 更新说明

- 2025-09-25：添加Podman支持，替换Docker方案
- 2025-09-25：优化构建脚本，添加错误检查
- 2025-09-25：添加Maven Wrapper支持
