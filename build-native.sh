#!/bin/bash

# TCP Demo Native Build Script
# 构建原生二进制文件

echo "🚀 开始构建 TCP Demo 原生二进制文件..."

# 检查 GraalVM
if ! command -v native-image &> /dev/null; then
    echo "❌ 错误: 未找到 GraalVM native-image 命令"
    echo "请安装 GraalVM JDK 17:"
    echo "1. 从 https://www.graalvm.org/downloads/ 下载"
    echo "2. 或者使用 SDKMAN: sdk install java 17.0.7-graal"
    exit 1
fi

echo "✅ GraalVM 已安装"

# 构建服务端
echo "📦 构建服务端原生二进制文件..."
cd tcp-server
mvn clean package -Pnative -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ 服务端构建成功!"
    echo "📁 二进制文件位置: tcp-server/target/tcp-server-native"
    ls -lh tcp-server/target/tcp-server-native
    
    # 复制外部配置文件到目标目录
    if [ -f "../response-config-external.json" ]; then
        cp ../response-config-external.json tcp-server/target/
        echo "📋 已复制外部配置文件到: tcp-server/target/response-config.json"
    fi
else
    echo "❌ 服务端构建失败!"
    exit 1
fi

# 构建客户端
echo "📦 构建客户端原生二进制文件..."
cd ../tcp-client
mvn clean package -Pnative -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ 客户端构建成功!"
    echo "📁 二进制文件位置: tcp-client/target/tcp-client-native"
    ls -lh tcp-client/target/tcp-client-native
    
    # 复制外部配置文件到目标目录
    if [ -f "../client-config-external.json" ]; then
        cp ../client-config-external.json tcp-client/target/
        echo "📋 已复制外部配置文件到: tcp-client/target/client-config.json"
    fi
else
    echo "❌ 客户端构建失败!"
    exit 1
fi

echo ""
echo "🎉 所有原生二进制文件构建完成!"
echo ""
echo "📊 文件大小对比:"
echo "服务端: $(ls -lh tcp-server/target/tcp-server-native | awk '{print $5}')"
echo "客户端: $(ls -lh tcp-client/target/tcp-client-native | awk '{print $5}')"
echo ""
echo "🚀 运行方式:"
echo "服务端: ./tcp-server/target/tcp-server-native"
echo "客户端: ./tcp-client/target/tcp-client-native"
echo ""
echo "💡 优势:"
echo "- 启动速度: 毫秒级启动 (vs 秒级)"
echo "- 内存占用: 减少 50-80%"
echo "- 无需 JVM: 直接运行二进制文件"
echo "- 部署简单: 单个文件即可运行"
