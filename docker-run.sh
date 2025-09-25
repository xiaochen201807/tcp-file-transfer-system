#!/bin/bash
# Docker运行脚本

echo "🚀 启动TCP Demo原生版本..."

# 启动服务端
echo "🖥️ 启动服务端..."
./tcp-server/target/tcp-server-native &

# 等待服务端启动
sleep 3

# 检查服务端是否启动成功
if curl -s http://localhost:8080/admin/status > /dev/null; then
    echo "✅ 服务端启动成功"
else
    echo "❌ 服务端启动失败"
    exit 1
fi

# 启动客户端
echo "📱 启动客户端..."
./tcp-client/target/tcp-client-native &

# 等待客户端启动
sleep 2

echo ""
echo "🎉 TCP Demo原生版本启动完成!"
echo ""
echo "🌐 访问地址:"
echo "服务端管理: http://localhost:8080/admin/status"
echo "客户端界面: http://localhost:8081"
echo ""
echo "💡 原生版本优势:"
echo "- 启动时间: ~100ms (vs ~3s)"
echo "- 内存占用: ~50MB (vs ~200MB)"
echo "- 无需JVM环境"
echo ""

# 保持容器运行
tail -f /dev/null
