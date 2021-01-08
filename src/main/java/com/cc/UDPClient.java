package com.cc;

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class UDPClient extends Frame implements Runnable {
    public static final Color HEAD_COLOR = Color.red; // 蛇头部颜色

    public static final Color FOOD_COLOR = Color.yellow; // 食物颜色

    public static final int BLOCK = 20; // 方格长宽

    public static final int LENGTH = 30; // 真实长宽

    private static final int LENGTH_ROW = LENGTH + 2; // 界面方格行

    private static final int LENGTH_COL = LENGTH + 3; // 界面方格列

    private final String playerName;

    InetAddress IP;

    private final BlockingQueue<Integer> keyboardQueue;

    private final BlockingQueue<UDPSnake> drawQueue;

    private Image iBuffer = null;

    private Graphics gBuffer = null;

    private HashMap<String, Snake> snakes;

    private Point food;

    public UDPClient(String playerName, String host) throws HeadlessException, UnknownHostException {
        this.playerName = playerName;
        IP = InetAddress.getByName(host);
        keyboardQueue = new LinkedBlockingQueue<>();
        drawQueue = new LinkedBlockingQueue<>();

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
        if (socket == null) {
            this.dispose();
            return;
        }

        //开启收,发线程,分别有对应的消息队列与此进程沟通
        new Thread(new UDPClientSend(IP, 8090, playerName, socket, keyboardQueue)).start();
        new Thread(new UDPClientReceive(socket, drawQueue)).start();

        //线程开始后才加键盘监听
        addKeyListener(new KeyMonitor(this));

        while (true) {
            try {
                //消息队列取出snakes表
                UDPSnake nowDraw = drawQueue.take();
                snakes = nowDraw.getSnakes();
                food = nowDraw.getFood();


                //检查蛇是不是si了
                if (!snakes.containsKey(playerName)) {
                    socket.close();
                    this.dispose();
                    return;
                }

                //每次都收到消息重画画板
                repaint();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
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
            g.setColor(HEAD_COLOR);
            g.fillRect(toFillParameter(snake.getHead()).x()
                    * BLOCK, toFillParameter(snake.getHead()).y()
                    * BLOCK, BLOCK, BLOCK);

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
        if (iBuffer == null) {
            iBuffer = createImage(LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);
            gBuffer = iBuffer.getGraphics();
        }

        //双缓冲技术，填充内存画板
        gBuffer.setColor(Color.WHITE);
        gBuffer.fillRect(0, 0, LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);

        //画蛇画食物(在内存画板上)
        draw(gBuffer);
        paint(gBuffer);

        //内存画板直接画前台
        g.drawImage(iBuffer, 0, 0, null);

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
            //准备好发送的包，端口随机
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
        private UDPClient window;
        public KeyMonitor(UDPClient window){
            this.window = window;
        }
        @Override
        public void keyPressed(KeyEvent e) {
            // ESC关闭窗口
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                window.dispose();

                //如果是上下左右,加入消息队列给UDPClientSend
            } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN ||
                    e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                try {
                    keyboardQueue.put(e.getKeyCode());
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
    }
}
