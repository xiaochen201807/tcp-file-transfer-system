package com.example.tcpclient.client;

import com.example.tcpclient.codec.MessageDecoder;
import com.example.tcpclient.codec.MessageEncoder;
import com.example.tcpclient.handler.ClientHandler;
import com.example.tcpclient.protocol.FileTransferProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * TCP客户端
 */
@Slf4j
@Component
public class TcpClient {
    
    @Value("${tcp.client.server.host:localhost}")
    private String serverHost;
    
    @Value("${tcp.client.server.port:8888}")
    private int serverPort;
    
    @Value("${tcp.client.connect.timeout:5000}")
    private int connectTimeout;
    
    private EventLoopGroup group;
    private Channel channel;
    private ClientHandler clientHandler;
    
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 添加编解码器
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        
                        // 添加业务处理器
                        pipeline.addLast(clientHandler);
                    }
                });
            
            ChannelFuture connectFuture = bootstrap.connect(serverHost, serverPort);
            connectFuture.addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    channel = channelFuture.channel();
                    log.info("Connected to server {}:{}", serverHost, serverPort);
                    future.complete(null);
                } else {
                    log.error("Failed to connect to server {}:{}", serverHost, serverPort);
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
     * 请求文件列表
     */
    public CompletableFuture<String> requestFileList() {
        if (!isConnected()) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Not connected to server"));
            return future;
        }
        
        FileTransferProtocol.Message request = new FileTransferProtocol.Message(
            FileTransferProtocol.MSG_TYPE_LIST_FILES, 
            new byte[0]
        );
        
        CompletableFuture<String> future = clientHandler.sendRequest(channel.pipeline().context(clientHandler), request)
            .thenApply(response -> {
                if (response.getType() == FileTransferProtocol.MSG_TYPE_FILE_LIST) {
                    return new String(response.getData());
                } else if (response.getType() == FileTransferProtocol.MSG_TYPE_ERROR) {
                    throw new RuntimeException("Server error: " + new String(response.getData()));
                } else {
                    throw new RuntimeException("Unexpected response type: " + response.getType());
                }
            });

        // JDK 1.8兼容的超时处理
        return addTimeout(future, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 下载文件
     */
    public CompletableFuture<byte[]> downloadFile(String fileName) {
        if (!isConnected()) {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Not connected to server"));
            return future;
        }
        
        FileTransferProtocol.Message request = new FileTransferProtocol.Message(
            FileTransferProtocol.MSG_TYPE_DOWNLOAD_FILE, 
            fileName
        );
        
        CompletableFuture<byte[]> future = clientHandler.sendRequest(channel.pipeline().context(clientHandler), request)
            .thenApply(response -> {
                if (response.getType() == FileTransferProtocol.MSG_TYPE_FILE_DATA) {
                    return response.getData();
                } else if (response.getType() == FileTransferProtocol.MSG_TYPE_ERROR) {
                    throw new RuntimeException("Server error: " + new String(response.getData()));
                } else {
                    throw new RuntimeException("Unexpected response type: " + response.getType());
                }
            });

        // JDK 1.8兼容的超时处理
        return addTimeout(future, 60, TimeUnit.SECONDS);
    }

    /**
     * JDK 1.8兼容的超时处理方法
     */
    private <T> CompletableFuture<T> addTimeout(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        CompletableFuture<T> timeoutFuture = new CompletableFuture<>();

        // 创建超时任务
        java.util.concurrent.ScheduledExecutorService scheduler =
            java.util.concurrent.Executors.newScheduledThreadPool(1);

        java.util.concurrent.ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            timeoutFuture.completeExceptionally(new java.util.concurrent.TimeoutException("Operation timed out"));
        }, timeout, unit);

        // 当原始future完成时，取消超时任务并完成结果
        future.whenComplete((result, throwable) -> {
            timeoutTask.cancel(false);
            scheduler.shutdown();
            if (throwable != null) {
                timeoutFuture.completeExceptionally(throwable);
            } else {
                timeoutFuture.complete(result);
            }
        });

        return timeoutFuture;
    }
}
