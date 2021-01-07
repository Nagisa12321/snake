package com.jtchen.Thread;

import com.jtchen.ClientInfo;
import com.struct.Point;
import com.struct.Snake;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Vector;

public class UDPServerMain {
    public static final int PORT = 8088;

    public static void main(String[] args) {
        Vector<ClientInfo> clientInfos = new Vector<>();
        Vector<String> operation = new Vector<>();
        HashMap<String, Snake> snakes = new HashMap<>();

        // 开启收线程
        new Thread(new SendSnakes(clientInfos, operation, snakes)).start();
        new Thread(new GetOperation(operation)).start();

        while (true) {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {

                // 建立链接
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);
                int clientPort = packet.getPort();
                InetAddress clientIP = packet.getAddress();

                // 新增玩家
                clientInfos.add(new ClientInfo(clientPort, clientIP));

                // 新增蛇
                String name = new String(packet.getData());
                Snake snake = new Snake(new Point[]{new Point(1, 0), new Point(1, 1),
                        new Point(1, 2), new Point(1, 3), new Point(1, 4)}, Color.pink);

                // 在只读的HashMap 中存入Snake
                snakes.put(name, snake);

                // 发送链接成功的消息
                socket.send(new DatagramPacket(new byte[1], 1));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
