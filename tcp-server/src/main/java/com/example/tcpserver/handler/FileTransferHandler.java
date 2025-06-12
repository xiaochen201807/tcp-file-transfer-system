package com.example.tcpserver.handler;

import com.example.tcpserver.protocol.FileTransferProtocol;
import com.example.tcpserver.service.FileService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 文件传输处理器
 */
@Slf4j
@Component
public class FileTransferHandler extends SimpleChannelInboundHandler<FileTransferProtocol.Message> {

    @Autowired
    private FileService fileService;

    // 统计信息
    private static volatile int connectedClients = 0;
    private static volatile long totalFilesServed = 0;
    private static final Set<String> connectedClientAddresses = ConcurrentHashMap.newKeySet();
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        connectedClients++;
        connectedClientAddresses.add(clientAddress);
        log.info("Client connected: {}, Total clients: {}", clientAddress, connectedClients);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        connectedClients--;
        connectedClientAddresses.remove(clientAddress);
        log.info("Client disconnected: {}, Total clients: {}", clientAddress, connectedClients);
        super.channelInactive(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileTransferProtocol.Message msg) throws Exception {
        log.info("Received message type: {} from {}", msg.getType(), ctx.channel().remoteAddress());
        
        switch (msg.getType()) {
            case FileTransferProtocol.MSG_TYPE_LIST_FILES:
                handleListFiles(ctx);
                break;
                
            case FileTransferProtocol.MSG_TYPE_DOWNLOAD_FILE:
                handleDownloadFile(ctx, msg);
                break;
                
            default:
                log.warn("Unknown message type: {}", msg.getType());
                sendError(ctx, "Unknown message type");
                break;
        }
    }
    
    /**
     * 处理文件列表请求
     */
    private void handleListFiles(ChannelHandlerContext ctx) {
        try {
            List<FileTransferProtocol.FileInfo> fileList = fileService.getFileList();
            
            // 将文件列表转换为字符串
            String fileListStr = fileList.stream()
                .map(FileTransferProtocol.FileInfo::toString)
                .collect(Collectors.joining("\n"));
            
            FileTransferProtocol.Message response = new FileTransferProtocol.Message(
                FileTransferProtocol.MSG_TYPE_FILE_LIST, 
                fileListStr
            );
            
            ctx.writeAndFlush(response);
            log.info("Sent file list with {} files", fileList.size());
            
        } catch (Exception e) {
            log.error("Error handling file list request: {}", e.getMessage());
            sendError(ctx, "Error getting file list: " + e.getMessage());
        }
    }
    
    /**
     * 处理文件下载请求
     */
    private void handleDownloadFile(ChannelHandlerContext ctx, FileTransferProtocol.Message msg) {
        try {
            String fileName = new String(msg.getData());
            log.info("Download request for file: {}", fileName);
            
            if (!fileService.fileExists(fileName)) {
                sendError(ctx, "File not found: " + fileName);
                return;
            }
            
            byte[] fileData = fileService.readFile(fileName);
            if (fileData == null) {
                sendError(ctx, "Error reading file: " + fileName);
                return;
            }
            
            FileTransferProtocol.Message response = new FileTransferProtocol.Message(
                FileTransferProtocol.MSG_TYPE_FILE_DATA, 
                fileData
            );
            
            ctx.writeAndFlush(response);
            totalFilesServed++;
            log.info("Sent file: {}, size: {} bytes, Total files served: {}", fileName, fileData.length, totalFilesServed);

        } catch (Exception e) {
            log.error("Error handling download request: {}", e.getMessage());
            sendError(ctx, "Error downloading file: " + e.getMessage());
        }
    }
    
    /**
     * 发送错误响应
     */
    private void sendError(ChannelHandlerContext ctx, String errorMessage) {
        FileTransferProtocol.Message errorResponse = new FileTransferProtocol.Message(
            FileTransferProtocol.MSG_TYPE_ERROR, 
            errorMessage
        );
        ctx.writeAndFlush(errorResponse);
        log.warn("Sent error response: {}", errorMessage);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception in channel: {}", cause.getMessage());
        ctx.close();
    }

    // 静态方法供管理接口调用
    public static int getConnectedClients() {
        return connectedClients;
    }

    public static long getTotalFilesServed() {
        return totalFilesServed;
    }

    public static Set<String> getConnectedClientAddresses() {
        return Set.copyOf(connectedClientAddresses);
    }
}
