package com.example.tcpserver.handler;

import com.example.tcpserver.protocol.TcpProtocol;
import com.example.tcpserver.service.ResponseConfigService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TCP协议处理器
 */
@Slf4j
@Component
public class TcpProtocolHandler extends SimpleChannelInboundHandler<TcpProtocol.Message> {

    @Autowired
    private ResponseConfigService responseConfigService;

    // 统计信息
    private static volatile int connectedClients = 0;
    private static volatile long totalRequests = 0;
    private static volatile long totalResponses = 0;
    private static final AtomicLong transactionSerialCounter = new AtomicLong(1);
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        connectedClients++;
        log.info("Client connected: {}, Total clients: {}", clientAddress, connectedClients);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientAddress = ctx.channel().remoteAddress().toString();
        connectedClients--;
        log.info("Client disconnected: {}, Total clients: {}", clientAddress, connectedClients);
        super.channelInactive(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TcpProtocol.Message msg) throws Exception {
        if (msg.isRequest()) {
            handleRequest(ctx, msg);
        } else {
            handleResponse(ctx, msg);
        }
    }
    
    /**
     * 处理请求报文
     */
    private void handleRequest(ChannelHandlerContext ctx, TcpProtocol.Message msg) {
        totalRequests++;
        
        TcpProtocol.RequestHeader header = msg.getRequestHeader();
        log.info("Received request: sender={}, receiver={}, type={}, code={}, serial={}", 
                header.getSenderNodeId(), header.getReceiverNodeId(), 
                header.getTransactionType(), header.getTransactionCode(), 
                header.getTransactionSerial());
        
        // 根据交易类型处理
        if (header.getTransactionType() == TcpProtocol.TRANSACTION_TYPE_SIGNIN) {
            handleSignInRequest(ctx, msg);
        } else if (header.getTransactionType() == TcpProtocol.TRANSACTION_TYPE_BUSINESS) {
            handleBusinessRequest(ctx, msg);
        } else {
            String errorMessage = responseConfigService.generateUnknownTransactionTypeResponse(header.getTransactionType());
            sendErrorResponse(ctx, msg, errorMessage);
        }
    }
    
    /**
     * 处理响应报文
     */
    private void handleResponse(ChannelHandlerContext ctx, TcpProtocol.Message msg) {
        totalResponses++;
        
        TcpProtocol.ResponseHeader header = msg.getResponseHeader();
        log.info("Received response: status={}", header.getStatus());
        
        // 这里可以处理响应报文的逻辑
        // 例如：更新交易状态、记录日志等
    }
    
    /**
     * 处理签到交易
     */
    private void handleSignInRequest(ChannelHandlerContext ctx, TcpProtocol.Message msg) {
        TcpProtocol.RequestHeader requestHeader = msg.getRequestHeader();
        
        // 使用配置化的响应
        String responseData = responseConfigService.generateSigninSuccessResponse();
        byte responseStatus = responseConfigService.getSigninSuccessStatus();
        
        // 创建响应header
        TcpProtocol.ResponseHeader responseHeader = new TcpProtocol.ResponseHeader(responseStatus);
        
        // 创建响应消息
        TcpProtocol.Message response = new TcpProtocol.Message(responseHeader, responseData.getBytes());
        
        // 发送响应
        ctx.writeAndFlush(response);
        
        log.info("Sent sign in response to client: {}, status: {}, data: {}", 
                requestHeader.getSenderNodeId(), responseStatus, responseData);
    }
    
    /**
     * 处理业务交易
     */
    private void handleBusinessRequest(ChannelHandlerContext ctx, TcpProtocol.Message msg) {
        TcpProtocol.RequestHeader requestHeader = msg.getRequestHeader();
        String transactionCode = requestHeader.getTransactionCode();
        
        // 根据交易码处理不同的业务
        String responseData;
        byte responseStatus;
        
        switch (transactionCode.trim()) {
            case "QUERY":
                responseData = responseConfigService.generateQuerySuccessResponse(new String(msg.getData()));
                responseStatus = responseConfigService.getBusinessSuccessStatus();
                break;
            case "UPDATE":
                responseData = responseConfigService.generateUpdateSuccessResponse(new String(msg.getData()));
                responseStatus = responseConfigService.getBusinessSuccessStatus();
                break;
            case "DELETE":
                responseData = responseConfigService.generateDeleteSuccessResponse(new String(msg.getData()));
                responseStatus = responseConfigService.getBusinessSuccessStatus();
                break;
            default:
                responseData = responseConfigService.generateUnknownTransactionCodeResponse(transactionCode);
                responseStatus = responseConfigService.getBusinessFailedStatus();
                break;
        }
        
        // 创建响应header
        TcpProtocol.ResponseHeader responseHeader = new TcpProtocol.ResponseHeader(responseStatus);
        
        // 创建响应消息
        TcpProtocol.Message response = new TcpProtocol.Message(responseHeader, responseData.getBytes());
        
        // 发送响应
        ctx.writeAndFlush(response);
        
        log.info("Sent business response: code={}, status={}, data={}", transactionCode, responseStatus, responseData);
    }
    
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, TcpProtocol.Message originalMsg, String errorMessage) {
        TcpProtocol.ResponseHeader errorHeader = new TcpProtocol.ResponseHeader(TcpProtocol.RESPONSE_FAILED);
        TcpProtocol.Message errorResponse = new TcpProtocol.Message(errorHeader, errorMessage.getBytes());
        
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
    
    public static long getTotalRequests() {
        return totalRequests;
    }
    
    public static long getTotalResponses() {
        return totalResponses;
    }
    
    public static long getNextTransactionSerial() {
        return transactionSerialCounter.getAndIncrement();
    }
}
