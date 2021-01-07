package com.struct;

import java.io.Serializable;
import java.util.List;

public class UDPSnake implements Serializable {
    private List<Snake> snakes;
    private Point food;

    public UDPSnake(List<Snake> snakes, Point food) {
        this.snakes = snakes;
        this.food = food;
    }

    public List<Snake> getSnakes() {
        return snakes;
    }

    public Point getFood() {
        return food;
    }
}
