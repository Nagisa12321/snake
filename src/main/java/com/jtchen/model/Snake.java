package com.jtchen.model;

import com.jtchen.struct.MyQueue;
import com.jtchen.struct.Point;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author jtchen
 * @version 1.0
 * @date 2020/12/30 15:38
 */
public class Snake {
    private final MyQueue<Point> queue; // 由点组成的队列

    private Point head; // 蛇的头的点

    private final Color color; // 蛇的颜色

    private int direction; // 0上 1下 2左 3右

    public Snake(Point[] points, Color color) {
        // 初始化蛇队列, 并且把点入队
        queue = new MyQueue<>();
        Point prev = null;
        for (int i = 0; i < points.length; i++) {
            queue.enqueue(points[i]);
            if (i == points.length - 1) head = points[i];
            if (i == points.length - 2) prev = points[i];

            // 添加入集中
            PlayerMap.getBody().add(points[i]);
        }

        // 上色
        this.color = color;

        // 根据最后一个点、 倒数第二个点判定方向
        assert prev != null;
        this.direction = Point.getDirection(prev, head);

        head = points[points.length - 1];

    }

    /* 通过前面的点来判断蛇该做出什么行为 */
    public void move(Point point) {
        // 如果前面的点是body, 则退出游戏
        if (PlayerMap.getBody().contains(point))
            System.exit(0);

        // 否则更新头部, 更新queue、set
        head = point;
        queue.enqueue(point);
        PlayerMap.getBody().add(point);

        // 如果不是food则会出队
        if (!PlayerMap.isFood(point)) {
            Point point1 = queue.dequeue();
            PlayerMap.getBody().remove(point1);

            // 如果是food则再随机生成一个
        } else PlayerMap.GenerateFood();
    }

    /* 让蛇在其方向上前进一格 */
    public void goAHead() {
        int x = head.x();
        int y = head.y();
        switch (direction) {
            // 0上 1下 2左 3右
            case 2:
                move(new Point(x - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--x) : --x, y));
                break;
            case 0:
                move(new Point(x, y - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--y) : --y));
                break;
            case 3:
                move(new Point(++x % PlayerMap.LENGTH, y));
                break;
            case 1:
                move(new Point(x, ++y % PlayerMap.LENGTH));
                break;
        }
    }

    /* 画出蛇 */
    public void draw(Graphics g) {
        Color c = g.getColor();
        g.setColor(color);

        //draw point
        for (Point point : queue) {
            g.fillRect(PlayerMap.toFillParameter(point).x()
                    * PlayerMap.BLOCK, PlayerMap.toFillParameter(point).y()
                    * PlayerMap.BLOCK, PlayerMap.BLOCK, PlayerMap.BLOCK);
        }

        // draw head
        g.setColor(PlayerMap.HEAD_COLOR);
        g.fillRect(PlayerMap.toFillParameter(head).x()
                * PlayerMap.BLOCK, PlayerMap.toFillParameter(head).y()
                * PlayerMap.BLOCK, PlayerMap.BLOCK, PlayerMap.BLOCK);

        g.setColor(c);
    }

    public Point getHead() {
        return head;
    }

    /* 键盘监听: 上下左右 */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int x = head.x();
        int y = head.y();
        switch (key) {
            // 0上 1下 2左 3右
            case KeyEvent.VK_LEFT:
                if (direction != 3) {
                    move(new Point(x - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--x) : --x, y));
                    direction = 2;
                }
                break;
            case KeyEvent.VK_UP:
                if (direction != 1) {
                    move(new Point(x, y - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--y) : --y));
                    direction = 0;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (direction != 2) {
                    move(new Point(++x % PlayerMap.LENGTH, y));
                    direction = 3;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (direction != 0) {
                    move(new Point(x, ++y % PlayerMap.LENGTH));
                    direction = 1;
                }
                break;
        }
    }
}
