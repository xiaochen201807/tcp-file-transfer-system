#!/bin/bash

# TCP协议系统测试脚本

echo "=========================================="
echo "TCP协议系统测试脚本 v2.0"
echo "=========================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java 17+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven 3.6+"
    exit 1
fi

echo "环境检查通过 ✓"

# 启动服务端
echo ""
echo "启动TCP服务端..."
cd tcp-server
mvn spring-boot:run &
SERVER_PID=$!
cd ..

# 等待服务端启动
echo "等待服务端启动..."
sleep 10

# 检查服务端是否启动成功
if ! curl -s http://localhost:8080/admin/health > /dev/null; then
    echo "错误: 服务端启动失败"
    kill $SERVER_PID 2>/dev/null
    exit 1
fi

echo "服务端启动成功 ✓"

# 启动客户端
echo ""
echo "启动TCP客户端..."
cd tcp-client
mvn spring-boot:run &
CLIENT_PID=$!
cd ..

# 等待客户端启动
echo "等待客户端启动..."
sleep 10

# 检查客户端是否启动成功
if ! curl -s http://localhost:8081/api/tcp/status > /dev/null; then
    echo "错误: 客户端启动失败"
    kill $SERVER_PID $CLIENT_PID 2>/dev/null
    exit 1
fi

echo "客户端启动成功 ✓"

# 测试API接口
echo ""
echo "开始API测试..."

# 测试连接状态
echo "1. 测试连接状态..."
curl -s http://localhost:8081/api/tcp/status | jq '.'

# 测试签到请求
echo ""
echo "2. 测试签到请求..."
curl -s -X POST http://localhost:8081/api/tcp/signin \
  -H "Content-Type: application/json" \
  -d '{"userData": "user:admin,password:123456"}' | jq '.'

# 测试查询请求
echo ""
echo "3. 测试查询请求..."
curl -s -X POST http://localhost:8081/api/tcp/query \
  -H "Content-Type: application/json" \
  -d '{"queryData": "SELECT * FROM users WHERE id=1"}' | jq '.'

# 测试更新请求
echo ""
echo "4. 测试更新请求..."
curl -s -X POST http://localhost:8081/api/tcp/update \
  -H "Content-Type: application/json" \
  -d '{"updateData": "UPDATE users SET name='\''test'\'' WHERE id=1"}' | jq '.'

# 测试删除请求
echo ""
echo "5. 测试删除请求..."
curl -s -X POST http://localhost:8081/api/tcp/delete \
  -H "Content-Type: application/json" \
  -d '{"deleteData": "DELETE FROM users WHERE id=1"}' | jq '.'

# 测试自定义业务请求
echo ""
echo "6. 测试自定义业务请求..."
curl -s -X POST http://localhost:8081/api/tcp/business \
  -H "Content-Type: application/json" \
  -d '{"transactionCode": "CUSTOM", "data": "Custom transaction data"}' | jq '.'

# 测试错误请求
echo ""
echo "7. 测试错误请求..."
curl -s -X POST http://localhost:8081/api/tcp/business \
  -H "Content-Type: application/json" \
  -d '{"transactionCode": "UNKNOWN", "data": "Unknown transaction"}' | jq '.'

# 测试保存响应数据
echo ""
echo "8. 测试保存响应数据..."
curl -s -X POST http://localhost:8081/api/tcp/save \
  -H "Content-Type: application/json" \
  -d '{"fileName": "test_response.txt", "data": "Test response data"}' | jq '.'

# 查看服务端统计
echo ""
echo "9. 查看服务端统计..."
curl -s http://localhost:8080/admin/protocol/stats | jq '.'

# 查看服务端状态
echo ""
echo "10. 查看服务端状态..."
curl -s http://localhost:8080/admin/status | jq '.'

echo ""
echo "=========================================="
echo "测试完成！"
echo "=========================================="

# 询问是否停止服务
echo ""
read -p "是否停止服务？(y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "停止服务..."
    kill $SERVER_PID $CLIENT_PID 2>/dev/null
    echo "服务已停止"
else
    echo "服务继续运行..."
    echo "服务端: http://localhost:8080"
    echo "客户端: http://localhost:8081"
    echo ""
    echo "停止服务命令:"
    echo "kill $SERVER_PID $CLIENT_PID"
fi
