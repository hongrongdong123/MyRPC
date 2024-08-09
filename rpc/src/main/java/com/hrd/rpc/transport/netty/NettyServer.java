package com.hrd.rpc.transport.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * description:
 */
public class NettyServer {
    private static NioEventLoopGroup bossGroup;
    private static NioEventLoopGroup workerGroup;

    public static void startServer(String serverHost, Integer serverPort) {
        try {
            // 1.创建线程组
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            // 2.创建服务端启动助手
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3.设置参数
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new ProtocolMessageDecoder())//加入解码器
                                    .addLast(new NettyServerHandler())//加入业务处理器
                                    .addLast(new ProtocolMessageEncoder());//加入编码器


                        }
                    });
            // 4.绑定端口
            ChannelFuture sync = serverBootstrap.bind(serverHost, serverPort).sync();
            System.out.println("==========服务端启动成功,地址为： " + serverHost + ":" + serverPort + "==========");
            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }

            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }
    }

    public void destroy() throws Exception {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

}
