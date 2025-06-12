package com.example.tcpserver.codec;

import com.example.tcpserver.protocol.FileTransferProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息编码器
 * 协议格式: [type(1字节)] + [length(4字节)] + [data(length字节)]
 */
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<FileTransferProtocol.Message> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, FileTransferProtocol.Message msg, ByteBuf out) throws Exception {
        // 写入消息类型
        out.writeByte(msg.getType());
        
        // 写入消息长度
        out.writeInt(msg.getLength());
        
        // 写入消息体
        if (msg.getData() != null && msg.getData().length > 0) {
            out.writeBytes(msg.getData());
        }
        
        log.debug("Encoded message: type={}, length={}", msg.getType(), msg.getLength());
    }
}
