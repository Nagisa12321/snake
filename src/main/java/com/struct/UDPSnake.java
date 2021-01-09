package com.struct;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class UDPSnake implements Serializable {
    private final HashMap<String, Snake> snakes;
    private final HashSet<Point> foodPoints;

    public UDPSnake(HashMap<String, Snake> snakes, HashSet<Point> foodPoints) {
        this.snakes = snakes;
        this.foodPoints = foodPoints;
    }


    public HashMap<String, Snake> getSnakes() {
        return snakes;
    }

    public HashSet<Point> getFood() {
        return foodPoints;
    }


    /* 将类转化为String来发送, 减少网络占用 */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // 存HashMap
        // 存Snakes数量 耗费1
        builder.append(snakes.size())
                .append(" ");
        for (var entry : snakes.entrySet()) {

            // 存名字 耗费 1
            builder.append(entry.getKey())
                    .append(" ");

            Snake snake = entry.getValue();

            Point head = snake.getHead();
            Color color = snake.getColor();
            Direction direction = snake.getDirection();
            Queue<Point> queue = snake.getQueue();

            // 存head 耗费 2
            builder.append(head.x())
                    .append(" ")
                    .append(head.y())
                    .append(" ");

            // 存颜色RGB 耗费 3
            builder.append(color.getRed())
                    .append(" ")
                    .append(color.getGreen())
                    .append(" ")
                    .append(color.getBlue())
                    .append(" ");

            // 存方向 耗费 1
            builder.append(direction)
                    .append(" ");

            // 存队列大小 耗费 1
            builder.append(queue.size())
                    .append(" ");

            // 存队列内容 耗费 2n
            for (var point : queue)
                builder.append(point.x())
                        .append(" ")
                        .append(point.y())
                        .append(" ");
        }

        // 存HashSet
        // 存HashSet大小 耗费 1
        builder.append(foodPoints.size())
                .append(" ");
        for (var point : foodPoints) {

            // 存HashSet内容 耗费 2n
            builder.append(point.x())
                    .append(" ")
                    .append(point.y())
                    .append(" ");
        }

        return builder.toString();
    }

    /* 通过报文转换为UDPSnake对象 */
    public static UDPSnake toObject(String s) {
        String[] information = s.split(" "); // 报文数组
        int idx = 0; // 读头
        HashMap<String, Snake> snakes = new HashMap<>();

        // 读取map
        int mapSize = Integer.parseInt(information[idx++]);
        for (int i = 0; i < mapSize; i++) {
            String name;
            Point head;
            Color color;
            Direction direction;
            Queue<Point> queue = new LinkedList<>();

            // 读取name
            name = information[idx++];

            // 读取head
            int headX = Integer.parseInt(information[idx++]);
            int headY = Integer.parseInt(information[idx++]);
            head = new Point(headX, headY);

            // 读取color
            int R = Integer.parseInt(information[idx++]);
            int G = Integer.parseInt(information[idx++]);
            int B = Integer.parseInt(information[idx++]);
            color = new Color(R, G, B);

            // 读取direction
            direction = Direction.valueOf(information[idx++]);

            // 读取queue
            int queueSize = Integer.parseInt(information[idx++]);
            for (int j = 0; j < queueSize; j++) {

                // 读取queue中元素
                int pointX = Integer.parseInt(information[idx++]);
                int pointY = Integer.parseInt(information[idx++]);
                queue.offer(new Point(pointX, pointY));
            }
            Snake snake = new Snake(head, color, direction, queue);
            snakes.put(name, snake);
        }

        // 读取set
        int setSize = Integer.parseInt(information[idx++]);
        HashSet<Point> foodPoints = new HashSet<>();
        for (int i = 0; i < setSize; i++) {
            int pointX = Integer.parseInt(information[idx++]);
            int pointY = Integer.parseInt(information[idx++]);
            foodPoints.add(new Point(pointX, pointY));
        }

        return new UDPSnake(snakes, foodPoints);
    }
}
