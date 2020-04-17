package com.imooc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO服务器端
 * @Author fate7
 * @Date 2020/4/17 1:41 下午
 **/
public class NioServer {

    /**
     * 启动方法
     */
    public void start() throws IOException {
        //1. 创建Selector
        Selector selector = Selector.open();

        //2. 通过ServerSocketChannel创建channel通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //3. 为channel绑定端口
        serverSocketChannel.bind(new InetSocketAddress(8000));

        //4. **设置channel为非阻塞模式**
        serverSocketChannel.configureBlocking(false);

        //5. 将channel注册到selector上，监听连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动。。。");
        //6. 循环等待新接入的连接
        for (;;){
            //获取可用的channel数量
            int readyChannels = selector.select();
            //TODO 防止空轮询
            if (readyChannels == 0) continue;

            //获取可用channel集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();
                //** 移除Set中的当前SelectionKey**
                iterator.remove();


                //7. 根据就绪状态，调用对应的方法处理业务逻辑
                /*
                如果是接入事件 do something
                 */
                if (next.isAcceptable()) {
                    this.accpectHandler(serverSocketChannel, selector);
                }
                /*
                如果是读写事件 do something
                 */
                if (next.isReadable()) {
                    this.readHandler(next, selector);
                }
            }

        }

    }


    private void accpectHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        /**
         * 如果是接入事件 创建socketChannel
         * 将socketChannel设置为非阻塞工作模式
         * 将channel注册到selector上，监听 可读事件
         * 回复客户端信息
         */

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.write(Charset.forName("UTF-8")
                .encode("你与聊天室里其他人都不是朋友关系，请注意隐私安全"));
    }

    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        /**
         * 要从selectionKey中获取到已经就绪的channel
         * 创建buffer
         * 循环读取客户端请求信息
         * 将channel再次注册到selector上，监听他的可读事件
         * 将客户端发送的请求信息，广播到其他客户端
         */

        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        String request = "";
        while (socketChannel.read(byteBuffer) > 1) {
            //切换为读模式
            byteBuffer.flip();
            request += Charset.forName("UTF-8").decode(byteBuffer);
        }
        socketChannel.register(selector, SelectionKey.OP_READ);
        if (request.length() > 0) {
            this.broadCast(selector, socketChannel, request);
        }
    }

    private void broadCast(Selector selector, SocketChannel sourceChannel, String request) {
        /**
         * 获取到所有已接入的客户端channel
         * 循环向所有channel广播信息
         */
        Set<SelectionKey> selectionKeySet = selector.keys();
        selectionKeySet.forEach(selectionKey -> {
            SelectableChannel targetChannel = selectionKey.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != sourceChannel) {
                try {
                    ((SocketChannel)targetChannel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
