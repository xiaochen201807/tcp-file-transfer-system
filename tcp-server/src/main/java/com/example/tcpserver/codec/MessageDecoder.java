package com.example.tcpserver.codec;

import com.example.tcpserver.protocol.FileTransferProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 消息解码器
 * 协议格式: [type(1字节)] + [length(4字节)] + [data(length字节)]
 */
@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {
    
    private static final int HEADER_SIZE = 5; // 1字节type + 4字节length
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查是否有足够的字节读取消息头
        if (in.readableBytes() < HEADER_SIZE) {
            return;
        }
        
        // 标记读取位置
        in.markReaderIndex();
        
        // 读取消息类型
        byte type = in.readByte();
        
        // 读取消息长度
        int length = in.readInt();
        
        // 检查消息长度是否合理
        if (length < 0 || length > 1024 * 1024 * 10) { // 最大10MB
            log.error("Invalid message length: {}", length);
            ctx.close();
            return;
        }
        
        // 检查是否有足够的字节读取消息体
        if (in.readableBytes() < length) {
            // 重置读取位置，等待更多数据
            in.resetReaderIndex();
            return;
        }
        
        // 读取消息体
        byte[] data = new byte[length];
        in.readBytes(data);
        
        // 创建消息对象
        FileTransferProtocol.Message message = new FileTransferProtocol.Message();
        message.setType(type);
        message.setLength(length);
        message.setData(data);
        
        log.debug("Decoded message: type={}, length={}", type, length);
        out.add(message);
    }
}
