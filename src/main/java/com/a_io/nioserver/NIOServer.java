package com.nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 简单的NIO服务器
 */
public class NIOServer {
    private Selector selector;

    /**
     * 获取一个ServerSocketChannel，并进行初始化操作
     * @param port
     * @throws IOException
     */
    public void initServer(int port) throws IOException {
        //获取一个ServerSocket通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //设置该通道非阻塞
        serverSocketChannel.configureBlocking(false);
        //为该通道的socket绑定地址和端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port));

        //创建一个selector管理器
        this.selector = Selector.open();

        //将该管理器注册到serverSocket通道中,并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
        //当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
        serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     */
    public void listen() throws Exception {
        System.out.println("服务端已启动！");

        //轮询访问selector
        while(true) {
            // 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
            selector.select();

            Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
            while(iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                // 删除已选的key,以防重复处理
                iterator.remove();

                //处理selectKey
                handler(selectionKey);
            }

        }
    }

    /**
     * 处理请求
     * @param selectionKey
     */
    public void handler(SelectionKey selectionKey) throws Exception {
        // 客户端请求连接事件
        if(selectionKey.isAcceptable()) {
            handlerAccept(selectionKey);
        }
        // 可读事件
        else if(selectionKey.isReadable()) {
            handlerRead(selectionKey);
        }

    }

    /**
     * 处理连接请求
     * @param selectionKey
     * @throws IOException
     */
    private void handlerAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        //获取和客户端的连接
        SocketChannel channel = serverSocketChannel.accept();
        //设置成非阻塞
        channel.configureBlocking(false);
        System.out.println("新的客户端连接");
        // 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    /**
     * 处理读请求
     * @param selectionKey
     * @throws IOException
     */
    private void handlerRead(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        //创建字节缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(buffer);
        if(read > 0) {
            byte[] data = buffer.array();
            String message = new String(data).trim();
            System.out.println("服务端收到消息： " + message);

            //回显数据
            ByteBuffer outBuffer = ByteBuffer.wrap("好的，".getBytes());
            socketChannel.write(outBuffer);
        } else {
            selectionKey.cancel();
            System.out.println("客户端关闭！");
        }
    }



    public static void main(String[] args) {
        try {
            NIOServer nioServer = new NIOServer();
            nioServer.initServer(8000);
            nioServer.listen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
