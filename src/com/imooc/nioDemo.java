package com.imooc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @Author fate7
 * @Date 2020/4/17 1:01 下午
 **/
public class nioDemo {

    public static void main(String[] args) throws IOException {
        /**
         * 服务端通过服务端socket创建channel
         */
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        /**
         * 服务端绑定端口
         */
        serverSocketChannel.bind(new InetSocketAddress(8000));

        /**
         * 服务端监听客户端连接，建立socketChannel连接
         */
        SocketChannel socketChannel = serverSocketChannel.accept();

        /**
         * 客户端连接远程主机及端口
         */
        SocketChannel socketChannel1 = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));


        /**
         * 初始化长度为10的byte类型buffer
         * Position = 0, Limit=Capacity=10
         */
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        /**
         * 向byteBuffer中写入三个字节
         * Position = 3, Limit=Capacity=10
         */
        byteBuffer.put("abc".getBytes(Charset.forName("UTF-8")));

        /**
         * 将byteBuffer从写模式切换成读模式
         * Position = 0, Limit=3, Capacity=10
         */
        byteBuffer.flip();

        /**
         * 从byteBuffer中读取一个字节
         * Position = 1, Limit=3, Capacity=10
         */
        byteBuffer.get();

        /**
         * 调用mark方法记录下当前Position的位置
         * Mark=1, Position = 1, Limit=3, Capacity=10
         */
        byteBuffer.mark();

        /**
         * 先调用get方法读取下一个字节
         * 在调用reset方法将Position重置到mark位置
         * Mark=1, Position = 1 -> 2 -> 1, Limit=3, Capacity=10
         */
        byteBuffer.get();
        byteBuffer.reset();

        /**
         * 调用clear方法，将所有属性重置
         * Position = 3, Limit=Capacity=10
         */
        byteBuffer.clear();

        /**
         * 创建Selector
         */
        Selector selector = Selector.open();

        /**
         * 将channel注册到Selector上，监听读就绪事件
         */
        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         * 阻塞等待channel有就绪事件发生
         */
        int selectNum = selector.select();

        /**
         * 获取发生就绪事件的channel集合
         */
        Set<SelectionKey> selectionKeys = selector.selectedKeys();

    }
}
