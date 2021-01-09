package com.struct;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author jtchen
 * @version 1.0
 * @date 2020/12/30 16:15
 */
public class Point implements Serializable, Cloneable {

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public Point clone() throws CloneNotSupportedException {
        Point clone = (Point) super.clone();
        clone.x = x;
        clone.y = y;
        return clone;
    }

    /* pointB 在 pointA 的什么方向 */
    // 0 上 1 下 2 左 3 右
    public static Direction getDirection(Point a, Point b) {
        if (b.x > a.x) return Direction.RIGHT;
        if (b.x < a.x) return Direction.LEFT;
        if (b.y > a.y) return Direction.DOWN;
        if (b.y < a.y) return Direction.UP;
        else return null;
    }
}
