package com.b_fixedlength.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class FixedlengthClient {
    private Bootstrap bootstrap = null;

    private EventLoopGroup group = null;

    public FixedlengthClient() {
        init();
    }

    private void init() {
        //初始化处理线程
        group = new NioEventLoopGroup();
        //初始化客户端服务配置
        bootstrap = new Bootstrap();
        //绑定线程
        bootstrap.group(group);
        //设置通信模式NIO
        bootstrap.channel(NioSocketChannel.class);
    }

    /**
     * 发起请求
     * @param host
     * @param port
     * @return
     * @throws InterruptedException
     */
    private ChannelFuture doRequest(String host, int port)
            throws InterruptedException {

        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel e) throws Exception {

                ChannelHandler[] channelHandlers = new ChannelHandler[3];
                channelHandlers[0] = new FixedLengthFrameDecoder(3);
                //字符串解码
                channelHandlers[1] = new StringDecoder();
                //自己定义的处理器
                channelHandlers[2] = new MyClientHandler();
                e.pipeline().addLast(channelHandlers);
            }
        });

        ChannelFuture future = this.bootstrap.connect(host, port).sync();
        return future;
    }

    public void release() {
        group.shutdownGracefully();
    }

    public static void main(String[] args) {
        FixedlengthClient client = null;
        ChannelFuture future = null;
        try {
            client = new FixedlengthClient();
            future = client.doRequest("localhost", 8080);
            Scanner in = new Scanner(System.in);
            while(true) {
                System.out.print("输入exit退出:");
                String s = in.nextLine();

                if("exit".equals(s)) {
                    //addListener当某条件满足时触发监听器
                    //ChannelFutureListener.CLOSE 关闭监听器，代表党ChannelFuture返回后，关闭连接
                    future.channel().writeAndFlush(Unpooled.copiedBuffer(s.getBytes("UTF-8")))
                            .addListener(ChannelFutureListener.CLOSE);
                }

                future.channel().writeAndFlush(Unpooled.copiedBuffer(s.getBytes("UTF-8")));
                TimeUnit.SECONDS.sleep(1);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            //释放资源
            if(future != null) {
                try {
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            client.release();
        }
    }
}
