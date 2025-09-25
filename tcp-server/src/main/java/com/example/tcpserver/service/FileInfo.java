package com.example.tcpserver.service;

import lombok.Data;

/**
 * 文件信息
 */
@Data
public class FileInfo {
    private String fileName;
    private long fileSize;
    private long lastModified;
    
    public FileInfo() {}
    
    public FileInfo(String fileName, long fileSize, long lastModified) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }
}
