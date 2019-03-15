package com.c_serilizable.server;

import com.c_serilizable.GzipUtils;
import com.c_serilizable.UserInfo;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable //多个客户端共享，节省资源但会出现安全性问题
public class MyServerHandler extends ChannelHandlerAdapter {

    /**
     * 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().localAddress().toString()+" channelActive");
    }

    /**
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().localAddress().toString()+" channelInactive");
    }

    /**
     * 在通道读取完成后会在这个方法里通知，对应可以做刷新操作
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 从通道中读取数据，但是在不进行解码时时ByteBuf类型
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof UserInfo) {
            UserInfo userInfo = (UserInfo) msg;
            System.out.println("来自客户端：" + userInfo);
            byte[] source = userInfo.getAttachment();
            System.out.println("来自客户端未解压前：" + source.length);
            byte[] target = GzipUtils.unzip(source);
            System.out.println("来自客户端解压后：" + target.length);
            System.out.println("来自客户端附件信息：" + new String(target, "UTF-8"));
            //向客户端回显
            ctx.writeAndFlush(userInfo);
        } else {
            System.out.println(msg.toString());
        }
    }

    /**
     * 统一异常处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        System.out.println("异常信息：\r\n"+cause.getMessage());
    }
}
