package com.d_timerdisconnect.client;

import com.d_timerdisconnect.GzipUtils;
import com.d_timerdisconnect.MarshallingCodeFactory;
import com.d_timerdisconnect.UserInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class TimerClient {
    private Bootstrap bootstrap = null;

    private EventLoopGroup group = null;

    public TimerClient() {
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
                e.pipeline().addLast(new WriteTimeoutHandler(3));
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
        TimerClient client = null;
        ChannelFuture future = null;
        try {
            client = new TimerClient();
            future = client.doRequest("localhost", 8080, new MyClientHandler());

            System.out.println("与服务器端的连接建立成功，向服务端传送对象");

            for (int i = 0; i < 3; i++) {
                String attachment = "这是一个附件信息：它很大，在传输时需要压缩发送：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
                byte[] target = GzipUtils.zip(attachment.getBytes("UTF-8"));
                UserInfo userInfo = new UserInfo("张三", 18, "男",
                        "zhangsan@163.com", "西安市", target);

                //向服务器发送对象
                future.channel().writeAndFlush(userInfo);
                TimeUnit.SECONDS.sleep(2);
            }

            //过3s之后自动关闭连接

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
