package com.d_timerdisconnect.client;

import com.d_timerdisconnect.GzipUtils;
import com.d_timerdisconnect.UserInfo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class MyClientHandler extends ChannelHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof UserInfo) {
                UserInfo userInfo = (UserInfo) msg;
                System.out.println("来自服务器:" + userInfo);
                byte[] source = userInfo.getAttachment();
                System.out.println("来自服务器未解压前：" + source.length);
                byte[] target = GzipUtils.unzip(source);
                System.out.println("来自服务器解压后：" + target.length);
                System.out.println("来自服务器附件信息：" + new String(target, "UTF-8"));

            } else {
                System.out.println(msg.toString());
            }

        } finally {
            //用于释放缓存，防止内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //初次连接成功时，可以进行一些初始化，或向服务端发送数据
    }

}
