package com.example.tcpclient.service;

import com.example.tcpclient.client.TcpClient;
import com.example.tcpclient.protocol.FileTransferProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 文件下载服务
 */
@Slf4j
@Service
public class FileDownloadService {
    
    @Autowired
    private TcpClient tcpClient;
    
    @Value("${tcp.client.download.directory:downloads}")
    private String downloadDirectory;
    
    /**
     * 获取服务器文件列表
     */
    public CompletableFuture<List<FileTransferProtocol.FileInfo>> getServerFileList() {
        return tcpClient.requestFileList()
            .thenApply(this::parseFileList);
    }
    
    /**
     * 下载文件
     */
    public CompletableFuture<String> downloadFile(String fileName) {
        return tcpClient.downloadFile(fileName)
            .thenApply(fileData -> {
                try {
                    return saveFile(fileName, fileData);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
                }
            });
    }
    
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
     * 解析文件列表字符串
     */
    private List<FileTransferProtocol.FileInfo> parseFileList(String fileListStr) {
        List<FileTransferProtocol.FileInfo> fileList = new ArrayList<>();
        
        if (fileListStr != null && !fileListStr.trim().isEmpty()) {
            String[] lines = fileListStr.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    FileTransferProtocol.FileInfo fileInfo = FileTransferProtocol.FileInfo.fromString(line);
                    if (fileInfo != null) {
                        fileList.add(fileInfo);
                    }
                }
            }
        }
        
        return fileList;
    }
    
    /**
     * 保存文件到本地
     */
    private String saveFile(String fileName, byte[] fileData) throws IOException {
        Path downloadDir = Paths.get(downloadDirectory);
        
        // 如果下载目录不存在，创建它
        if (!Files.exists(downloadDir)) {
            Files.createDirectories(downloadDir);
            log.info("Created download directory: {}", downloadDir.toAbsolutePath());
        }
        
        Path filePath = downloadDir.resolve(fileName);
        Files.write(filePath, fileData);
        
        String absolutePath = filePath.toAbsolutePath().toString();
        log.info("File saved: {}, size: {} bytes", absolutePath, fileData.length);
        
        return absolutePath;
    }
}
