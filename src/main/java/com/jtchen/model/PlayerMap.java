package com.jtchen.model;

import com.jtchen.struct.Point;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;

/**
 * @author jtchen
 * @version 1.0
 * @date 2020/12/30 16:42
 */
public class PlayerMap extends Frame {
    public static final Color HEAD_COLOR = Color.red;

    public static final Color FOOD_COLOR = Color.yellow;

    public static final int BLOCK = 15; // 方格长宽

    private static final int LENGTH_ROW = 42; // 界面方格行

    private static final int LENGTH_COL = 43; // 界面方格列

    public static final int LENGTH = 40;

    private final Snake[] snakes;

    private static com.jtchen.struct.Point foodPoint;

    private static HashSet<com.jtchen.struct.Point> body = new HashSet<>();

    private Image offScreenImage = null;

    public PlayerMap(Snake[] snakes) {
        this.snakes = snakes;
        this.setTitle("Snake");
        this.setSize(LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);
        this.setLocation(30, 40);
        this.setBackground(Color.WHITE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

        });
        this.setResizable(false);
        this.setVisible(true);

        GenerateFood();
        addKeyListener(new KeyMonitor());
        repaint();
        new Thread(new DrawThread()).start();
    }

    /* draw every snakes */
    public void drawSnakes(Graphics g) {
        for (Snake snake : snakes)
            snake.draw(g);
    }

    public static HashSet<com.jtchen.struct.Point> getBody() {
        return body;
    }

    public void pushSnake() {
        for (Snake snake : snakes)
            snake.goAHead();
    }

    public static void GenerateFood() {
        int x = (int) (Math.random() * LENGTH);
        int y = (int) (Math.random() * LENGTH);

        foodPoint = new com.jtchen.struct.Point(x, y);
    }

    public void draw(Graphics g) {
        drawSnakes(g);
        drawFood(g);
    }

    public void drawFood(Graphics g) {
        Color c = g.getColor();
        g.setColor(FOOD_COLOR);

        //draw food
        g.fillRect(PlayerMap.toFillParameter(foodPoint).x()
                * PlayerMap.BLOCK, PlayerMap.toFillParameter(foodPoint).y()
                * PlayerMap.BLOCK, PlayerMap.BLOCK, PlayerMap.BLOCK);

        g.setColor(c);

    }

    public static boolean isFood(com.jtchen.struct.Point point) {
        return point.equals(foodPoint);
    }

    public void update(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = createImage(LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);
        }
        Graphics graphics = offScreenImage.getGraphics();

        // 先把内容画在虚拟画布上
        paint(graphics);

        //然后将虚拟画布上的内容一起画在画布上
        g.drawImage(offScreenImage, 0, 0, null);

        //draw snakes and food
        draw(g);
    }

    public static Point toFillParameter(Point point) {
        return new Point(point.x() + 1, point.y() + 2);
    }


    private class KeyMonitor extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                System.exit(0);
            } else {
                snakes[0].keyPressed(e);
                repaint();
            }
        }

    }
    public class DrawThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                pushSnake();
                repaint();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.err.println(e.toString());
                }
            }
        }
    }
    public static void main(String[] args) {
        Snake snake = new Snake(new Point[]{new Point(1, 0), new Point(1, 1),
                new Point(1, 2), new Point(1, 3), new Point(1, 4)}, Color.MAGENTA);
        new PlayerMap(new Snake[]{snake});
    }
}
