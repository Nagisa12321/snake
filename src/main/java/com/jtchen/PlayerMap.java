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
    public static final Color HEAD_COLOR = Color.red; // ��ͷ����ɫ

    public static final Color FOOD_COLOR = Color.yellow; // ʳ����ɫ

    public static final int BLOCK = 20; // ���񳤿�

    public static final int LENGTH = 30; // ��ʵ����

    private static final int LENGTH_ROW = LENGTH + 2; // ���淽����

    private static final int LENGTH_COL = LENGTH + 3; // ���淽����

    private final Snake[] snakes; // ��ͼ�е���

    private static Point foodPoint; // ʳ��ĵ�

    private static final HashSet<Point> body = new HashSet<>(); // ����ĵ�, �ж�lose��

    private Image offScreenImage = null;

    public PlayerMap(Snake[] snakes) {
        /* ��ʼ������ */
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

        // ����ʳ��
        GenerateFood();

        // ��Ӽ��̼���
        /*addKeyListener(new KeyMonitor());
*/
        // �Ȼ�����ǰ����
        repaint();

        // ��ʼpush-paint�߳�
        /*new Thread(new DrawThread()).start();*/
    }

    /* draw every snakes */
    public void drawSnakes(Graphics g) {
        for (Snake snake : snakes)
            snake.draw(g);
    }

    // ��ȡ����ĵ�
    public static HashSet<Point> getBody() {
        return body;
    }

/*    // ��ÿһ����ǰ��һ��
    public void pushSnake() {
        for (Snake snake : snakes)
            snake.goAHead();
    }*/

    // �ڵ�ͼ���һ������ʳ��
    public static void GenerateFood() {
        int x = (int) (Math.random() * LENGTH);
        int y = (int) (Math.random() * LENGTH);

        Point tmp = new Point(x, y);
        if (body.contains(tmp)) {
            GenerateFood();
        } else
            foodPoint = new Point(x, y);
    }

    // �����ߺ�ʳ��
    public void draw(Graphics g) {
        drawSnakes(g);
        drawFood(g);
    }

    // ����ʳ��
    public void drawFood(Graphics g) {
        Color c = g.getColor();
        g.setColor(FOOD_COLOR);

        //draw food
        g.fillRect(PlayerMap.toFillParameter(foodPoint).x()
                * PlayerMap.BLOCK, PlayerMap.toFillParameter(foodPoint).y()
                * PlayerMap.BLOCK, PlayerMap.BLOCK, PlayerMap.BLOCK);

        g.setColor(c);

    }

    // �ж�ĳ�����Ƿ�Ϊʳ��ĵ�
    public static boolean isFood(Point point) {
        return point.equals(foodPoint);
    }


    // ����/�ػ����
    public void update(Graphics g) {
        // �����⻭��Ϊ��, �½����⻭��
        if (offScreenImage == null)
            offScreenImage = createImage(LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);
        Graphics graphics = offScreenImage.getGraphics();

        // �Ȱ����ݻ������⻭����
        paint(graphics);

        //Ȼ�����⻭���ϵ�����һ���ڻ�����
        g.drawImage(offScreenImage, 0, 0, null);

        //draw snakes and food
        draw(g);
    }

    /**
     * ������Ҫ������ת��Ϊ�����ϵ�����
     * PS: �������Ͻ�Ϊ(0, 0), ��������{@code new Point(0, 0)}����
     */
    public static Point toFillParameter(Point point) {
        return new Point(point.x() + 1, point.y() + 2);
    }


   /* *//* ���̼����� *//*
    private class KeyMonitor extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            // �ո����˳�
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                System.exit(0);

                // �����򴫸��ߵļ�����
            } else {
                snakes[0].keyPressed(e);
                repaint();
            }
        }

    }

    *//* ���߳�ר�Ÿ���ÿ��n�����ƶ���һ�β����ػ� *//*
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
