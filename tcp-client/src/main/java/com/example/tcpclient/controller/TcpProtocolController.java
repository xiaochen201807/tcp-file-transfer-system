package com.example.tcpclient.controller;

import com.example.tcpclient.service.TcpProtocolClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * TCP协议客户端控制器 - 提供REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/tcp")
public class TcpProtocolController {
    
    @Autowired
    private TcpProtocolClientService tcpProtocolClientService;
    
    /**
     * 连接到服务器
     */
    @PostMapping("/connect")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> connect() {
        return tcpProtocolClientService.connect()
                .thenApply(result -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Connected to server successfully");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Failed to connect: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }
    
    /**
     * 断开连接
     */
    @PostMapping("/disconnect")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> disconnect() {
        return tcpProtocolClientService.disconnect()
                .thenApply(result -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Disconnected from server successfully");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Failed to disconnect: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }
    
    /**
     * 检查连接状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("connected", tcpProtocolClientService.isConnected());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 发送签到请求
     */
    @PostMapping("/signin")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> signIn(@RequestBody Map<String, String> request) {
        String userData = request.get("userData");
        
        if (userData == null || userData.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User data is required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (!tcpProtocolClientService.isConnected()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not connected to server");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        return tcpProtocolClientService.signIn(userData)
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(throwable -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Sign in failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }
    
    /**
     * 发送查询请求
     */
    @PostMapping("/query")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> query(@RequestBody Map<String, String> request) {
        String queryData = request.get("queryData");
        
        if (queryData == null || queryData.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Query data is required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (!tcpProtocolClientService.isConnected()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not connected to server");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        return tcpProtocolClientService.query(queryData)
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(throwable -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Query failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }
    
    /**
     * 发送更新请求
     */
    @PostMapping("/update")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> update(@RequestBody Map<String, String> request) {
        String updateData = request.get("updateData");
        
        if (updateData == null || updateData.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Update data is required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (!tcpProtocolClientService.isConnected()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not connected to server");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        return tcpProtocolClientService.update(updateData)
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(throwable -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Update failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }
    
    /**
     * 发送删除请求
     */
    @PostMapping("/delete")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> delete(@RequestBody Map<String, String> request) {
        String deleteData = request.get("deleteData");
        
        if (deleteData == null || deleteData.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Delete data is required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (!tcpProtocolClientService.isConnected()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not connected to server");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        return tcpProtocolClientService.delete(deleteData)
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(throwable -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Delete failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }
    
    /**
     * 发送自定义业务请求
     */
    @PostMapping("/business")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendBusinessRequest(@RequestBody Map<String, String> request) {
        String transactionCode = request.get("transactionCode");
        String data = request.get("data");
        
        if (transactionCode == null || transactionCode.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Transaction code is required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (!tcpProtocolClientService.isConnected()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not connected to server");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        return tcpProtocolClientService.sendBusinessRequest(transactionCode, data != null ? data : "")
                .thenApply(result -> ResponseEntity.ok(result))
                .exceptionally(throwable -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Business request failed: " + throwable.getMessage());
                    return ResponseEntity.badRequest().body(response);
                });
    }
    
    /**
     * 保存响应数据到文件
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveResponseToFile(@RequestBody Map<String, String> request) {
        String fileName = request.get("fileName");
        String data = request.get("data");
        
        if (fileName == null || fileName.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "File name is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (data == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Data is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            String filePath = tcpProtocolClientService.saveResponseToFile(fileName, data);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data saved successfully");
            response.put("filePath", filePath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to save data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
