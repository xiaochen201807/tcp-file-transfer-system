package com.example.tcpclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端配置服务 - 支持热更新
 */
@Slf4j
@Service
public class ClientConfigService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Object> configCache = new ConcurrentHashMap<>();
    private long lastModified = 0;
    private Path configFilePath;
    
    @PostConstruct
    public void init() {
        try {
            // 优先使用外部配置文件，其次使用内部配置文件
            configFilePath = findConfigFile();
            
            // 初始加载配置
            loadConfig();
            
            log.info("ClientConfigService initialized, config file: {}", configFilePath);
            
        } catch (Exception e) {
            log.error("Failed to initialize ClientConfigService: {}", e.getMessage());
            // 使用默认配置
            loadDefaultConfig();
        }
    }
    
    /**
     * 查找配置文件
     * 优先级：外部配置文件 > 内部配置文件 > 默认配置
     */
    private Path findConfigFile() {
        // 1. 检查外部配置文件（与二进制文件同级目录）
        Path externalConfig = Paths.get("client-config.json");
        if (Files.exists(externalConfig)) {
            log.info("Found external client config file: {}", externalConfig.toAbsolutePath());
            return externalConfig;
        }
        
        // 2. 检查外部配置文件（config目录）
        Path configDirConfig = Paths.get("config/client-config.json");
        if (Files.exists(configDirConfig)) {
            log.info("Found config directory client file: {}", configDirConfig.toAbsolutePath());
            return configDirConfig;
        }
        
        // 3. 使用内部配置文件
        try {
            ClassPathResource resource = new ClassPathResource("client-config.json");
            Path internalConfig = Paths.get(resource.getURI());
            log.info("Using internal client config file: {}", internalConfig);
            return internalConfig;
        } catch (Exception e) {
            log.warn("Internal client config file not found, will use default config");
            return null;
        }
    }
    
    /**
     * 加载配置文件
     */
    private void loadConfig() {
        try {
            if (configFilePath != null && Files.exists(configFilePath)) {
                String content = Files.readString(configFilePath);
                Map<String, Object> config = objectMapper.readValue(content, Map.class);
                configCache.clear();
                configCache.putAll(config);
                lastModified = Files.getLastModifiedTime(configFilePath).toMillis();
                log.info("Client config loaded from file: {}", configFilePath);
            } else {
                log.warn("Client config file not found: {}, using default config", configFilePath);
                loadDefaultConfig();
            }
        } catch (Exception e) {
            log.error("Failed to load client config file: {}", e.getMessage());
            loadDefaultConfig();
        }
    }
    
    /**
     * 加载默认配置
     */
    private void loadDefaultConfig() {
        configCache.clear();
        // 设置默认配置
        Map<String, Object> defaultConfig = new HashMap<>();
        
        // TCP客户端配置
        Map<String, Object> tcpClient = new HashMap<>();
        Map<String, Object> server = new HashMap<>();
        server.put("host", "localhost");
        server.put("port", 8888);
        tcpClient.put("server", server);
        
        Map<String, Object> connect = new HashMap<>();
        connect.put("timeout", 5000);
        tcpClient.put("connect", connect);
        
        Map<String, Object> download = new HashMap<>();
        download.put("directory", "downloads");
        tcpClient.put("download", download);
        
        Map<String, Object> sender = new HashMap<>();
        sender.put("nodeId", "CLIENT");
        tcpClient.put("sender", sender);
        
        defaultConfig.put("tcp", Map.of("client", tcpClient));
        configCache.putAll(defaultConfig);
        
        log.info("Using default client config");
    }
    
    /**
     * 检查并重新加载配置（如果文件被修改）
     */
    private void checkAndReloadConfig() {
        try {
            if (configFilePath != null && Files.exists(configFilePath)) {
                long currentModified = Files.getLastModifiedTime(configFilePath).toMillis();
                if (currentModified > lastModified) {
                    log.info("Client config file modified, reloading...");
                    loadConfig();
                }
            }
        } catch (Exception e) {
            log.error("Failed to check client config file modification: {}", e.getMessage());
        }
    }
    
    /**
     * 获取TCP服务器主机
     */
    public String getTcpServerHost() {
        checkAndReloadConfig();
        Object value = getNestedValue("tcp.client.server.host", "localhost");
        return value != null ? value.toString() : "localhost";
    }
    
    /**
     * 获取TCP服务器端口
     */
    public int getTcpServerPort() {
        checkAndReloadConfig();
        Object port = getNestedValue("tcp.client.server.port", 8888);
        if (port instanceof Number) {
            return ((Number) port).intValue();
        }
        return 8888;
    }
    
    /**
     * 获取连接超时时间
     */
    public int getConnectTimeout() {
        checkAndReloadConfig();
        Object timeout = getNestedValue("tcp.client.connect.timeout", 5000);
        if (timeout instanceof Number) {
            return ((Number) timeout).intValue();
        }
        return 5000;
    }
    
    /**
     * 获取下载目录
     */
    public String getDownloadDirectory() {
        checkAndReloadConfig();
        Object value = getNestedValue("tcp.client.download.directory", "downloads");
        return value != null ? value.toString() : "downloads";
    }
    
    /**
     * 获取发送方节点ID
     */
    public String getSenderNodeId() {
        checkAndReloadConfig();
        Object value = getNestedValue("tcp.client.sender.nodeId", "CLIENT");
        return value != null ? value.toString() : "CLIENT";
    }
    
    /**
     * 手动重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
    }
    
    /**
     * 获取当前配置
     */
    public Map<String, Object> getCurrentConfig() {
        return new HashMap<>(configCache);
    }
    
    /**
     * 获取嵌套值
     */
    private Object getNestedValue(String key, Object defaultValue) {
        String[] keys = key.split("\\.");
        Object current = configCache;
        
        for (String k : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(k);
            } else {
                return defaultValue;
            }
        }
        
        return current != null ? current : defaultValue;
    }
}
