package com.example.tcpserver.controller;

import com.example.tcpserver.handler.TcpProtocolHandler;
import com.example.tcpserver.service.FileService;
import com.example.tcpserver.service.ResponseConfigService;
import com.example.tcpserver.service.ExternalConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务端管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private ResponseConfigService responseConfigService;
    
    @Autowired
    private ExternalConfigManager externalConfigManager;
    
    @Autowired
    private FileService fileService;
    
    @Value("${tcp.server.port:8888}")
    private int tcpPort;
    
    @Value("${server.port:8080}")
    private int httpPort;
    
    /**
     * 获取服务器状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("serverName", "TCP Protocol Server");
        status.put("version", "2.0.0");
        status.put("status", "RUNNING");
        status.put("startTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // TCP服务器信息
        Map<String, Object> tcpInfo = new HashMap<>();
        tcpInfo.put("port", tcpPort);
        tcpInfo.put("protocol", "Custom TCP Protocol (header+length+data)");
        tcpInfo.put("isRunning", true);
        status.put("tcpServer", tcpInfo);
        
        // HTTP服务器信息
        Map<String, Object> httpInfo = new HashMap<>();
        httpInfo.put("port", httpPort);
        httpInfo.put("protocol", "HTTP/REST");
        httpInfo.put("isRunning", true);
        status.put("httpServer", httpInfo);
        
        // 连接统计
        Map<String, Object> stats = new HashMap<>();
        stats.put("connectedClients", TcpProtocolHandler.getConnectedClients());
        stats.put("totalRequests", TcpProtocolHandler.getTotalRequests());
        stats.put("totalResponses", TcpProtocolHandler.getTotalResponses());
        stats.put("availableFiles", fileService.getFileList().size());
        status.put("statistics", stats);
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 获取连接的客户端列表
     */
    @GetMapping("/clients")
    public ResponseEntity<Map<String, Object>> getConnectedClients() {
        Map<String, Object> response = new HashMap<>();
        response.put("connectedClients", TcpProtocolHandler.getConnectedClients());
        response.put("totalCount", TcpProtocolHandler.getConnectedClients());
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取协议统计信息
     */
    @GetMapping("/protocol/stats")
    public ResponseEntity<Map<String, Object>> getProtocolStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalRequests", TcpProtocolHandler.getTotalRequests());
        stats.put("totalResponses", TcpProtocolHandler.getTotalResponses());
        stats.put("connectedClients", TcpProtocolHandler.getConnectedClients());
        stats.put("nextTransactionSerial", TcpProtocolHandler.getNextTransactionSerial());
        
        // 协议信息
        Map<String, Object> protocolInfo = new HashMap<>();
        protocolInfo.put("requestHeaderLength", 42);
        protocolInfo.put("responseHeaderLength", 2);
        protocolInfo.put("lengthFieldLength", 4);
        protocolInfo.put("transactionTypes", Map.of(
            "BUSINESS", 0,
            "SIGNIN", 1
        ));
        protocolInfo.put("responseStatus", Map.of(
            "SUCCESS", 0,
            "FAILED", 1
        ));
        stats.put("protocolInfo", protocolInfo);
        
        stats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取文件统计信息
     */
    @GetMapping("/files/stats")
    public ResponseEntity<Map<String, Object>> getFileStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<com.example.tcpserver.service.FileInfo> fileList = fileService.getFileList();
        stats.put("totalFiles", fileList.size());
        
        // 计算总文件大小
        long totalSize = fileList.stream()
            .mapToLong(file -> file.getFileSize())
            .sum();
        stats.put("totalFileSize", totalSize);
        stats.put("totalFileSizeFormatted", formatFileSize(totalSize));
        
        stats.put("files", fileList);
        stats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取服务器健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("tcpServer", "RUNNING");
        health.put("httpServer", "RUNNING");
        health.put("protocol", "TCP Protocol v2.0");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * 获取系统信息
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> system = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        
        // 内存信息
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemory", formatFileSize(runtime.totalMemory()));
        memory.put("freeMemory", formatFileSize(runtime.freeMemory()));
        memory.put("maxMemory", formatFileSize(runtime.maxMemory()));
        memory.put("usedMemory", formatFileSize(runtime.totalMemory() - runtime.freeMemory()));
        system.put("memory", memory);
        
        system.put("availableProcessors", runtime.availableProcessors());
        system.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(system);
    }
    
    /**
     * 重新加载响应配置
     */
    @PostMapping("/config/reload")
    public ResponseEntity<Map<String, Object>> reloadConfig() {
        Map<String, Object> result = new HashMap<>();
        try {
            responseConfigService.reloadConfig();
            result.put("success", true);
            result.put("message", "配置重新加载成功");
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to reload config: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "配置重新加载失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取配置信息
     */
    @GetMapping("/config/info")
    public ResponseEntity<Map<String, Object>> getConfigInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> configInfo = externalConfigManager.getConfigInfo();
            result.put("success", true);
            result.put("configInfo", configInfo);
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get config info: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取配置信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 创建外部配置文件
     */
    @PostMapping("/config/create-external")
    public ResponseEntity<Map<String, Object>> createExternalConfig(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "配置内容不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            externalConfigManager.createExternalConfig(content);
            result.put("success", true);
            result.put("message", "外部配置文件创建成功");
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to create external config: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "创建外部配置文件失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}
