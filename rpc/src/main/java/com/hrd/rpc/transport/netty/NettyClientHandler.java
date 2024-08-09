package com.hrd.rpc.transport.netty;

import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.transport.protocol.ProtocolMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.CompletableFuture;

/**
 * description:
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private final CompletableFuture<RpcResponse> responseFuture;

    public NettyClientHandler(CompletableFuture responseFuture) {
        this.responseFuture = responseFuture;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收服务端返回信息
        ProtocolMessage<RpcResponse> responseMessage = (ProtocolMessage<RpcResponse>) msg;
        responseFuture.complete(responseMessage.getBody());
    }
}
