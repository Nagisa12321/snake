package com.struct;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author jtchen
 * @version 1.0
 * @date 2020/12/30 16:15
 */
public class Point implements Serializable {
    private final int x;
    private final int y;

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

    /* pointB 在 pointA 的什么方向 */
    // 0 上 1 下 2 左 3 右
    public static int getDirection(Point a, Point b) {
        if (b.x > a.x) return 3;
        if (b.x < a.x) return 2;
        if (b.y > a.y) return 1;
        if (b.y < a.y) return 0;
        else return -1;
    }
}
