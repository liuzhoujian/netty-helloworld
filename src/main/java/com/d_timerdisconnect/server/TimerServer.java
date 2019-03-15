package com.d_timerdisconnect.server;

import com.d_timerdisconnect.MarshallingCodeFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.nio.charset.Charset;

public class TimerServer {

    //服务端核心组件
    private ServerBootstrap serverBootstrap;

    //监听链接线程池
    private EventLoopGroup boss = null;
    //处理连接线程池
    private EventLoopGroup worker = null;

    public TimerServer() {
        init();
    }

    /**
     * 初始化资源
     */
    private void init() {
        serverBootstrap = new ServerBootstrap();
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        //组合
        serverBootstrap.group(boss, worker);
        //设置通信模式NIO
        serverBootstrap.channel(NioServerSocketChannel.class);
        //设置TCP参数，连接请求的最大队列长度
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

        serverBootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    /**
     * 等待客户端连接
     * @param port
     * @param channelHandlers
     * @return
     */
    public ChannelFuture doAccept(int port, final ChannelHandler... channelHandlers) throws InterruptedException {
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel e) throws Exception {
                //定时断线处理器，3秒内没有读取到数据，则断开连接。
                e.pipeline().addLast(new ReadTimeoutHandler(3));
                e.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());
                e.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
                e.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));
                e.pipeline().addLast(channelHandlers);
            }
        });

        //绑定端口
        ChannelFuture future = this.serverBootstrap.bind(port).sync();

        return future;
    }

    /**
     * 优雅关闭资源
     */
    private void release() {
        this.boss.shutdownGracefully();
        this.worker.shutdownGracefully();
    }

    public static void main(String[] args) throws InterruptedException {
        TimerServer server = null;
        ChannelFuture future = null;

        try {
            server = new TimerServer();
            future = server.doAccept(8080, new MyServerHandler());
            System.out.println("server start!");
            //关闭服务器
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(future != null) {
                future.channel().closeFuture().sync();
            }

            server.release();
        }
    }
}
