package com.struct;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class UDPSnake implements Serializable {
    private HashMap<String, Snake> snakes;
    private HashSet<Point> foodPoints;

    public UDPSnake(HashMap<String, Snake> snakes, HashSet<Point> foodPoints) {
        this.snakes = snakes;
        this.foodPoints = foodPoints;
    }


    public HashMap<String, Snake> getSnakes() {
        return snakes;
    }

    public HashSet<Point>  getFood() {
        return foodPoints;
    }
}
