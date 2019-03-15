package com.c_serilizable.client;

import com.c_serilizable.MarshallingCodeFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.nio.charset.Charset;

public class SerializableClient {
    private Bootstrap bootstrap = null;

    private EventLoopGroup group = null;

    public SerializableClient() {
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
    private ChannelFuture doRequest(String host, int port, final ChannelHandler... channelHandlers)
            throws InterruptedException {

        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel e) throws Exception {
                e.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
                e.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());
                e.pipeline().addLast(new StringDecoder(Charset.forName("UTF-8")));
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
        SerializableClient client = null;
        ChannelFuture future = null;
        try {
            client = new SerializableClient();
            future = client.doRequest("localhost", 8080, new MyClientHandler());

            //1s后关闭客户端
            /*TimeUnit.SECONDS.sleep(1);
            future.addListener(ChannelFutureListener.CLOSE);
            */
        } catch (InterruptedException e) {
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
