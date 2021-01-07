package com.jtchen.Thread;

import com.jtchen.ClientInfo;
import com.jtchen.PlayerMap;
import com.struct.Point;
import com.struct.Snake;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class SendSnakes implements Runnable {
    private Vector<ClientInfo> clientInfos; // 用户IP PORT列表
    private Vector<String> operation; // 操作队列
    private HashMap<String, Snake> snakes; // 蛇

    private Point foodPoint; // 食物的点
    private HashSet<Point> body; // 身体点集

    public SendSnakes(Vector<ClientInfo> clientInfos, Vector<String> operation, HashMap<String, Snake> snakes) {
        this.clientInfos = clientInfos;
        this.operation = operation;
        this.snakes = snakes;

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

                Snake snake = snakes.get(name);
            }
            // ...
        }
    }

    /* 解析字符串 获得name和op */
    public NameAndOperation getNameAndOperation() {
        // 出队一个name 和 op 的 String 并且解析一番
        String s = operation.get(0);
        operation.remove(0);

        String[] tmp = s.split(" ");
        String name = tmp[0];
        String op = tmp[1];
        return new NameAndOperation(name, op);
    }

    /* 根据出队的操作移动某条蛇, 并且改变蛇的方向 */
    public void moveSnake(String operation, Snake snake) {
        int direction = snake.getDirection();

        int x = snake.getHead().x();
        int y = snake.getHead().y();

        // 判断是否方向改变 并且重新改变方向
        switch (operation) {
            // 0上 1下 2左 3右
            case "left":
                if (direction != 3) {
                    Point movePoint = new Point(x - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--x) : --x, y);
                    snake.move(movePoint);
                    snake.setDirection(2);
                }
                break;
            case "up":
                if (direction != 1) {
                    Point movePoint = new Point(x, y - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--y) : --y);
                    snake.move(movePoint);
                    snake.setDirection(0);
                }
                break;
            case "right":
                if (direction != 2) {
                    Point movePoint = new Point(++x % PlayerMap.LENGTH, y);
                    snake.move(movePoint);
                    snake.setDirection(3);
                }
                break;
            case "down":
                if (direction != 0) {
                    Point movePoint = new Point(x, ++y % PlayerMap.LENGTH);
                    snake.move(movePoint);
                    snake.setDirection(1);
                }
                break;
        }
    }

    // 在地图随机一点生成食物
    public  void GenerateFood() {
        int x = (int) (Math.random() * LENGTH);
        int y = (int) (Math.random() * LENGTH);

        Point tmp = new Point(x, y);
        if (body.contains(tmp)) {
            GenerateFood();
        } else
            foodPoint = new Point(x, y);
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
