package com.example.tcpserver.codec;

import com.example.tcpserver.protocol.TcpProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * TCP协议消息编码器
 * 协议格式：header + length + data
 */
@Slf4j
public class TcpProtocolEncoder extends MessageToByteEncoder<TcpProtocol.Message> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, TcpProtocol.Message msg, ByteBuf out) throws Exception {
        try {
            // 将消息转换为字节数组
            byte[] messageBytes = msg.toBytes();
            
            // 写入ByteBuf
            out.writeBytes(messageBytes);
            
            log.debug("Encoded message: length={}, isRequest={}", msg.getLength(), msg.isRequest());
            
        } catch (Exception e) {
            log.error("Error encoding message: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Encoder exception: {}", cause.getMessage());
        ctx.close();
    }
}
