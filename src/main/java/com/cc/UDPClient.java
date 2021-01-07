package com.cc;

import com.jtchen.PlayerMap;
import com.struct.Point;
import com.struct.Snake;
import com.struct.UDPSnake;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Vector;

public class UDPClient extends Frame implements Runnable {
    public static final Color HEAD_COLOR = Color.red; // 蛇头部颜色

    public static final Color FOOD_COLOR = Color.yellow; // 食物颜色

    public static final int BLOCK = 20; // 方格长宽

    public static final int LENGTH = 30; // 真实长宽

    private static final int LENGTH_ROW = LENGTH + 2; // 界面方格行

    private static final int LENGTH_COL = LENGTH + 3; // 界面方格列

    private final String playerName;

    InetAddress IP;

    private final Vector<Integer> keyboardQueue;

    private final Vector<UDPSnake> drawQueue;

    private Image offScreenImage = null;

    private HashMap<String, Snake> snakes;

    private Point food;

    public UDPClient(String playerName, String host) throws HeadlessException, UnknownHostException {
        this.playerName = playerName;
        IP = InetAddress.getByName(host);
        keyboardQueue = new Vector<>();
        drawQueue = new Vector<>();

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
    }

    public void run() {
        DatagramSocket socket = establish(IP, 8088, playerName);
        if(socket==null) return;
        new Thread(new UDPClientSend(IP, 8090, playerName, socket, keyboardQueue)).start();
        new Thread(new UDPClientReceive(socket, drawQueue)).start();

        //线程开始后才加键盘监听
        addKeyListener(new KeyMonitor());

        while (true) {
            if (drawQueue.isEmpty()) continue;
            UDPSnake nowDraw = drawQueue.remove(0);
            snakes = nowDraw.getSnakes();
            food = nowDraw.getFood();


            //..
            if (!snakes.containsKey(playerName)){
                socket.close();
                return;
            }


            repaint();
            System.err.println("repaint");
        }
    }

    // 画出蛇和食物
    public void draw(Graphics g) {
        drawSnakes(g);
        drawFood(g);
    }

    /* draw every snakes */
    public void drawSnakes(Graphics g) {
        for (var entry : snakes.entrySet()) {
            Snake snake = entry.getValue();
            System.err.println("方向： " + snake.getDirection());

            Color c = g.getColor();
            g.setColor(snake.getColor());

            //draw point
            for (Point point : snake.getQueue()) {
                g.fillRect(toFillParameter(point).x()
                        * BLOCK, toFillParameter(point).y()
                        * BLOCK, BLOCK, BLOCK);
            }

            // draw head
            g.setColor(PlayerMap.HEAD_COLOR);
            g.fillRect(PlayerMap.toFillParameter(snake.getHead()).x()
                    * PlayerMap.BLOCK, PlayerMap.toFillParameter(snake.getHead()).y()
                    * PlayerMap.BLOCK, PlayerMap.BLOCK, PlayerMap.BLOCK);

            g.setColor(c);
        }
    }

    // 画出食物
    public void drawFood(Graphics g) {
        Color c = g.getColor();
        g.setColor(FOOD_COLOR);

        //draw food
        g.fillRect(toFillParameter(food).x()
                * BLOCK, toFillParameter(food).y()
                * BLOCK, BLOCK, BLOCK);

        g.setColor(c);

    }

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
    public Point toFillParameter(Point point) {
        return new Point(point.x() + 1, point.y() + 2);
    }

    private DatagramSocket establish(InetAddress serverIp, int serverPort, String name) {
        try {
            DatagramSocket socket = new DatagramSocket(0);
            byte[] sendName = name.getBytes(StandardCharsets.UTF_8);
            DatagramPacket request = new DatagramPacket(sendName, sendName.length, serverIp, serverPort);
            DatagramPacket respond = new DatagramPacket(new byte[1], 1);

            //poke一下服务器,超时抛异常
            //socket.setSoTimeout(5000);
            socket.send(request);
            socket.receive(respond);

            return socket;
        } catch (IOException e) {
            System.out.println(e.getMessage() + "(establish)");
            return null;
        }
    }


    private class KeyMonitor extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            // 空格则退出
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                System.exit(0);

                //加入队列
            } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN ||
                    e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                keyboardQueue.add(e.getKeyCode());
            }
        }
    }
}
