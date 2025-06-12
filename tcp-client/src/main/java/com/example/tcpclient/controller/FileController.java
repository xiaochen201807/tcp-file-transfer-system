package com.example.tcpclient.controller;

import com.example.tcpclient.protocol.FileTransferProtocol;
import com.example.tcpclient.service.FileDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 文件控制器 - 提供REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private FileDownloadService fileDownloadService;
    
    /**
     * 连接到服务器
     */
    @PostMapping("/connect")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> connect() {
        return fileDownloadService.connect()
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
        return fileDownloadService.disconnect()
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
        response.put("connected", fileDownloadService.isConnected());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取服务器文件列表
     */
    @GetMapping("/list")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getFileList() {
        if (!fileDownloadService.isConnected()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not connected to server");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        return fileDownloadService.getServerFileList()
            .thenApply(fileList -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("files", fileList);
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to get file list: " + throwable.getMessage());
                return ResponseEntity.badRequest().body(response);
            });
    }
    
    /**
     * 下载文件
     */
    @PostMapping("/download")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> downloadFile(@RequestBody Map<String, String> request) {
        String fileName = request.get("fileName");
        
        if (fileName == null || fileName.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "File name is required");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        if (!fileDownloadService.isConnected()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not connected to server");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response));
        }
        
        return fileDownloadService.downloadFile(fileName)
            .thenApply(filePath -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "File downloaded successfully");
                response.put("filePath", filePath);
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Failed to download file: " + throwable.getMessage());
                return ResponseEntity.badRequest().body(response);
            });
    }
}
