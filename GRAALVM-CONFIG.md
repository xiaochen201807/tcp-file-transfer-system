# GraalVM Native Image 配置说明

## 📋 概述

本文档说明了TCP文件传输系统的GraalVM Native Image配置，包括反射配置、资源访问配置和JNI配置。

## 🔧 配置文件

### 1. 反射配置 (`reflect-config.json`)

**位置**: `src/main/resources/META-INF/native-image/reflect-config.json`

**作用**: 告诉GraalVM哪些类需要在运行时通过反射访问

**包含的类**:
- 应用程序主类 (`TcpServerApplication`, `TcpClientApplication`)
- Netty相关类 (`TcpServer`, `TcpProtocolHandler`, `TcpProtocolEncoder`, `TcpProtocolDecoder`)
- 协议类 (`TcpProtocol`, `TcpProtocol$Message`)
- 服务类 (`ResponseConfigService`, `ClientConfigService`, `FileService`)
- 控制器类 (`AdminController`, `HomeController`, `TcpProtocolController`)
- Jackson JSON处理类 (`ObjectMapper`, `JsonNode`, `ObjectNode`, `ArrayNode`)
- Java标准库类 (`HashMap`, `ArrayList`, `List`, `File`, `Path`, `Paths`)

### 2. 资源访问配置 (`resource-config.json`)

**位置**: `src/main/resources/META-INF/native-image/resource-config.json`

**作用**: 指定哪些资源文件需要包含在native image中

**包含的资源**:
- 配置文件: `application.yml`, `response-config.json`, `client-config.json`
- 外部配置文件: `response-config-external.json`, `client-config-external.json`
- 模板文件: `templates/*`
- 静态资源: `static/*`

### 3. JNI配置 (`jni-config.json`)

**位置**: `src/main/resources/META-INF/native-image/jni-config.json`

**作用**: 配置JNI (Java Native Interface) 访问

**配置内容**:
- 允许所有声明的类、构造函数、方法和字段的JNI访问

## ⚙️ Maven配置

### GraalVM Native Image插件配置

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
    <version>0.9.28</version>
    <extensions>true</extensions>
    <executions>
        <execution>
            <id>build-native</id>
            <goals>
                <goal>compile-no-fork</goal>
            </goals>
            <phase>package</phase>
        </execution>
    </executions>
    <configuration>
        <imageName>tcp-server-native</imageName>
        <mainClass>com.example.tcpserver.TcpServerApplication</mainClass>
        <buildArgs>
            <buildArg>--no-fallback</buildArg>
            <buildArg>--enable-http</buildArg>
            <buildArg>--enable-https</buildArg>
            <buildArg>--initialize-at-build-time=io.netty</buildArg>
            <buildArg>--initialize-at-run-time=com.example.tcpserver</buildArg>
            <buildArg>--trace-class-initialization=io.netty</buildArg>
            <buildArg>--allow-incomplete-classpath</buildArg>
            <buildArg>--report-unsupported-elements-at-runtime</buildArg>
            <buildArg>--enable-monitoring=heapdump,jfr</buildArg>
            <buildArg>--enable-url-protocols=http,https</buildArg>
            <buildArg>--enable-all-security-services</buildArg>
        </buildArgs>
    </configuration>
</plugin>
```

### 构建参数说明

| 参数 | 说明 |
|------|------|
| `-H:ReflectionConfigurationFiles` | 指定反射配置文件路径 |
| `-H:ResourceConfigurationFiles` | 指定资源访问配置文件路径 |
| `-H:JNIConfigurationFiles` | 指定JNI配置文件路径 |
| `--enable-http` | 启用HTTP协议支持 |
| `--enable-https` | 启用HTTPS协议支持 |
| `--initialize-at-build-time=io.netty` | 在构建时初始化Netty类 |
| `--initialize-at-run-time=com.example.tcpserver` | 在运行时初始化应用程序类 |
| `--trace-class-initialization=io.netty` | 跟踪Netty类的初始化 |
| `--allow-incomplete-classpath` | 允许不完整的类路径 |
| `--report-unsupported-elements-at-runtime` | 在运行时报告不支持的元素 |

**注意**: 移除了 `--no-fallback` 参数，使用fallback模式提高兼容性。

## 🚀 构建命令

### 本地构建

```bash
# 构建服务端
cd tcp-server
mvn package -Pnative -DskipTests

# 构建客户端
cd tcp-client
mvn package -Pnative -DskipTests
```

### GitHub Actions构建

工作流会自动使用这些配置文件进行构建，无需额外配置。

## 🔍 故障排除

### 常见问题

1. **反射访问错误**
   - 确保所有需要反射访问的类都在 `reflect-config.json` 中
   - 检查类名是否正确

2. **资源访问错误**
   - 确保所有需要的资源文件都在 `resource-config.json` 中
   - 检查资源路径是否正确

3. **JNI访问错误**
   - 确保JNI配置正确
   - 检查是否需要额外的JNI配置

### 调试技巧

1. **启用详细日志**
   ```bash
   mvn package -Pnative -DskipTests -X
   ```

2. **检查生成的配置文件**
   - 查看 `target/graalvm-reachability-metadata/` 目录
   - 检查生成的配置文件是否正确

3. **使用GraalVM工具**
   ```bash
   native-image --help
   native-image --version
   ```

## 📚 相关文档

- [GraalVM Native Image文档](https://www.graalvm.org/latest/reference-manual/native-image/)
- [Spring Boot Native Image支持](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html)
- [Netty Native Image支持](https://netty.io/wiki/native-transports.html)

## 🔄 更新日志

- **2025-09-25**: 初始创建
  - 添加反射配置文件
  - 添加资源访问配置文件
  - 添加JNI配置文件
  - 优化Maven构建参数
- **2025-09-25**: 进一步优化
  - 移除 `--no-fallback` 参数，使用fallback模式
  - 添加Spring Boot Native支持依赖
  - 明确指定配置文件路径
  - 简化构建参数
