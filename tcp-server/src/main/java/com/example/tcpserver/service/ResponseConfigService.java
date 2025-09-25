package com.example.tcpserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 响应配置服务 - 支持热更新
 */
@Slf4j
@Service
public class ResponseConfigService {
    
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
            
            log.info("ResponseConfigService initialized, config file: {}", configFilePath);
            
        } catch (Exception e) {
            log.error("Failed to initialize ResponseConfigService: {}", e.getMessage());
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
        Path externalConfig = Paths.get("response-config.json");
        if (Files.exists(externalConfig)) {
            log.info("Found external config file: {}", externalConfig.toAbsolutePath());
            return externalConfig;
        }
        
        // 2. 检查外部配置文件（config目录）
        Path configDirConfig = Paths.get("config/response-config.json");
        if (Files.exists(configDirConfig)) {
            log.info("Found config directory file: {}", configDirConfig.toAbsolutePath());
            return configDirConfig;
        }
        
        // 3. 使用内部配置文件
        try {
            ClassPathResource resource = new ClassPathResource("response-config.json");
            Path internalConfig = Paths.get(resource.getURI());
            log.info("Using internal config file: {}", internalConfig);
            return internalConfig;
        } catch (Exception e) {
            log.warn("Internal config file not found, will use default config");
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
                log.info("Response config loaded from file: {}", configFilePath);
            } else {
                log.warn("Config file not found: {}, using default config", configFilePath);
                loadDefaultConfig();
            }
        } catch (Exception e) {
            log.error("Failed to load config file: {}", e.getMessage());
            loadDefaultConfig();
        }
    }
    
    /**
     * 加载默认配置
     */
    private void loadDefaultConfig() {
        configCache.clear();
        // 这里可以设置一些默认配置
        log.info("Using default response config");
    }
    
    /**
     * 检查并重新加载配置（如果文件被修改）
     */
    private void checkAndReloadConfig() {
        try {
            if (configFilePath != null && Files.exists(configFilePath)) {
                long currentModified = Files.getLastModifiedTime(configFilePath).toMillis();
                if (currentModified > lastModified) {
                    log.info("Config file modified, reloading...");
                    loadConfig();
                }
            }
        } catch (Exception e) {
            log.error("Failed to check config file modification: {}", e.getMessage());
        }
    }
    
    @Data
    public static class SigninConfig {
        private ResponseTemplate success;
        private ResponseTemplate failed;
    }
    
    @Data
    public static class BusinessConfig {
        private TransactionConfig query;
        private TransactionConfig update;
        private TransactionConfig delete;
        private ResponseTemplate unknown;
    }
    
    @Data
    public static class TransactionConfig {
        private ResponseTemplate success;
        private ResponseTemplate failed;
    }
    
    @Data
    public static class ErrorConfig {
        private ResponseTemplate unknown_transaction_type;
        private ResponseTemplate invalid_request;
    }
    
    @Data
    public static class ResponseTemplate {
        private byte status;
        private String message;
        private String template;
    }
    
    /**
     * 生成签到成功响应
     */
    public String generateSigninSuccessResponse() {
        checkAndReloadConfig();
        return getResponseTemplate("signin.success.template", 
            "Sign in successful at " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * 生成签到失败响应
     */
    public String generateSigninFailedResponse(String reason) {
        checkAndReloadConfig();
        return getResponseTemplate("signin.failed.template", 
            "Sign in failed: " + (reason != null ? reason : "Unknown error"));
    }
    
    /**
     * 生成查询成功响应
     */
    public String generateQuerySuccessResponse(String data) {
        checkAndReloadConfig();
        return getResponseTemplate("business.query.success.template", 
            "Query result: " + (data != null ? data : "") + " - processed at " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * 生成更新成功响应
     */
    public String generateUpdateSuccessResponse(String data) {
        checkAndReloadConfig();
        return getResponseTemplate("business.update.success.template", 
            "Update result: " + (data != null ? data : "") + " - updated at " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * 生成删除成功响应
     */
    public String generateDeleteSuccessResponse(String data) {
        checkAndReloadConfig();
        return getResponseTemplate("business.delete.success.template", 
            "Delete result: " + (data != null ? data : "") + " - deleted at " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * 生成未知交易码响应
     */
    public String generateUnknownTransactionCodeResponse(String code) {
        checkAndReloadConfig();
        return getResponseTemplate("business.unknown.template", 
            "Unknown transaction code: " + (code != null ? code : "UNKNOWN"));
    }
    
    /**
     * 生成未知交易类型响应
     */
    public String generateUnknownTransactionTypeResponse(byte type) {
        checkAndReloadConfig();
        return getResponseTemplate("error.unknown_transaction_type.template", 
            "Unknown transaction type: " + type);
    }
    
    /**
     * 获取响应模板
     */
    private String getResponseTemplate(String key, String defaultValue) {
        try {
            String template = getNestedValue(key);
            if (template != null) {
                return formatTemplate(template, Map.of(
                    "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
            }
        } catch (Exception e) {
            log.warn("Failed to get template for key {}: {}", key, e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * 获取嵌套值
     */
    private String getNestedValue(String key) {
        String[] keys = key.split("\\.");
        Object current = configCache;
        
        for (String k : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(k);
            } else {
                return null;
            }
        }
        
        if (current instanceof Map) {
            return (String) ((Map<?, ?>) current).get("template");
        }
        return current != null ? current.toString() : null;
    }
    
    /**
     * 获取签到成功状态码
     */
    public byte getSigninSuccessStatus() {
        return 0; // 默认成功
    }
    
    /**
     * 获取签到失败状态码
     */
    public byte getSigninFailedStatus() {
        return 1; // 默认失败
    }
    
    /**
     * 获取业务成功状态码
     */
    public byte getBusinessSuccessStatus() {
        return 0; // 默认成功
    }
    
    /**
     * 获取业务失败状态码
     */
    public byte getBusinessFailedStatus() {
        return 1; // 默认失败
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
     * 格式化模板字符串
     */
    private String formatTemplate(String template, Map<String, String> variables) {
        if (template == null) {
            return "";
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
