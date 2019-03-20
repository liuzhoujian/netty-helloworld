package com.e_heartbeat.server;

import com.e_heartbeat.HeartBeatMessage;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

@ChannelHandler.Sharable
public class HeartBeatServerHandler extends ChannelHandlerAdapter {

    /*保存校验的身份信息列表*/
    private static List<String> credentials = new ArrayList<String>();

    /*服务端给客户端反馈的信息*/
    private static final String HEARTBEAT_SUCCESS = "SERVER_RETURN_HEARTBEAT_SUCCESS";

    public HeartBeatServerHandler() {
        credentials.add("192.168.154.1_DESKTOP-BAAET0D");
    }

    /**
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().localAddress().toString()+" channelActive");
    }


    /**
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof String) {
            this.checkCredential(ctx, msg.toString());
        } else if(msg instanceof HeartBeatMessage) {
            this.readHeartBeatMessage(ctx, msg.toString());
        } else {
            ctx.writeAndFlush("wrong message").addListener(ChannelFutureListener.CLOSE);
        }

    }

    /**
     * 读取心跳信息：在这里只是简单的显示，可以使用折线图显示状态信息更加直观
     * @param ctx
     * @param msg
     */
    private void readHeartBeatMessage(ChannelHandlerContext ctx, String msg) {
        System.out.println(msg);
        System.out.println("=========================");
        ctx.writeAndFlush("server received heartbeat message");

    }

    /**
     * 校验身份
     * 成功给客户端返回：HEARTBEAT_SUCCESS
     * 失败给客户端返回：no credential contains
     * 其实可以从文件或者数据库中定制身份策略，再从数据库或文件中读取身份信息进行验证
     * @param ctx
     * @param credential
     */
    private void checkCredential(ChannelHandlerContext ctx, String credential) {
        if(credentials.contains(credential)) {
            ctx.writeAndFlush(HEARTBEAT_SUCCESS);
        } else {
            ctx.writeAndFlush("no credential contains").addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().localAddress().toString()+" channelInactive");
    }

    /**
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
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
