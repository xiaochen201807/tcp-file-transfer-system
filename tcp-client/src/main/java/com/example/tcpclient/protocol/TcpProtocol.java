package com.example.tcpclient.protocol;

import lombok.Data;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * TCP协议定义
 * 格式：header + length + data
 * 请求报文header：42字节（6+6+1+7+20+2）
 * 响应报文header：2字节
 */
public class TcpProtocol {
    
    // 请求报文header长度
    public static final int REQUEST_HEADER_LENGTH = 42;
    
    // 响应报文header长度
    public static final int RESPONSE_HEADER_LENGTH = 2;
    
    // 长度字段长度（4字节）
    public static final int LENGTH_FIELD_LENGTH = 4;
    
    // 交易类型标识
    public static final byte TRANSACTION_TYPE_BUSINESS = 0;  // 业务类交易
    public static final byte TRANSACTION_TYPE_SIGNIN = 1;    // 签到交易
    
    // 响应状态码
    public static final byte RESPONSE_SUCCESS = 0x00;        // 成功
    public static final byte RESPONSE_FAILED = 0x01;         // 失败
    
    /**
     * 请求报文header（42字节）
     */
    @Data
    public static class RequestHeader {
        private String senderNodeId;      // 发送系统节点号（6字节）
        private String receiverNodeId;   // 接收系统节点号（6字节）
        private byte transactionType;     // 交易类型标识（1字节）
        private String transactionCode;   // 交易类型码（7字节）
        private String transactionSerial; // 交易流水号（20字节）
        private byte[] reserved;          // 预留（2字节）
        
        public RequestHeader() {
            this.reserved = new byte[2];
        }
        
        public RequestHeader(String senderNodeId, String receiverNodeId, 
                           byte transactionType, String transactionCode, 
                           String transactionSerial) {
            this.senderNodeId = senderNodeId;
            this.receiverNodeId = receiverNodeId;
            this.transactionType = transactionType;
            this.transactionCode = transactionCode;
            this.transactionSerial = transactionSerial;
            this.reserved = new byte[2];
        }
        
        /**
         * 将header转换为字节数组
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(REQUEST_HEADER_LENGTH);
            buffer.order(ByteOrder.BIG_ENDIAN);
            
            // 发送系统节点号（6字节）
            byte[] senderBytes = padToLength(senderNodeId.getBytes(), 6);
            buffer.put(senderBytes);
            
            // 接收系统节点号（6字节）
            byte[] receiverBytes = padToLength(receiverNodeId.getBytes(), 6);
            buffer.put(receiverBytes);
            
            // 交易类型标识（1字节）
            buffer.put(transactionType);
            
            // 交易类型码（7字节）
            byte[] codeBytes = padToLength(transactionCode.getBytes(), 7);
            buffer.put(codeBytes);
            
            // 交易流水号（20字节）
            byte[] serialBytes = padToLength(transactionSerial.getBytes(), 20);
            buffer.put(serialBytes);
            
            // 预留（2字节）
            buffer.put(reserved);
            
            return buffer.array();
        }
        
        /**
         * 从字节数组解析header
         */
        public static RequestHeader fromBytes(byte[] bytes) {
            if (bytes.length != REQUEST_HEADER_LENGTH) {
                throw new IllegalArgumentException("Invalid header length: " + bytes.length);
            }
            
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.BIG_ENDIAN);
            
            RequestHeader header = new RequestHeader();
            
            // 发送系统节点号（6字节）
            byte[] senderBytes = new byte[6];
            buffer.get(senderBytes);
            header.senderNodeId = new String(senderBytes).trim();
            
            // 接收系统节点号（6字节）
            byte[] receiverBytes = new byte[6];
            buffer.get(receiverBytes);
            header.receiverNodeId = new String(receiverBytes).trim();
            
            // 交易类型标识（1字节）
            header.transactionType = buffer.get();
            
            // 交易类型码（7字节）
            byte[] codeBytes = new byte[7];
            buffer.get(codeBytes);
            header.transactionCode = new String(codeBytes).trim();
            
            // 交易流水号（20字节）
            byte[] serialBytes = new byte[20];
            buffer.get(serialBytes);
            header.transactionSerial = new String(serialBytes).trim();
            
            // 预留（2字节）
            header.reserved = new byte[2];
            buffer.get(header.reserved);
            
            return header;
        }
        
        /**
         * 将字节数组填充到指定长度
         */
        private byte[] padToLength(byte[] bytes, int length) {
            byte[] result = new byte[length];
            int copyLength = Math.min(bytes.length, length);
            System.arraycopy(bytes, 0, result, 0, copyLength);
            return result;
        }
    }
    
    /**
     * 响应报文header（2字节）
     */
    @Data
    public static class ResponseHeader {
        private byte status;  // 返回状态
        
        public ResponseHeader() {}
        
        public ResponseHeader(byte status) {
            this.status = status;
        }
        
        /**
         * 将header转换为字节数组
         */
        public byte[] toBytes() {
            return new byte[]{status, 0x00}; // 第二个字节预留
        }
        
        /**
         * 从字节数组解析header
         */
        public static ResponseHeader fromBytes(byte[] bytes) {
            if (bytes.length != RESPONSE_HEADER_LENGTH) {
                throw new IllegalArgumentException("Invalid response header length: " + bytes.length);
            }
            return new ResponseHeader(bytes[0]);
        }
    }
    
    /**
     * 完整报文
     */
    @Data
    public static class Message {
        private RequestHeader requestHeader;    // 请求header（仅请求报文有）
        private ResponseHeader responseHeader;   // 响应header（仅响应报文有）
        private int length;                      // 报文长度
        private byte[] data;                     // 报文数据
        private boolean isRequest;               // 是否为请求报文
        
        public Message() {}
        
        public Message(RequestHeader requestHeader, byte[] data) {
            this.requestHeader = requestHeader;
            this.data = data != null ? data : new byte[0];
            this.length = REQUEST_HEADER_LENGTH + LENGTH_FIELD_LENGTH + this.data.length;
            this.isRequest = true;
        }
        
        public Message(ResponseHeader responseHeader, byte[] data) {
            this.responseHeader = responseHeader;
            this.data = data != null ? data : new byte[0];
            this.length = RESPONSE_HEADER_LENGTH + LENGTH_FIELD_LENGTH + this.data.length;
            this.isRequest = false;
        }
        
        /**
         * 将完整报文转换为字节数组
         */
        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(length);
            buffer.order(ByteOrder.BIG_ENDIAN);
            
            if (isRequest) {
                // 请求报文：header + length + data
                buffer.put(requestHeader.toBytes());
                buffer.putInt(length);
                buffer.put(data);
            } else {
                // 响应报文：header + length + data
                buffer.put(responseHeader.toBytes());
                buffer.putInt(length);
                buffer.put(data);
            }
            
            return buffer.array();
        }
        
        /**
         * 从字节数组解析完整报文
         */
        public static Message fromBytes(byte[] bytes) {
            if (bytes.length < LENGTH_FIELD_LENGTH) {
                throw new IllegalArgumentException("Message too short");
            }
            
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.BIG_ENDIAN);
            
            // 通过检查前2字节来判断是请求还是响应报文
            // 响应报文的前2字节是状态码（通常是0x00或0x01）
            byte firstByte = bytes[0];
            byte secondByte = bytes[1];
            
            boolean isResponse = (firstByte == 0x00 || firstByte == 0x01) && secondByte == 0x00;
            
            if (isResponse) {
                // 响应报文：header(2) + length(4) + data
                byte[] headerBytes = new byte[RESPONSE_HEADER_LENGTH];
                buffer.get(headerBytes);
                ResponseHeader responseHeader = ResponseHeader.fromBytes(headerBytes);
                
                // 读取长度字段
                int length = buffer.getInt();
                
                if (bytes.length != length) {
                    throw new IllegalArgumentException("Incomplete message");
                }
                
                int dataLength = length - RESPONSE_HEADER_LENGTH - LENGTH_FIELD_LENGTH;
                byte[] data = new byte[dataLength];
                buffer.get(data);
                
                Message message = new Message(responseHeader, data);
                message.length = length;
                return message;
            } else {
                // 请求报文：header(42) + length(4) + data
                byte[] headerBytes = new byte[REQUEST_HEADER_LENGTH];
                buffer.get(headerBytes);
                RequestHeader requestHeader = RequestHeader.fromBytes(headerBytes);
                
                // 读取长度字段
                int length = buffer.getInt();
                
                if (bytes.length != length) {
                    throw new IllegalArgumentException("Incomplete message");
                }
                
                int dataLength = length - REQUEST_HEADER_LENGTH - LENGTH_FIELD_LENGTH;
                byte[] data = new byte[dataLength];
                buffer.get(data);
                
                Message message = new Message(requestHeader, data);
                message.length = length;
                return message;
            }
        }
    }
}
