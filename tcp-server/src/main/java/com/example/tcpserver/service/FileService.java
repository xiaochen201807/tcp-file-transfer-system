package com.example.tcpserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件服务
 */
@Slf4j
@Service
public class FileService {
    
    @Value("${tcp.server.file.directory:files}")
    private String fileDirectory;
    
    /**
     * 获取文件列表
     */
    public List<FileInfo> getFileList() {
        List<FileInfo> fileList = new ArrayList<>();
        
        try {
            Path dirPath = Paths.get(fileDirectory);
            
            // 如果目录不存在，创建它
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("Created file directory: {}", dirPath.toAbsolutePath());
            }
            
            File dir = dirPath.toFile();
            File[] files = dir.listFiles();
            
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        FileInfo fileInfo = new FileInfo(
                            file.getName(),
                            file.length(),
                            file.lastModified()
                        );
                        fileList.add(fileInfo);
                    }
                }
            }
            
            log.info("Found {} files in directory: {}", fileList.size(), dirPath.toAbsolutePath());
            
        } catch (IOException e) {
            log.error("Error reading file directory: {}", e.getMessage());
        }
        
        return fileList;
    }
    
    /**
     * 读取文件内容
     */
    public byte[] readFile(String fileName) {
        try {
            Path filePath = Paths.get(fileDirectory, fileName);
            
            if (!Files.exists(filePath)) {
                log.warn("File not found: {}", fileName);
                return null;
            }
            
            if (!Files.isRegularFile(filePath)) {
                log.warn("Not a regular file: {}", fileName);
                return null;
            }
            
            byte[] data = Files.readAllBytes(filePath);
            log.info("Read file: {}, size: {} bytes", fileName, data.length);
            return data;
            
        } catch (IOException e) {
            log.error("Error reading file {}: {}", fileName, e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String fileName) {
        Path filePath = Paths.get(fileDirectory, fileName);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }
}
