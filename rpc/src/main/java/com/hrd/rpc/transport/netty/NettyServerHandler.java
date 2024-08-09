package com.hrd.rpc.transport.netty;

import com.hrd.rpc.RpcApplication;
import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.registry.LocalRegistry;
import com.hrd.rpc.serializer.JdkSerializer;
import com.hrd.rpc.serializer.Serializer;
import com.hrd.rpc.serializer.SerializerFactory;
import com.hrd.rpc.transport.protocol.ProtocolMessage;
import com.hrd.rpc.transport.protocol.ProtocolMessageStatusEnum;
import com.hrd.rpc.transport.protocol.ProtocolMessageTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * description:
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            //接收请求
            ProtocolMessage<RpcRequest> requeatMessage = (ProtocolMessage<RpcRequest>) msg;
            System.out.println("服务端收到的消息是:" + requeatMessage);
            RpcRequest rpcRequest = requeatMessage.getBody();
            //调用服务
            RpcResponse rpcResponse = handleRpcRequest(rpcRequest);
            //返回响应
            ProtocolMessage<RpcResponse> responseMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = requeatMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
            //计算消息体长度
            int bodylength = getBodylength(rpcResponse);
            header.setBodyLength(bodylength);
            responseMessage.setHeader(header);
            responseMessage.setBody(rpcResponse);
            ctx.channel().writeAndFlush(responseMessage);


        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    private static int getBodylength(RpcResponse rpcResponse) throws IOException {
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        byte[] bodyBytes = serializer.serialize(rpcResponse);
        int bodylength = bodyBytes.length;
        return bodylength;
    }

    public RpcResponse handleRpcRequest(RpcRequest rpcRequest) {
        //  处理请求

        //构造响应结果
        RpcResponse rpcResponse = new RpcResponse();
        try {
            //指定序列化器
            Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());


            //服务发现
            Class<?> implClass = LocalRegistry.getServiceImpl(rpcRequest.getServiceName());
            Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(implClass.newInstance(), rpcRequest.getParameters());

            //封装返回结果
            rpcResponse.setData(result);
            rpcResponse.setMessage("ok");
            rpcResponse.setDataType(method.getReturnType());

            return rpcResponse;

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
