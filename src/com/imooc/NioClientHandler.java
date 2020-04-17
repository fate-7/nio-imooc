package com.imooc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author fate7
 * @Date 2020/4/17 2:22 下午
 **/
public class NioClientHandler implements Runnable {

    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try{
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

                    /*
                    如果是读写事件 do something
                     */
                    if (next.isReadable()) {
                        this.readHandler(next, selector);
                    }
                }

            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        /**
         * 要从selectionKey中获取到已经就绪的channel
         * 创建buffer
         * 循环读取客户端请求信息
         * 将channel再次注册到selector上，监听他的可读事件
         * 将服务器端响应信息打印到本地
         */

        SocketChannel socketChannel = (SocketChannel)selectionKey.channel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        String response = "";
        while (socketChannel.read(byteBuffer) > 1) {
            //切换为读模式
            byteBuffer.flip();
            response += Charset.forName("UTF-8").decode(byteBuffer);
        }
        socketChannel.register(selector, SelectionKey.OP_READ);
        if (response.length() > 0) {
            System.out.println("::" + response);
        }
    }
}
