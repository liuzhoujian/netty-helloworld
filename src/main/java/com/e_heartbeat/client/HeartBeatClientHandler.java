package com.e_heartbeat.client;

import com.e_heartbeat.HeartBeatMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.hyperic.sigar.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class HeartBeatClientHandler extends ChannelHandlerAdapter {

    /*定时任务的线程池*/
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture heartbeat;

    private InetAddress remoteAddr;
    private static final String HEARTBEAT_SUCCESS = "SERVER_RETURN_HEARTBEAT_SUCCESS";


    /**
     * 客户端连接到服务器就给服务器发送身份验证信息
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //获取INET信息
        this.remoteAddr = InetAddress.getLocalHost();
        //获取计算机名
        String computerName = System.getenv().get("COMPUTERNAME");
        //组装成身份信息
        String credential = this.remoteAddr.getHostAddress() + "_" + computerName;
        System.out.println(credential);
        //发送到服务端
        ctx.writeAndFlush(credential);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof String) {
                if(HEARTBEAT_SUCCESS.equals(msg.toString())) {
                    //来自服务端的身份验证反馈信息：成功收到服务端的反馈后
                    //开启定时任务线程池，每隔2秒发送一次心跳信息
                    this.heartbeat = this.executorService.scheduleWithFixedDelay(new HeartBeatTask(ctx), 0L, 2L, TimeUnit.SECONDS);
                    System.out.println("client rcv:" + msg);
                } else {
                    System.out.println("client rcv:" + msg);
                }
            }
        } finally {
            //用于释放缓存，防止内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //回收资源
        System.out.println("client: exception caught method running... ");
        if(this.heartbeat != null) {
            this.heartbeat.cancel(true);
            this.heartbeat = null;
        }

        ctx.close();
    }

    /**
     * 发送心跳信息
     */
    private class HeartBeatTask implements Runnable {
        private ChannelHandlerContext ctx;

        public HeartBeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void run() {
            try {
                HeartBeatMessage msg = new HeartBeatMessage();
                msg.setIp(remoteAddr.getHostAddress());
                Sigar sigar = new Sigar();
                //CPU信息
                CpuPerc cpuPerc = sigar.getCpuPerc();
                Map<String, Object> cpuMsgMap = new HashMap<String, Object>();
                cpuMsgMap.put("Combined", cpuPerc.getCombined());
                cpuMsgMap.put("User", cpuPerc.getUser());
                cpuMsgMap.put("Sys", cpuPerc.getSys());
                cpuMsgMap.put("Wait", cpuPerc.getWait());
                cpuMsgMap.put("Idle", cpuPerc.getIdle());
                //内存信息
                Mem mem = sigar.getMem();
                Map<String, Object> memMsgMap = new HashMap<String, Object>();
                memMsgMap.put("Total", mem.getTotal());
                memMsgMap.put("Used", mem.getUsed());
                memMsgMap.put("Free", mem.getFree());
                //文件系统
                FileSystem[] fileSystemList = sigar.getFileSystemList();
                Map<String, Object> fileSysMsgMap = new HashMap<String, Object>();
                fileSysMsgMap.put("FileSysCount", fileSystemList.length);
                List<String> msgList = null;
                for (FileSystem fs: fileSystemList) {
                    msgList = new ArrayList<String>();
                    msgList.add(fs.getDevName() + "总大小：" + sigar.getFileSystemUsage(fs.getDirName()).getTotal());
                    msgList.add(fs.getDevName() + "剩余大小：" + sigar.getFileSystemUsage(fs.getDirName()).getFree());
                    fileSysMsgMap.put(fs.getDevName(), msgList);
                }

                msg.setCpuMsgMap(cpuMsgMap);
                msg.setFileSysMsgMap(fileSysMsgMap);
                msg.setMemMsgMap(memMsgMap);

                /*将心跳包发送给服务端*/
                ctx.writeAndFlush(msg);
            } catch (SigarException e) {
                e.printStackTrace();
            }
        }
    }
}
/*
* 客户端在连接成功后，会首先将身份信息发送给服务端，在服务端进行验证，并给客户端返回验证结果。
* HeartBeatSuccess 身份验证成功，在客户端开启心跳任务，每个一段时间给服务器发送客户机的状态信息
* 失败，结束客户端
* */