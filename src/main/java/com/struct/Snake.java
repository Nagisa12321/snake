package com.struct;

import java.awt.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


/**
 * @author jtchen
 * @version 1.0
 * @date 2020/12/30 15:38
 */
public class Snake implements Serializable, Cloneable {
    private Queue<Point> queue; // 由点组成的队列

    private Point head; // 蛇的头的点

    public Queue<Point> getQueue() {
        return queue;
    }

    public Color getColor() {
        return color;
    }

    private Color color; // 蛇的颜色

    private Direction direction; // 0上 1下 2左 3右

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
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


    // 专用于客户端画图用
    public Snake(Point head, Color color, Direction direction, Queue<Point> queue) {
        this.head = head;
        this.color = color;
        this.direction = direction;
        this.queue = queue;
    }

    /* 通过前面的点来判断蛇该做出什么行为 */
    public boolean move(Point point, Set<Point> foodPoints, Set<Point> body) {
        // 如果前面的点是body, 则退出游戏
        if (body.contains(point))
            return false;

        // 否则更新头部, 更新queue、set
        head = point;
        queue.offer(point);
        body.add(point);

        // 如果不是food则会出队
        if (!foodPoints.contains(point)) {
            Point point1 = queue.poll();
            body.remove(point1);
        }
        return true;
    }

    public Point getHead() {
        return head;
    }

    public Snake clone() throws CloneNotSupportedException {
        Snake clone = (Snake) super.clone();
        clone.head = head.clone();
        clone.queue = new LinkedList<>(queue);
        clone.color = color;
        clone.direction = direction;
        return clone;
    }
}
