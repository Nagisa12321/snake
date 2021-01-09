package com.jtchen;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

public class UDPServerReceive implements Runnable {
    public static final int PORT = 8090;

    private final BlockingQueue<String> operation;

    public UDPServerReceive(BlockingQueue<String> operation) {
        this.operation = operation;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        // 不断等待接收链接
        while (true) {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {

                byte[] buff = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buff, 4096);
                socket.receive(packet);

                // 收到的是String 是操作和name组合,
                String opStr = new String(buff,
                        0, packet.getLength(), StandardCharsets.UTF_8);

                // 加入队列中, 让SendSnakes解析操作并且处理
                operation.put(opStr);
            } catch (IOException | InterruptedException e) {
                System.err.println(e.getMessage() + "(UDPServerReceive)");
            }
        }
    }
}
