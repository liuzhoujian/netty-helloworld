package com.protocol.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import com.protocol.ProtocolStringUtils;

@ChannelHandler.Sharable
public class MyClientHandler extends ChannelHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            System.out.println("接受到服务端的原始数据：" + msg);

            String content = ProtocolStringUtils.parse(msg.toString());
            //说明服务器给客户端传输的数据不完整
            if(content == null) {
                return;
            }

            System.out.println("来自服务器:" + content);
        } finally {
            //用于释放缓存，防止内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }
}
