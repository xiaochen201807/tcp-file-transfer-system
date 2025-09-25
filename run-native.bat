@echo off
chcp 65001 >nul
REM TCP Demo Native Run Script for Windows
REM 运行原生二进制文件

echo 🚀 启动 TCP Demo 原生版本...

REM 检查二进制文件是否存在
if not exist "tcp-server\target\tcp-server-native.exe" (
    echo ❌ 错误: 服务端二进制文件不存在
    echo 请先运行: build-native.bat
    pause
    exit /b 1
)

if not exist "tcp-client\target\tcp-client-native.exe" (
    echo ❌ 错误: 客户端二进制文件不存在
    echo 请先运行: build-native.bat
    pause
    exit /b 1
)

echo ✅ 二进制文件检查通过

REM 启动服务端
echo 🖥️  启动服务端...
start "TCP Server" tcp-server\target\tcp-server-native.exe

REM 等待服务端启动
timeout /t 3 /nobreak >nul

REM 检查服务端是否启动成功
curl -s http://localhost:8080/admin/status >nul 2>nul
if %errorlevel% equ 0 (
    echo ✅ 服务端启动成功
) else (
    echo ❌ 服务端启动失败
    pause
    exit /b 1
)

REM 启动客户端
echo 📱 启动客户端...
start "TCP Client" tcp-client\target\tcp-client-native.exe

REM 等待客户端启动
timeout /t 2 /nobreak >nul

echo.
echo 🎉 TCP Demo 原生版本启动完成!
echo.
echo 🌐 访问地址:
echo 服务端管理: http://localhost:8080/admin/status
echo 客户端界面: http://localhost:8081
echo.
echo 💡 原生版本优势:
echo - 启动时间: ~100ms (vs ~3s)
echo - 内存占用: ~50MB (vs ~200MB)
echo - 无需 JVM 环境
echo.
echo ⏹️  停止服务: 关闭对应的命令行窗口

pause
