package com.jtchen;

import com.struct.Point;
import com.struct.Snake;

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
    public static final Color HEAD_COLOR = Color.red; // 蛇头部颜色

    public static final Color FOOD_COLOR = Color.yellow; // 食物颜色

    public static final int BLOCK = 20; // 方格长宽

    public static final int LENGTH = 30; // 真实长宽

    private static final int LENGTH_ROW = LENGTH + 2; // 界面方格行

    private static final int LENGTH_COL = LENGTH + 3; // 界面方格列

    private final Snake[] snakes; // 地图中的蛇

    private static Point foodPoint; // 食物的点

    private static final HashSet<Point> body = new HashSet<>(); // 身体的点, 判定lose用

    private Image offScreenImage = null;

    public PlayerMap(Snake[] snakes) {
        /* 初始化窗体 */
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

        // 生成食物
        GenerateFood();

        // 添加键盘监听
        /*addKeyListener(new KeyMonitor());
*/
        // 先画出当前画面
        repaint();

        // 开始push-paint线程
        /*new Thread(new DrawThread()).start();*/
    }

    /* draw every snakes */
    public void drawSnakes(Graphics g) {
        for (Snake snake : snakes)
            snake.draw(g);
    }

    // 获取身体的点
    public static HashSet<Point> getBody() {
        return body;
    }

/*    // 让每一条蛇前进一格
    public void pushSnake() {
        for (Snake snake : snakes)
            snake.goAHead();
    }*/

    // 在地图随机一点生成食物
    public static void GenerateFood() {
        int x = (int) (Math.random() * LENGTH);
        int y = (int) (Math.random() * LENGTH);

        Point tmp = new Point(x, y);
        if (body.contains(tmp)) {
            GenerateFood();
        } else
            foodPoint = new Point(x, y);
    }

    // 画出蛇和食物
    public void draw(Graphics g) {
        drawSnakes(g);
        drawFood(g);
    }

    // 画出食物
    public void drawFood(Graphics g) {
        Color c = g.getColor();
        g.setColor(FOOD_COLOR);

        //draw food
        g.fillRect(PlayerMap.toFillParameter(foodPoint).x()
                * PlayerMap.BLOCK, PlayerMap.toFillParameter(foodPoint).y()
                * PlayerMap.BLOCK, PlayerMap.BLOCK, PlayerMap.BLOCK);

        g.setColor(c);

    }

    // 判定某个点是否为食物的点
    public static boolean isFood(Point point) {
        return point.equals(foodPoint);
    }


    // 更新/重画面板
    public void update(Graphics g) {
        // 若虚拟画布为空, 新建虚拟画布
        if (offScreenImage == null)
            offScreenImage = createImage(LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);
        Graphics graphics = offScreenImage.getGraphics();

        // 先把内容画在虚拟画布上
        paint(graphics);

        //然后将虚拟画布上的内容一起画在画布上
        g.drawImage(offScreenImage, 0, 0, null);

        //draw snakes and food
        draw(g);
    }

    /**
     * 把你想要的坐标转换为画布上的坐标
     * PS: 画布左上角为(0, 0), 且你输入{@code new Point(0, 0)}即可
     */
    public static Point toFillParameter(Point point) {
        return new Point(point.x() + 1, point.y() + 2);
    }


   /* *//* 键盘监听器 *//*
    private class KeyMonitor extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            // 空格则退出
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                System.exit(0);

                // 其他则传给蛇的监听器
            } else {
                snakes[0].keyPressed(e);
                repaint();
            }
        }

    }

    *//* 新线程专门负责每隔n毫秒推动蛇一次并且重画 *//*
    public class DrawThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                pushSnake();
                repaint();
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    System.err.println(e.toString());
                }
            }
        }
    }*/

    public static void main(String[] args) {
        Snake snake = new Snake(new Point[]{new Point(1, 0), new Point(1, 1),
                new Point(1, 2), new Point(1, 3), new Point(1, 4)}, Color.pink);
        new PlayerMap(new Snake[]{snake});
    }
}
