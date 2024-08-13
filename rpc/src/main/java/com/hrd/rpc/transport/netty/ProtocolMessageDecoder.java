package com.hrd.rpc.transport.netty;

import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.serializer.Serializer;
import com.hrd.rpc.serializer.SerializerFactory;
import com.hrd.rpc.transport.protocol.ProtocolConstant;
import com.hrd.rpc.transport.protocol.ProtocolMessage;
import com.hrd.rpc.transport.protocol.ProtocolMessageSerializerEnum;
import com.hrd.rpc.transport.protocol.ProtocolMessageTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * description:协议消息解码器
 */
public class ProtocolMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        // 检查是否有足够的字节用于读取头部
        if (byteBuf.readableBytes() < ProtocolConstant.MESSAGE_HEADER_LENGTH) {
            return;
        }

        byteBuf.markReaderIndex(); // 标记当前读取位置

        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = byteBuf.readByte();

        // 校验魔数
//        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
//            throw new RuntimeException("消息 magic 非法");
//        }
        header.setMagic(magic);
        header.setVersion(byteBuf.readByte());
        header.setSerializer(byteBuf.readByte());
        header.setType(byteBuf.readByte());
        header.setStatus(byteBuf.readByte());
        header.setRequestId(byteBuf.readLong());
        header.setBodyLength(byteBuf.readInt());

        // 检查是否有足够的字节用于读取消息体
        if (byteBuf.readableBytes() < header.getBodyLength()) {
            byteBuf.resetReaderIndex(); // 恢复到标记位置
            return;
        }

        byte[] bodyBytes = new byte[header.getBodyLength()];
        byteBuf.readBytes(bodyBytes);//读取消息体

        // 解析消息体
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化消息的协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (messageTypeEnum == null) {
            throw new RuntimeException("序列化消息的类型不存在");
        }
        switch (messageTypeEnum) {
            case REQUEST:
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                //ProtocolMessage<RpcRequest> message = new ProtocolMessage<>(header, request);
                list.add(new ProtocolMessage<>(header, request));
                //System.out.println("进入下一个处理器");
                //channelHandlerContext.fireChannelRead(message);
                break;
            case RESPONSE:
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                list.add(new ProtocolMessage<>(header, response));
                break;
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("暂不支持该消息类型");
        }

    }
}
