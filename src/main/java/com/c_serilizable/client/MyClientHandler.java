package com.c_serilizable.client;

import com.c_serilizable.GzipUtils;
import com.c_serilizable.UserInfo;
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
        System.out.println("与服务器端的连接建立成功，向服务端传送对象");

        String attachment = "这是一个附件信息：它很大，在传输时需要压缩发送：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        byte[] target = GzipUtils.zip(attachment.getBytes("UTF-8"));
        UserInfo userInfo = new UserInfo("张三", 18, "男",
                "zhangsan@163.com", "西安市", target);

        //向服务器发送对象
        ctx.writeAndFlush(userInfo);
    }

}
