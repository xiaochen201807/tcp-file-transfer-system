#!/bin/bash

# TCP Demo Native Run Script
# 运行原生二进制文件

echo "🚀 启动 TCP Demo 原生版本..."

# 检查二进制文件是否存在
if [ ! -f "tcp-server/target/tcp-server-native" ]; then
    echo "❌ 错误: 服务端二进制文件不存在"
    echo "请先运行: ./build-native.sh"
    exit 1
fi

if [ ! -f "tcp-client/target/tcp-client-native" ]; then
    echo "❌ 错误: 客户端二进制文件不存在"
    echo "请先运行: ./build-native.sh"
    exit 1
fi

echo "✅ 二进制文件检查通过"

# 启动服务端
echo "🖥️  启动服务端..."
./tcp-server/target/tcp-server-native &
SERVER_PID=$!

# 等待服务端启动
sleep 3

# 检查服务端是否启动成功
if curl -s http://localhost:8080/admin/status > /dev/null; then
    echo "✅ 服务端启动成功 (PID: $SERVER_PID)"
else
    echo "❌ 服务端启动失败"
    kill $SERVER_PID 2>/dev/null
    exit 1
fi

# 启动客户端
echo "📱 启动客户端..."
./tcp-client/target/tcp-client-native &
CLIENT_PID=$!

# 等待客户端启动
sleep 2

echo ""
echo "🎉 TCP Demo 原生版本启动完成!"
echo ""
echo "📊 进程信息:"
echo "服务端 PID: $SERVER_PID"
echo "客户端 PID: $CLIENT_PID"
echo ""
echo "🌐 访问地址:"
echo "服务端管理: http://localhost:8080/admin/status"
echo "客户端界面: http://localhost:8081"
echo ""
echo "⏹️  停止服务:"
echo "kill $SERVER_PID $CLIENT_PID"
echo ""
echo "💡 原生版本优势:"
echo "- 启动时间: ~100ms (vs ~3s)"
echo "- 内存占用: ~50MB (vs ~200MB)"
echo "- 无需 JVM 环境"

# 等待用户输入
read -p "按 Enter 键停止服务..."
kill $SERVER_PID $CLIENT_PID 2>/dev/null
echo "🛑 服务已停止"
