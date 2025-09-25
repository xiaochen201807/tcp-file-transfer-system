package com.example.tcpserver.codec;

import com.example.tcpserver.protocol.TcpProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * TCP协议消息解码器
 * 协议格式：header + length + data
 */
@Slf4j
public class TcpProtocolDecoder extends ByteToMessageDecoder {
    
    private static final int REQUEST_HEADER_LENGTH = 42;
    private static final int RESPONSE_HEADER_LENGTH = 2;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int MIN_MESSAGE_LENGTH = 4; // 最小消息长度（length字段）
    private static final int MAX_MESSAGE_LENGTH = 1024 * 1024; // 最大消息长度（1MB）
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查是否有足够的字节读取header（至少需要42字节来判断是请求还是响应）
        if (in.readableBytes() < REQUEST_HEADER_LENGTH) {
            return;
        }
        
        // 标记读取位置
        in.markReaderIndex();
        
        // 读取前42字节来判断消息类型
        byte[] headerBytes = new byte[REQUEST_HEADER_LENGTH];
        in.readBytes(headerBytes);
        
        // 判断是请求还是响应报文
        boolean isRequest = true;
        int headerLength = REQUEST_HEADER_LENGTH;
        
        // 检查是否有足够的字节读取长度字段
        if (in.readableBytes() < LENGTH_FIELD_LENGTH) {
            in.resetReaderIndex();
            return;
        }
        
        // 读取长度字段
        int messageLength = in.readInt();
        
        // 检查消息长度是否合理
        if (messageLength < MIN_MESSAGE_LENGTH || messageLength > MAX_MESSAGE_LENGTH) {
            log.error("Invalid message length: {}", messageLength);
            ctx.close();
            return;
        }
        
        // 检查是否有足够的字节读取完整消息
        int remainingBytes = messageLength - headerLength - LENGTH_FIELD_LENGTH;
        if (in.readableBytes() < remainingBytes) {
            in.resetReaderIndex();
            return;
        }
        
        try {
            // 读取完整消息（包含header和length字段）
            byte[] messageBytes = new byte[messageLength];
            in.resetReaderIndex(); // 重置到开始位置
            in.readBytes(messageBytes);
            
            // 解析消息
            TcpProtocol.Message message = TcpProtocol.Message.fromBytes(messageBytes);
            
            log.debug("Decoded message: length={}, isRequest={}", messageLength, message.isRequest());
            out.add(message);
            
        } catch (Exception e) {
            log.error("Error decoding message: {}", e.getMessage());
            ctx.close();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Decoder exception: {}", cause.getMessage());
        ctx.close();
    }
}
