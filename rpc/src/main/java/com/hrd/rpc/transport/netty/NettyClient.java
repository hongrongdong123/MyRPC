package com.hrd.rpc.transport.netty;

import com.hrd.rpc.model.RpcRequest;
import com.hrd.rpc.model.RpcResponse;
import com.hrd.rpc.transport.protocol.ProtocolMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CompletableFuture;

/**
 * description:netty客户端
 */
public class NettyClient {

    private static NioEventLoopGroup group;

    private static Channel channel;

    public static RpcResponse initAndSend(String serverHost, Integer serverPort, ProtocolMessage<RpcRequest> requestMessage) {
        try {

            CompletableFuture<RpcResponse> completableFuture = new CompletableFuture();
            // 1.创建线程组
            group = new NioEventLoopGroup();
            // 2.创建客户端启动助手
            Bootstrap bootstrap = new Bootstrap();
            // 3.设置参数
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new ProtocolMessageDecoder())//加入解码器
                                    .addLast(new NettyClientHandler(completableFuture))//加入业务处理器
                                    .addLast(new ProtocolMessageEncoder());//加入编码器
                        }
                    });
            // 4.连接服务端
            channel = bootstrap.connect(serverHost, serverPort).sync().channel();

            channel.writeAndFlush(requestMessage);

            RpcResponse rpcResponse = completableFuture.get();
            return rpcResponse;
            // 等待关闭
//            channel.closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                channel.close();
            }
            if (group != null) {
                group.shutdownGracefully();
            }
        }
    }

}
