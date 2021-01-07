package com.struct;

import com.struct.Point;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static com.jtchen.Thread.SendSnakes.LENGTH;

/**
 * @author jtchen
 * @version 1.0
 * @date 2020/12/30 15:38
 */
public class Snake implements Serializable {
    private final Queue<Point> queue; // 由点组成的队列

    private Point head; // 蛇的头的点

    public Queue<Point> getQueue() {
        return queue;
    }

    public Color getColor() {
        return color;
    }

    private final Color color; // 蛇的颜色

    private int direction; // 0上 1下 2左 3右

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Snake(Point[] points, Color color) {
        // 初始化蛇队列, 并且把点入队
        queue = new LinkedList<>();
        Point prev = null;
        for (int i = 0; i < points.length; i++) {
            queue.offer(points[i]);
            if (i == points.length - 1) head = points[i];
            if (i == points.length - 2) prev = points[i];
        }

        // 上色
        this.color = color;

        // 根据最后一个点、 倒数第二个点判定方向
        assert prev != null;
        assert head != null;
        this.direction = Point.getDirection(prev, head);

        head = points[points.length - 1];

    }

    /* 通过前面的点来判断蛇该做出什么行为 */
    public boolean move(Point point, Point foodPoint, Set<Point> body) {
        // 如果前面的点是body, 则退出游戏
        if (body.contains(point))
            return false;

        // 否则更新头部, 更新queue、set
        head = point;
        queue.offer(point);
        body.add(point);

        // 如果不是food则会出队
        if (!point.equals(foodPoint)) {
            Point point1 = queue.poll();
            body.remove(point1);
        }
        return true;
    }

    /* 让蛇在其方向上前进一格 */
    public boolean goAHead(Point foodPoint, Set<Point> body) {
        int x = head.x();
        int y = head.y();
        switch (direction) {
            // 0上 1下 2左 3右
            case 2:
                return move(new Point(x - 1 < 0 ? LENGTH - Math.abs(--x) : --x, y), foodPoint, body);
            case 0:
                return move(new Point(x, y - 1 < 0 ? LENGTH - Math.abs(--y) : --y), foodPoint, body);
            case 3:
                return move(new Point(++x % LENGTH, y), foodPoint, body);
            case 1:
                return move(new Point(x, ++y % LENGTH), foodPoint, body);
        }
        return true;
    }


    public Point getHead() {
        return head;
    }


}
