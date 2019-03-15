package com.b_protocol.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import com.b_protocol.ProtocolStringUtils;

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
        //接收到原始的消息数据，如：HEADcontent-length:5HEADBODYhelloBODY
        String s = msg.toString();

        System.out.println("接受到客户端的原始数据:" + s);

        //首先校验头部的数据长度和内容数据的长度是否一致，之后将有用的内容信息解析出来
        String content = ProtocolStringUtils.parse(s);

        //长度不一致，说明此数据库不完整，可以直接丢弃。
        if(content == null) {
            return;
        }

        if("exit".equals(content)) {
            ctx.close();
            return;
        }

        System.out.println("接受到客户端的消息：" + content);

        //将服务器的数据组装成特殊格式发送
        String line = "message server to client";
        String messOut = ProtocolStringUtils.transferTo(line);
        //写操作后自动释放内存，防止内存溢出
        ctx.writeAndFlush(Unpooled.copiedBuffer(messOut.getBytes("UTF-8")));
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
