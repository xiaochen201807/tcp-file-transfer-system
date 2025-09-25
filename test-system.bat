@echo off
chcp 65001 >nul
echo ==========================================
echo TCP协议系统测试脚本 v2.0
echo ==========================================

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Java环境，请先安装Java 17+
    pause
    exit /b 1
)

REM 检查Maven环境
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Maven环境，请先安装Maven 3.6+
    pause
    exit /b 1
)

echo 环境检查通过 ✓

REM 启动服务端
echo.
echo 启动TCP服务端...
cd tcp-server
start /B mvn spring-boot:run
cd ..

REM 等待服务端启动
echo 等待服务端启动...
timeout /t 15 /nobreak >nul

REM 检查服务端是否启动成功
curl -s http://localhost:8080/admin/health >nul 2>&1
if errorlevel 1 (
    echo 错误: 服务端启动失败
    pause
    exit /b 1
)

echo 服务端启动成功 ✓

REM 启动客户端
echo.
echo 启动TCP客户端...
cd tcp-client
start /B mvn spring-boot:run
cd ..

REM 等待客户端启动
echo 等待客户端启动...
timeout /t 15 /nobreak >nul

REM 检查客户端是否启动成功
curl -s http://localhost:8081/api/tcp/status >nul 2>&1
if errorlevel 1 (
    echo 错误: 客户端启动失败
    pause
    exit /b 1
)

echo 客户端启动成功 ✓

REM 测试API接口
echo.
echo 开始API测试...

REM 测试连接状态
echo 1. 测试连接状态...
curl -s http://localhost:8081/api/tcp/status

REM 测试签到请求
echo.
echo 2. 测试签到请求...
curl -s -X POST http://localhost:8081/api/tcp/signin -H "Content-Type: application/json" -d "{\"userData\": \"user:admin,password:123456\"}"

REM 测试查询请求
echo.
echo 3. 测试查询请求...
curl -s -X POST http://localhost:8081/api/tcp/query -H "Content-Type: application/json" -d "{\"queryData\": \"SELECT * FROM users WHERE id=1\"}"

REM 测试更新请求
echo.
echo 4. 测试更新请求...
curl -s -X POST http://localhost:8081/api/tcp/update -H "Content-Type: application/json" -d "{\"updateData\": \"UPDATE users SET name='test' WHERE id=1\"}"

REM 测试删除请求
echo.
echo 5. 测试删除请求...
curl -s -X POST http://localhost:8081/api/tcp/delete -H "Content-Type: application/json" -d "{\"deleteData\": \"DELETE FROM users WHERE id=1\"}"

REM 测试自定义业务请求
echo.
echo 6. 测试自定义业务请求...
curl -s -X POST http://localhost:8081/api/tcp/business -H "Content-Type: application/json" -d "{\"transactionCode\": \"CUSTOM\", \"data\": \"Custom transaction data\"}"

REM 测试错误请求
echo.
echo 7. 测试错误请求...
curl -s -X POST http://localhost:8081/api/tcp/business -H "Content-Type: application/json" -d "{\"transactionCode\": \"UNKNOWN\", \"data\": \"Unknown transaction\"}"

REM 测试保存响应数据
echo.
echo 8. 测试保存响应数据...
curl -s -X POST http://localhost:8081/api/tcp/save -H "Content-Type: application/json" -d "{\"fileName\": \"test_response.txt\", \"data\": \"Test response data\"}"

REM 查看服务端统计
echo.
echo 9. 查看服务端统计...
curl -s http://localhost:8080/admin/protocol/stats

REM 查看服务端状态
echo.
echo 10. 查看服务端状态...
curl -s http://localhost:8080/admin/status

echo.
echo ==========================================
echo 测试完成！
echo ==========================================
echo.
echo 服务端: http://localhost:8080
echo 客户端: http://localhost:8081
echo.
echo 按任意键退出...
pause >nul
