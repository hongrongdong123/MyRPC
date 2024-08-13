package com.hrd.rpc.transport.netty;

import com.hrd.rpc.serializer.Serializer;
import com.hrd.rpc.serializer.SerializerFactory;
import com.hrd.rpc.transport.protocol.ProtocolMessage;
import com.hrd.rpc.transport.protocol.ProtocolMessageSerializerEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * description:协议消息编码器
 */
public class ProtocolMessageEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {

        if(!(o instanceof ProtocolMessage<?>)) {
            throw new Exception(String.format("Unknown type: %s!", o.getClass().getCanonicalName()));
        }

        ProtocolMessage protocolMessage = (ProtocolMessage) o;
        ProtocolMessage.Header header = protocolMessage.getHeader();

        // 将消息头写入缓冲区
        byteBuf.writeByte(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getSerializer());
        byteBuf.writeByte(header.getType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        byteBuf.writeInt(header.getBodyLength());
        // 获取序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());
        //将消息体写入缓冲区
        byteBuf.writeBytes(bodyBytes);
    }
}
