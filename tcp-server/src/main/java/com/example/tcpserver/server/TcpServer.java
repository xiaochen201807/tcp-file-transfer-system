package com.example.tcpserver.server;

import com.example.tcpserver.codec.MessageDecoder;
import com.example.tcpserver.codec.MessageEncoder;
import com.example.tcpserver.handler.FileTransferHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * TCP服务器
 */
@Slf4j
@Component
public class TcpServer {
    
    @Value("${tcp.server.port:8888}")
    private int port;
    
    @Autowired
    private FileTransferHandler fileTransferHandler;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    @PostConstruct
    public void start() {
        new Thread(this::doStart).start();
    }
    
    private void doStart() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 添加编解码器
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        
                        // 添加业务处理器
                        pipeline.addLast(fileTransferHandler);
                    }
                });
            
            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            
            log.info("TCP Server started on port {}", port);
            
            // 等待服务器关闭
            serverChannel.closeFuture().sync();
            
        } catch (InterruptedException e) {
            log.error("TCP Server interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("TCP Server error: {}", e.getMessage());
        } finally {
            shutdown();
        }
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down TCP Server...");
        
        if (serverChannel != null) {
            serverChannel.close();
        }
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        
        log.info("TCP Server shutdown complete");
    }
}
