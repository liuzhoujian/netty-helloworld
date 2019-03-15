package com.b_protocol.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import com.b_protocol.ProtocolStringUtils;

import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {
    private Bootstrap bootstrap = null;

    private EventLoopGroup group = null;

    public Client() {
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
                ChannelHandler[] channelHandlers = new ChannelHandler[2];
                //字符串解码
                channelHandlers[0] = new StringDecoder();
                //自己定义的处理器
                channelHandlers[1] = new MyClientHandler();

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
        Client client = null;
        ChannelFuture future = null;
        try {
            client = new Client();
            future = client.doRequest("localhost", 8080);
            Scanner in = new Scanner(System.in);
            while(true) {
                System.out.print("输入exit退出:");
                String s = in.nextLine();

                if("exit".equals(s)) {
                    future.channel().writeAndFlush(Unpooled.copiedBuffer(s.getBytes("UTF-8")))
                            .addListener(ChannelFutureListener.CLOSE);
                }

                //组装成特殊格式
                String proStr = ProtocolStringUtils.transferTo(s);
                future.channel().writeAndFlush(Unpooled.copiedBuffer(proStr.getBytes("UTF-8")));

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
