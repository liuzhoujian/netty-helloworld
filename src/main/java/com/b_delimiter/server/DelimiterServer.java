package com.b_delimiter.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.Charset;

public class DelimiterServer {
    //服务端启动类
    private ServerBootstrap serverBootstrap;
    //监听链接的线程组
    private EventLoopGroup boss;
    //处理连接的线程组
    private EventLoopGroup worker;

    public DelimiterServer() {
        init();
    }

    private void init() {
        //初始化服务配置
        serverBootstrap = new ServerBootstrap();

        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        //绑定线程组
        serverBootstrap.group(boss, worker);

        //设置通信模式为NIO,同步非阻塞
        serverBootstrap.channel(NioServerSocketChannel.class);

        //设置TCP参数，连接请求的最大队列长度
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

      /*  serverBootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true); //心跳检测*/
    }

    private ChannelFuture doAccept(int port) throws InterruptedException {
        //增加处理handler,一次可以添加多个，责任链模式
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel e) throws Exception {
                System.out.println("报告");
                System.out.println("信息：有一客户端链接到本服务端");
                System.out.println("IP:"+e.localAddress().getHostName());
                System.out.println("Port:"+e.localAddress().getPort());
                System.out.println("报告完毕");

                ChannelHandler[] channelHandlers = new ChannelHandler[3];

                ByteBuf delimiter = Unpooled.copiedBuffer("$E$".getBytes("UTF-8"));
                channelHandlers[0] = new DelimiterBasedFrameDecoder(1024, delimiter);

                //字符串解码
                channelHandlers[1] = new StringDecoder(Charset.forName("UTF-8"));
                //自定义处理器
                channelHandlers[2] = new MyServerHandler();

                //添加处理器
                e.pipeline().addLast(channelHandlers);
            }
        });

        //绑定端口，并执行
        ChannelFuture channelFuture = this.serverBootstrap.bind(port).sync();
        return channelFuture;
    }

    //shutdownGracefully 安全的关闭线程池，确保已经连接的线程在处理完后再关闭
    private void release() {
        this.boss.shutdownGracefully();
        this.worker.shutdownGracefully();
    }

    public static void main(String[] args) {
        DelimiterServer server = null;
        ChannelFuture future = null;
        try {
            server = new DelimiterServer();
            future = server.doAccept(8080);
            System.out.println("server start!");

            //关闭连接
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭资源
            if(future != null) {
                try {
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(server != null) {
                server.release();
            }
        }
    }
}
