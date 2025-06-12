package com.example.tcpserver.protocol;

import lombok.Data;

/**
 * 文件传输协议定义
 */
public class FileTransferProtocol {
    
    // 消息类型常量
    public static final byte MSG_TYPE_LIST_FILES = 1;      // 请求文件列表
    public static final byte MSG_TYPE_FILE_LIST = 2;       // 文件列表响应
    public static final byte MSG_TYPE_DOWNLOAD_FILE = 3;   // 请求下载文件
    public static final byte MSG_TYPE_FILE_DATA = 4;       // 文件数据响应
    public static final byte MSG_TYPE_ERROR = 5;           // 错误响应
    public static final byte MSG_TYPE_SUCCESS = 6;         // 成功响应
    
    @Data
    public static class Message {
        private byte type;          // 消息类型
        private int length;         // 消息体长度
        private byte[] data;        // 消息体数据
        
        public Message() {}
        
        public Message(byte type, byte[] data) {
            this.type = type;
            this.data = data != null ? data : new byte[0];
            this.length = this.data.length;
        }
        
        public Message(byte type, String text) {
            this(type, text != null ? text.getBytes() : new byte[0]);
        }
    }
    
    @Data
    public static class FileInfo {
        private String fileName;
        private long fileSize;
        private long lastModified;
        
        public FileInfo() {}
        
        public FileInfo(String fileName, long fileSize, long lastModified) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.lastModified = lastModified;
        }
        
        @Override
        public String toString() {
            return fileName + "," + fileSize + "," + lastModified;
        }
        
        public static FileInfo fromString(String str) {
            String[] parts = str.split(",");
            if (parts.length == 3) {
                return new FileInfo(parts[0], Long.parseLong(parts[1]), Long.parseLong(parts[2]));
            }
            return null;
        }
    }
}
