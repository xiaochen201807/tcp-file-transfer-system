package com.example.tcpserver.controller;

import com.example.tcpserver.handler.FileTransferHandler;
import com.example.tcpserver.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务端管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    
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
        
        status.put("serverName", "TCP File Transfer Server");
        status.put("version", "1.0.0");
        status.put("status", "RUNNING");
        status.put("startTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // TCP服务器信息
        Map<String, Object> tcpInfo = new HashMap<>();
        tcpInfo.put("port", tcpPort);
        tcpInfo.put("protocol", "Custom Binary Protocol");
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
        stats.put("connectedClients", FileTransferHandler.getConnectedClients());
        stats.put("totalFilesServed", FileTransferHandler.getTotalFilesServed());
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
        response.put("connectedClients", FileTransferHandler.getConnectedClientAddresses());
        response.put("totalCount", FileTransferHandler.getConnectedClients());
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取文件统计信息
     */
    @GetMapping("/files/stats")
    public ResponseEntity<Map<String, Object>> getFileStats() {
        Map<String, Object> stats = new HashMap<>();
        
        var fileList = fileService.getFileList();
        stats.put("totalFiles", fileList.size());
        stats.put("totalFilesServed", FileTransferHandler.getTotalFilesServed());
        
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
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}
