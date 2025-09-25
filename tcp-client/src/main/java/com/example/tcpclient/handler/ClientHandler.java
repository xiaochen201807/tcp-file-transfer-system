package com.example.tcpclient.handler;

import com.example.tcpclient.protocol.TcpProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端处理器
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<TcpProtocol.Message> {
    
    private final ConcurrentHashMap<Long, CompletableFuture<TcpProtocol.Message>> pendingRequests = new ConcurrentHashMap<>();
    private CompletableFuture<TcpProtocol.Message> currentRequest;
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Connected to server: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Disconnected from server: {}", ctx.channel().remoteAddress());
        
        // 完成所有待处理的请求
        pendingRequests.values().forEach(future -> 
            future.completeExceptionally(new RuntimeException("Connection closed"))
        );
        pendingRequests.clear();
        
        if (currentRequest != null && !currentRequest.isDone()) {
            currentRequest.completeExceptionally(new RuntimeException("Connection closed"));
        }
        
        super.channelInactive(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TcpProtocol.Message msg) throws Exception {
        log.debug("Received message: isRequest={}, length={}", msg.isRequest(), msg.getLength());
        
        if (msg.isRequest()) {
            // 收到请求报文（通常客户端不会收到请求）
            log.warn("Received unexpected request message");
        } else {
            // 收到响应报文
            TcpProtocol.ResponseHeader responseHeader = msg.getResponseHeader();
            log.info("Received response: status={}, data={}", 
                    responseHeader.getStatus(), new String(msg.getData()));
            
            if (currentRequest != null && !currentRequest.isDone()) {
                currentRequest.complete(msg);
                currentRequest = null;
            } else {
                log.warn("Received unexpected response message");
            }
        }
    }
    
    /**
     * 发送请求并等待响应
     */
    public CompletableFuture<TcpProtocol.Message> sendRequest(ChannelHandlerContext ctx, TcpProtocol.Message request) {
        if (currentRequest != null && !currentRequest.isDone()) {
            return CompletableFuture.failedFuture(new RuntimeException("Another request is in progress"));
        }
        
        currentRequest = new CompletableFuture<>();
        ctx.writeAndFlush(request);
        
        return currentRequest;
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception in client handler: {}", cause.getMessage());
        
        if (currentRequest != null && !currentRequest.isDone()) {
            currentRequest.completeExceptionally(cause);
        }
        
        ctx.close();
    }
}
