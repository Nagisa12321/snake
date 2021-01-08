package com.jtchen;

import com.struct.ClientInfo;
import com.struct.Point;
import com.struct.Snake;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("InfiniteLoopStatement")
public class UDPServerMain implements Runnable {
    public static final int PORT = 8088;

    public static Color randomColor() {

        float x = (float) (Math.random() * 255);
        float y = (float) (Math.random() * 255);
        float z = (float) (Math.random() * 255);

        return Color.getHSBColor(x, y, z);
    }

    @Override
    public void run() {
        System.out.println("已开启服务器main函数!");

        // main和GetOperation维护的玩家列表
        BlockingQueue<ClientInfo> clientInfos = new LinkedBlockingQueue<>();

        // SendSnakes和GetOperation维护的操作队列
        BlockingQueue<String> operation = new LinkedBlockingQueue<>();

        // main和SendSnakes维护的snakes map
        // SendSnakes如果move snake失败了可以从表中删除
        HashMap<String, Snake> snakes = new HashMap<>();

        // main和Snake维护的身体点集
        HashSet<Point> body = new HashSet<>();

        // 开启收线程
        new Thread(new SendSnakes(clientInfos, operation, snakes, body)).start();
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
                String name = new String(packet.getData(),
                        0, packet.getLength(), StandardCharsets.UTF_8);

                System.out.println("有位玩家进入了服务器 id: " + name);

                Point p1 = new Point(1, 0);
                Point p2 = new Point(1, 1);
                Point p3 = new Point(1, 2);
                Point p4 = new Point(1, 3);

                Snake snake = new Snake(new Point[]{p1, p2, p3, p4}, randomColor());

                // 加入点集
                body.add(p1);
                body.add(p2);
                body.add(p3);
                body.add(p4);

                // 在只读的HashMap 中存入Snake
                snakes.put(name, snake);

                for (var entry : snakes.entrySet()) {
                    System.out.println("name " + entry.getKey());
                    System.out.println("name.size()" + name.length());
                    System.out.println("snake " + entry.getValue().getDirection());
                }

                // 发送链接成功的消息
                socket.send(new DatagramPacket(new byte[1], 1, clientIP, clientPort));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
