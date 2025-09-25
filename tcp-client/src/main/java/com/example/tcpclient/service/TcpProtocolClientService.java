package com.example.tcpclient.service;

import com.example.tcpclient.client.TcpClient;
import com.example.tcpclient.protocol.TcpProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * TCP协议客户端服务
 */
@Slf4j
@Service
public class TcpProtocolClientService {
    
    @Autowired
    private TcpClient tcpClient;
    
    @Autowired
    private ClientConfigService clientConfigService;
    
    /**
     * 连接到服务器
     */
    public CompletableFuture<Void> connect() {
        return tcpClient.connect();
    }
    
    /**
     * 断开连接
     */
    public CompletableFuture<Void> disconnect() {
        return tcpClient.disconnect();
    }
    
    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return tcpClient.isConnected();
    }
    
    /**
     * 发送签到请求
     */
    public CompletableFuture<Map<String, Object>> signIn(String userData) {
        return tcpClient.sendSignInRequest(userData)
                .thenApply(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", response.getResponseHeader().getStatus() == TcpProtocol.RESPONSE_SUCCESS);
                    result.put("status", response.getResponseHeader().getStatus());
                    result.put("data", new String(response.getData()));
                    return result;
                })
                .exceptionally(throwable -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", throwable.getMessage());
                    return result;
                });
    }
    
    /**
     * 发送查询请求
     */
    public CompletableFuture<Map<String, Object>> query(String queryData) {
        return tcpClient.sendQueryRequest(queryData)
                .thenApply(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", response.getResponseHeader().getStatus() == TcpProtocol.RESPONSE_SUCCESS);
                    result.put("status", response.getResponseHeader().getStatus());
                    result.put("data", new String(response.getData()));
                    return result;
                })
                .exceptionally(throwable -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", throwable.getMessage());
                    return result;
                });
    }
    
    /**
     * 发送更新请求
     */
    public CompletableFuture<Map<String, Object>> update(String updateData) {
        return tcpClient.sendUpdateRequest(updateData)
                .thenApply(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", response.getResponseHeader().getStatus() == TcpProtocol.RESPONSE_SUCCESS);
                    result.put("status", response.getResponseHeader().getStatus());
                    result.put("data", new String(response.getData()));
                    return result;
                })
                .exceptionally(throwable -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", throwable.getMessage());
                    return result;
                });
    }
    
    /**
     * 发送删除请求
     */
    public CompletableFuture<Map<String, Object>> delete(String deleteData) {
        return tcpClient.sendDeleteRequest(deleteData)
                .thenApply(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", response.getResponseHeader().getStatus() == TcpProtocol.RESPONSE_SUCCESS);
                    result.put("status", response.getResponseHeader().getStatus());
                    result.put("data", new String(response.getData()));
                    return result;
                })
                .exceptionally(throwable -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", throwable.getMessage());
                    return result;
                });
    }
    
    /**
     * 发送自定义业务请求
     */
    public CompletableFuture<Map<String, Object>> sendBusinessRequest(String transactionCode, String data) {
        return tcpClient.sendBusinessRequest(transactionCode, data)
                .thenApply(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", response.getResponseHeader().getStatus() == TcpProtocol.RESPONSE_SUCCESS);
                    result.put("status", response.getResponseHeader().getStatus());
                    result.put("data", new String(response.getData()));
                    return result;
                })
                .exceptionally(throwable -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", throwable.getMessage());
                    return result;
                });
    }
    
    /**
     * 保存响应数据到文件
     */
    public String saveResponseToFile(String fileName, String data) throws IOException {
        Path downloadDir = Paths.get(clientConfigService.getDownloadDirectory());
        
        // 如果下载目录不存在，创建它
        if (!Files.exists(downloadDir)) {
            Files.createDirectories(downloadDir);
            log.info("Created download directory: {}", downloadDir.toAbsolutePath());
        }
        
        Path filePath = downloadDir.resolve(fileName);
        Files.write(filePath, data.getBytes());
        
        String absolutePath = filePath.toAbsolutePath().toString();
        log.info("Response data saved: {}, size: {} bytes", absolutePath, data.length());
        
        return absolutePath;
    }
}
