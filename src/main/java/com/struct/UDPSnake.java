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


    /* ����ת��ΪString������, ��������ռ�� */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // ��HashMap
        // ��Snakes���� �ķ�1
        builder.append(snakes.size())
                .append(" ");
        for (var entry : snakes.entrySet()) {

            // ������ �ķ� 1
            builder.append(entry.getKey())
                    .append(" ");

            Snake snake = entry.getValue();

            Point head = snake.getHead();
            Color color = snake.getColor();
            Direction direction = snake.getDirection();
            Queue<Point> queue = snake.getQueue();

            // ��head �ķ� 2
            builder.append(head.x())
                    .append(" ")
                    .append(head.y())
                    .append(" ");

            // ����ɫRGB �ķ� 3
            builder.append(color.getRed())
                    .append(" ")
                    .append(color.getGreen())
                    .append(" ")
                    .append(color.getBlue())
                    .append(" ");

            // �淽�� �ķ� 1
            builder.append(direction)
                    .append(" ");

            // ����д�С �ķ� 1
            builder.append(queue.size())
                    .append(" ");

            // ��������� �ķ� 2n
            for (var point : queue)
                builder.append(point.x())
                        .append(" ")
                        .append(point.y())
                        .append(" ");
        }

        // ��HashSet
        // ��HashSet��С �ķ� 1
        builder.append(foodPoints.size())
                .append(" ");
        for (var point : foodPoints) {

            // ��HashSet���� �ķ� 2n
            builder.append(point.x())
                    .append(" ")
                    .append(point.y())
                    .append(" ");
        }

        return builder.toString();
    }

    /* ͨ������ת��ΪUDPSnake���� */
    public static UDPSnake toObject(String s) {
        String[] information = s.split(" "); // ��������
        int idx = 0; // ��ͷ
        HashMap<String, Snake> snakes = new HashMap<>();

        // ��ȡmap
        int mapSize = Integer.parseInt(information[idx++]);
        for (int i = 0; i < mapSize; i++) {
            String name;
            Point head;
            Color color;
            Direction direction;
            Queue<Point> queue = new LinkedList<>();

            // ��ȡname
            name = information[idx++];

            // ��ȡhead
            int headX = Integer.parseInt(information[idx++]);
            int headY = Integer.parseInt(information[idx++]);
            head = new Point(headX, headY);

            // ��ȡcolor
            int R = Integer.parseInt(information[idx++]);
            int G = Integer.parseInt(information[idx++]);
            int B = Integer.parseInt(information[idx++]);
            color = new Color(R, G, B);

            // ��ȡdirection
            direction = Direction.valueOf(information[idx++]);

            // ��ȡqueue
            int queueSize = Integer.parseInt(information[idx++]);
            for (int j = 0; j < queueSize; j++) {

                // ��ȡqueue��Ԫ��
                int pointX = Integer.parseInt(information[idx++]);
                int pointY = Integer.parseInt(information[idx++]);
                queue.offer(new Point(pointX, pointY));
            }
            Snake snake = new Snake(head, color, direction, queue);
            snakes.put(name, snake);
        }

        // ��ȡset
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
