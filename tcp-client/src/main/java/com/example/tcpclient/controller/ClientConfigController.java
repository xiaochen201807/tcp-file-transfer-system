package com.example.tcpclient.controller;

import com.example.tcpclient.service.ClientConfigService;
import com.example.tcpclient.service.ClientExternalConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
public class ClientConfigController {
    
    @Autowired
    private ClientConfigService clientConfigService;
    
    @Autowired
    private ClientExternalConfigManager externalConfigManager;
    
    /**
     * 重新加载配置
     */
    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadConfig() {
        Map<String, Object> result = new HashMap<>();
        try {
            clientConfigService.reloadConfig();
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
    @GetMapping("/info")
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
    @PostMapping("/create-external")
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
     * 获取当前配置
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentConfig() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> config = clientConfigService.getCurrentConfig();
            result.put("success", true);
            result.put("config", config);
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to get current config: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取当前配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}
