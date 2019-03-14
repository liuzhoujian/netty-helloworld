package com.fixedlength.client;

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
           /* ByteBuf byteBuf = (ByteBuf)msg;
            byte[] msgByte = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(msgByte);
             String s = new String(msgByte, "UTF-8");
            */
            System.out.println("来自服务器:" + msg.toString());
        } finally {
            //用于释放缓存，防止内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }
}
