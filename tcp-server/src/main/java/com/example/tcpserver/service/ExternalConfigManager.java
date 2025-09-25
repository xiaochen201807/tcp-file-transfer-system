package com.example.tcpserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * 外部配置管理工具
 */
@Slf4j
@Component
public class ExternalConfigManager {
    
    /**
     * 创建外部配置文件
     */
    public void createExternalConfig(String content) {
        try {
            // 创建 config 目录
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                log.info("Created config directory: {}", configDir.toAbsolutePath());
            }
            
            // 创建配置文件
            Path configFile = configDir.resolve("response-config.json");
            Files.write(configFile, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Created external config file: {}", configFile.toAbsolutePath());
            
        } catch (IOException e) {
            log.error("Failed to create external config file: {}", e.getMessage());
        }
    }
    
    /**
     * 检查外部配置文件是否存在
     */
    public boolean hasExternalConfig() {
        Path externalConfig = Paths.get("response-config.json");
        Path configDirConfig = Paths.get("config/response-config.json");
        return Files.exists(externalConfig) || Files.exists(configDirConfig);
    }
    
    /**
     * 获取外部配置文件路径
     */
    public Path getExternalConfigPath() {
        Path externalConfig = Paths.get("response-config.json");
        if (Files.exists(externalConfig)) {
            return externalConfig;
        }
        
        Path configDirConfig = Paths.get("config/response-config.json");
        if (Files.exists(configDirConfig)) {
            return configDirConfig;
        }
        
        return null;
    }
    
    /**
     * 获取配置信息
     */
    public Map<String, Object> getConfigInfo() {
        Map<String, Object> info = new HashMap<>();
        
        Path configPath = getExternalConfigPath();
        if (configPath != null) {
            info.put("hasExternalConfig", true);
            info.put("configPath", configPath.toAbsolutePath().toString());
            try {
                info.put("lastModified", Files.getLastModifiedTime(configPath).toMillis());
                info.put("fileSize", Files.size(configPath));
            } catch (IOException e) {
                log.warn("Failed to get file info: {}", e.getMessage());
            }
        } else {
            info.put("hasExternalConfig", false);
            info.put("configPath", "Using internal config");
        }
        
        return info;
    }
}
