package com.example.tcpclient.client;

import com.example.tcpclient.codec.TcpProtocolDecoder;
import com.example.tcpclient.codec.TcpProtocolEncoder;
import com.example.tcpclient.handler.ClientHandler;
import com.example.tcpclient.protocol.TcpProtocol;
import com.example.tcpclient.service.ClientConfigService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TCP客户端
 */
@Slf4j
@Component
public class TcpClient {
    
    @Autowired
    private ClientConfigService clientConfigService;
    
    private EventLoopGroup group;
    private Channel channel;
    private ClientHandler clientHandler;
    private final AtomicLong transactionSerialCounter = new AtomicLong(1);
    
    /**
     * 连接到服务器
     */
    public CompletableFuture<Void> connect() {
        if (isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        group = new NioEventLoopGroup();
        clientHandler = new ClientHandler();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfigService.getConnectTimeout())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 添加编解码器
                        pipeline.addLast(new TcpProtocolDecoder());
                        pipeline.addLast(new TcpProtocolEncoder());
                        
                        // 添加业务处理器
                        pipeline.addLast(clientHandler);
                    }
                });
            
            ChannelFuture connectFuture = bootstrap.connect(clientConfigService.getTcpServerHost(), clientConfigService.getTcpServerPort());
            connectFuture.addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    channel = channelFuture.channel();
                    log.info("Connected to server {}:{}", clientConfigService.getTcpServerHost(), clientConfigService.getTcpServerPort());
                    future.complete(null);
                } else {
                    log.error("Failed to connect to server {}:{}", clientConfigService.getTcpServerHost(), clientConfigService.getTcpServerPort());
                    future.completeExceptionally(channelFuture.cause());
                    group.shutdownGracefully();
                }
            });
            
        } catch (Exception e) {
            log.error("Error connecting to server: {}", e.getMessage());
            future.completeExceptionally(e);
            if (group != null) {
                group.shutdownGracefully();
            }
        }
        
        return future;
    }
    
    /**
     * 断开连接
     */
    public CompletableFuture<Void> disconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (channel != null && channel.isActive()) {
            channel.close().addListener((ChannelFutureListener) channelFuture -> {
                if (group != null) {
                    group.shutdownGracefully().addListener(groupFuture -> {
                        log.info("Disconnected from server");
                        future.complete(null);
                    });
                } else {
                    future.complete(null);
                }
            });
        } else {
            if (group != null) {
                group.shutdownGracefully().addListener(groupFuture -> {
                    future.complete(null);
                });
            } else {
                future.complete(null);
            }
        }
        
        return future;
    }
    
    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }
    
    /**
     * 发送签到请求
     */
    public CompletableFuture<TcpProtocol.Message> sendSignInRequest(String userData) {
        if (!isConnected()) {
            return CompletableFuture.failedFuture(new RuntimeException("Not connected to server"));
        }
        
        String transactionSerial = generateTransactionSerial();
        TcpProtocol.RequestHeader requestHeader = new TcpProtocol.RequestHeader(
                clientConfigService.getSenderNodeId(),
                "SERVER",
                TcpProtocol.TRANSACTION_TYPE_SIGNIN,
                "SIGNIN",
                transactionSerial
        );
        
        TcpProtocol.Message request = new TcpProtocol.Message(requestHeader, userData.getBytes());
        
        return clientHandler.sendRequest(channel.pipeline().context(clientHandler), request)
                .orTimeout(30, TimeUnit.SECONDS);
    }
    
    /**
     * 发送业务请求
     */
    public CompletableFuture<TcpProtocol.Message> sendBusinessRequest(String transactionCode, String data) {
        if (!isConnected()) {
            return CompletableFuture.failedFuture(new RuntimeException("Not connected to server"));
        }
        
        String transactionSerial = generateTransactionSerial();
        TcpProtocol.RequestHeader requestHeader = new TcpProtocol.RequestHeader(
                clientConfigService.getSenderNodeId(),
                "SERVER",
                TcpProtocol.TRANSACTION_TYPE_BUSINESS,
                transactionCode,
                transactionSerial
        );
        
        TcpProtocol.Message request = new TcpProtocol.Message(requestHeader, data.getBytes());
        
        return clientHandler.sendRequest(channel.pipeline().context(clientHandler), request)
                .orTimeout(60, TimeUnit.SECONDS);
    }
    
    /**
     * 发送查询请求
     */
    public CompletableFuture<TcpProtocol.Message> sendQueryRequest(String queryData) {
        return sendBusinessRequest("QUERY", queryData);
    }
    
    /**
     * 发送更新请求
     */
    public CompletableFuture<TcpProtocol.Message> sendUpdateRequest(String updateData) {
        return sendBusinessRequest("UPDATE", updateData);
    }
    
    /**
     * 发送删除请求
     */
    public CompletableFuture<TcpProtocol.Message> sendDeleteRequest(String deleteData) {
        return sendBusinessRequest("DELETE", deleteData);
    }
    
    /**
     * 生成交易流水号
     */
    private String generateTransactionSerial() {
        return String.format("%020d", transactionSerialCounter.getAndIncrement());
    }
}
