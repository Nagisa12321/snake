package com.struct;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class UDPSnake implements Serializable {
    private HashMap<String, Snake> snakes;
    private Point food;

    public UDPSnake(HashMap<String, Snake> snakes, Point food) {
        this.snakes = snakes;
        this.food = food;
    }


    public HashMap<String, Snake> getSnakes() {
        return snakes;
    }

    public Point getFood() {
        return food;
    }
}
