package com.jtchen.Thread;

import com.jtchen.ClientInfo;
import com.struct.Point;
import com.struct.Snake;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

@SuppressWarnings("InfiniteLoopStatement")
public class UDPServerMain {
    public static final int PORT = 8088;

    public static Color randomColor() {
        Color[] colors = {
                Color.BLACK, Color.cyan,
                Color.YELLOW, Color.LIGHT_GRAY,
                Color.BLUE, Color.ORANGE,
                Color.pink
        };

        int x = (int) (Math.random() * 100) / colors.length;
        return colors[x];
    }

    public static void main(String[] args) {
        // main和GetOperation维护的玩家列表
        Vector<ClientInfo> clientInfos = new Vector<>();

        // SendSnakes和GetOperation维护的操作队列
        Vector<String> operation = new Vector<>();

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
                String name = new String(packet.getData());

                Point p1 = new Point(1, 0);
                Point p2 = new Point(1, 1);
                Point p3 = new Point(1, 2);

                Snake snake = new Snake(new Point[]{p1, p2, p3}, randomColor());

                // 加入点集
                body.add(p1);
                body.add(p2);
                body.add(p3);

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
