package com.jtchen.Thread;

import com.jtchen.ClientInfo;
import com.jtchen.PlayerMap;
import com.struct.Point;
import com.struct.Snake;
import com.struct.UDPSnake;

import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class SendSnakes implements Runnable {
    public static final int LENGTH = 30; // 真实长宽

    private Vector<ClientInfo> clientInfos; // 用户IP PORT列表

    private Vector<String> operation; // 操作队列

    private HashMap<String, Snake> snakes; // 蛇

    private Point foodPoint; // 食物的点

    private HashSet<Point> body; // 身体点集

    public SendSnakes(Vector<ClientInfo> clientInfos,
                      Vector<String> operation,
                      HashMap<String, Snake> snakes,
                      HashSet<Point> body) {
        this.clientInfos = clientInfos;
        this.operation = operation;
        this.snakes = snakes;
        this.body = body;

        // 地图随机生成食物
        GenerateFood();
    }


    public void run() {
        while (true) {
            if (clientInfos.isEmpty()) continue;
            if (!operation.isEmpty()) {
                // 解析一个Snake的name和动作
                NameAndOperation nAo = getNameAndOperation();
                String name = nAo.name;
                String operation = nAo.Op;

                // 由name得到蛇
                Snake snake = snakes.get(name);

                // 由operation 和 具体snake操作蛇
                // 如果snake 撞到body, 则移除snake
                if (!moveSnake(operation, snake)) {
                    snakes.remove(name);
                }

                // 遍历玩家列表 发送UDPSnakes给玩家们
            }
        }
    }

    /* 解析字符串 获得name和op */
    public NameAndOperation getNameAndOperation() {
        // 出队一个name 和 op 的 String 并且解析一番
        String s = operation.remove(0);

        String[] tmp = s.split(" ");
        String name = tmp[0];
        String op = tmp[1];
        return new NameAndOperation(name, op);
    }

    /* 根据出队的操作移动某条蛇, 并且改变蛇的方向 */
    public boolean moveSnake(String operation, Snake snake) {
        int direction = snake.getDirection();

        int x = snake.getHead().x();
        int y = snake.getHead().y();

        // 判断是否方向改变 并且重新改变方向
        switch (operation) {
            // 0上 1下 2左 3右
            case "left":
                if (direction != 3) {
                    Point movePoint = new Point(x - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--x) : --x, y);
                    snake.setDirection(2);

                    // 如果将要遇到的是食物, 则在生成食物
                    if (movePoint.equals(foodPoint)) GenerateFood();

                    // 如果移动蛇前面是body则移动失败
                    // 返回false
                    return snake.move(movePoint, foodPoint, body);
                }
            case "up":
                if (direction != 1) {
                    Point movePoint = new Point(x, y - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--y) : --y);
                    snake.setDirection(0);
                    if (movePoint.equals(foodPoint)) GenerateFood();
                    return snake.move(movePoint, foodPoint, body);
                }
            case "right":
                if (direction != 2) {
                    Point movePoint = new Point(++x % PlayerMap.LENGTH, y);
                    snake.setDirection(3);
                    if (movePoint.equals(foodPoint)) GenerateFood();
                    return snake.move(movePoint, foodPoint, body);
                }
            case "down":
                if (direction != 0) {
                    Point movePoint = new Point(x, ++y % PlayerMap.LENGTH);
                    snake.setDirection(1);
                    if (movePoint.equals(foodPoint)) GenerateFood();
                    return snake.move(movePoint, foodPoint, body);
                }
        }
        // 如果什么都没发生则返回true
        // 比如说蛇的方向是向前, 你按了后, 则什么也没发生
        // 换句话说 蛇不死的话返回true
        return true;
    }

    // 在地图随机一点生成食物
    public void GenerateFood() {
        int x = (int) (Math.random() * LENGTH);
        int y = (int) (Math.random() * LENGTH);

        Point tmp = new Point(x, y);
        if (body.contains(tmp)) {
            GenerateFood();
        } else
            foodPoint = new Point(x, y);
    }

    /* 向每个玩家发送UDPSnakes */
    public void SendUDPSnakes() throws IOException {
        UDPSnake snake = new UDPSnake(snakes, foodPoint);

        for (var c : clientInfos) {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(new byte[0], 0, InetAddress.getByName("127.0.0.1"), 1688);
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

            // 转为Object流
            ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream);
            objectStream.writeObject(snake);
            byte[] arr = byteArrayStream.toByteArray();
            packet.setData(arr);//填充DatagramPacket
            socket.send(packet);//发送
            objectStream.close();
            byteArrayStream.close();
        }
    }

    public static class NameAndOperation {
        private final String name;
        private final String Op;


        public NameAndOperation(String name, String op) {
            this.name = name;
            this.Op = op;
        }
    }
}
