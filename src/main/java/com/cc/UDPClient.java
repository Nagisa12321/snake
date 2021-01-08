package com.cc;

import com.jtchen.SendSnakes;
import com.struct.Direction;
import com.struct.Point;
import com.struct.Snake;
import com.struct.UDPSnake;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UDPClient extends Frame implements Runnable {
    public static final Color HEAD_COLOR = Color.red; // ��ͷ����ɫ

    public static final Color FOOD_COLOR = Color.yellow; // ʳ����ɫ

    public static final int BLOCK = 18; // ���񳤿�

    public static final int LENGTH = SendSnakes.LENGTH; // ��ʵ����

    private static final int LENGTH_ROW = LENGTH + 2; // ���淽����

    private static final int LENGTH_COL = LENGTH + 3; // ���淽����

    private final String playerName;

    private final InetAddress IP;

    private final BlockingQueue<Integer> keyboardQueue;

    private final BlockingQueue<UDPSnake> drawQueue;

    private final DefaultTableModel tableModel;

    private Image iBuffer = null;

    private Graphics gBuffer = null;

    private HashMap<String, Snake> snakes;

    private HashSet<Point> foodPoints;

    public UDPClient(String playerName, String host, DefaultTableModel tableModel) throws HeadlessException, UnknownHostException {
        this.playerName = playerName;
        IP = InetAddress.getByName(host);
        keyboardQueue = new LinkedBlockingQueue<>();
        drawQueue = new LinkedBlockingQueue<>();
        this.tableModel = tableModel;

        this.setTitle("Snake");
        this.setSize(LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);
        this.setLocation(30, 40);
        this.setBackground(Color.gray);
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

        //������,���߳�,�ֱ��ж�Ӧ����Ϣ������˽��̹�ͨ
        new Thread(new UDPClientSend(IP, 8090, playerName, socket, keyboardQueue)).start();
        new Thread(new UDPClientReceive(socket, drawQueue)).start();

        //�߳̿�ʼ��żӼ��̼���
        addKeyListener(new KeyMonitor(this));

        while (true) {
            try {
                //��Ϣ����ȡ��snakes��
                UDPSnake nowDraw = drawQueue.take();
                snakes = nowDraw.getSnakes();
                foodPoints = nowDraw.getFood();


                //������ǲ���si��
                if (!snakes.containsKey(playerName)) {
                    socket.close();
                    this.dispose();
                    JOptionPane.showMessageDialog(null, "��������, ������", "!!!!!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                //ÿ�ζ��յ���Ϣ�ػ�����
                repaint();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    // �����ߺ�ʳ��
    public void draw(Graphics g) {
        drawSnakes(g);
        drawFood(g);
    }

    /* draw every snakes */
    public void drawSnakes(Graphics g) {
        tableModel.setRowCount(0);
        for (var entry : snakes.entrySet()) {
            tableModel.addRow(new String[]{
                    entry.getKey(),
                    String.valueOf(entry.getValue().getQueue().size() - 4)});
            Snake snake = entry.getValue();
            System.err.println("���� " + snake.getDirection());

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

    // ����ʳ��
    public void drawFood(Graphics g) {
        Color c = g.getColor();
        g.setColor(FOOD_COLOR);

        //draw food

        for (var food : foodPoints)
            g.fillRect(toFillParameter(food).x()
                    * BLOCK, toFillParameter(food).y()
                    * BLOCK, BLOCK, BLOCK);

        g.setColor(c);

    }

    public void update(Graphics g) {
        // �����⻭��Ϊ��, �½����⻭��
        if (iBuffer == null) {
            iBuffer = createImage(LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);
            gBuffer = iBuffer.getGraphics();
        }

        //˫���弼��������ڴ滭��
        gBuffer.setColor(Color.gray);
        gBuffer.fillRect(0, 0, LENGTH_ROW * BLOCK, LENGTH_COL * BLOCK);

        //���߻�ʳ��(���ڴ滭����)
        draw(gBuffer);
        paint(gBuffer);

        //�ڴ滭��ֱ�ӻ�ǰ̨
        g.drawImage(iBuffer, 0, 0, null);

    }

    /**
     * ������Ҫ������ת��Ϊ�����ϵ�����
     * PS: �������Ͻ�Ϊ(0, 0), ��������{@code new Point(0, 0)}����
     */
    public Point toFillParameter(Point point) {
        return new Point(point.x() + 1, point.y() + 2);
    }

    private DatagramSocket establish(InetAddress serverIp, int serverPort, String name) {
        try {
            //׼���÷��͵İ����˿����
            DatagramSocket socket = new DatagramSocket(0);
            byte[] sendName = name.getBytes(StandardCharsets.UTF_8);
            DatagramPacket request = new DatagramPacket(sendName, sendName.length, serverIp, serverPort);
            DatagramPacket respond = new DatagramPacket(new byte[1], 1);

            //pokeһ�·�����,��ʱ���쳣
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
        private HashMap<Direction, List<Integer>> dMap = new HashMap<>() {{
            put(Direction.UP, new ArrayList<>() {{
                add(KeyEvent.VK_LEFT);
                add(KeyEvent.VK_DOWN);
                add(KeyEvent.VK_RIGHT);
            }});
            put(Direction.DOWN, new ArrayList<>() {{
                add(KeyEvent.VK_LEFT);
                add(KeyEvent.VK_UP);
                add(KeyEvent.VK_RIGHT);
            }});
            put(Direction.LEFT, new ArrayList<>() {{
                add(KeyEvent.VK_DOWN);
                add(KeyEvent.VK_RIGHT);
                add(KeyEvent.VK_UP);
            }});
            put(Direction.RIGHT, new ArrayList<>() {{
                add(KeyEvent.VK_DOWN);
                add(KeyEvent.VK_LEFT);
                add(KeyEvent.VK_UP);
            }});
        }};

        public KeyMonitor(UDPClient window) {
            this.window = window;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            try {
                // ESC�رմ���
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    /*window.dispose();*/
                    Snake snake = snakes.get(playerName);
                    Direction d = snake.getDirection();
                    var opList = dMap.get(d);
                    /*keyboardQueue.addAll(opList);*/
                    for (var op : opList) {
                        keyboardQueue.put(op);
                        Thread.sleep(5);
                    }
                    //�������������,������Ϣ���и�UDPClientSend
                } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN ||
                        e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    keyboardQueue.put(e.getKeyCode());
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    Snake snake = snakes.get(playerName);
                    Direction d = snake.getDirection();
                    switch (d) {
                        case UP:
                            for (int i = 0; i < 5; i++) {
                                keyboardQueue.put(KeyEvent.VK_UP);
                                Thread.sleep(2);
                            }
                            break;
                        case DOWN:
                            for (int i = 0; i < 5; i++) {
                                keyboardQueue.put(KeyEvent.VK_DOWN);
                                Thread.sleep(2);
                            }
                            break;
                        case LEFT:
                            for (int i = 0; i < 5; i++) {
                                keyboardQueue.put(KeyEvent.VK_LEFT);
                                Thread.sleep(2);
                            }
                            break;
                        case RIGHT:
                            for (int i = 0; i < 5; i++) {
                                keyboardQueue.put(KeyEvent.VK_RIGHT);
                                Thread.sleep(2);
                            }
                            break;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_Z) {
                    Snake snake = snakes.get(playerName);
                    Direction d = snake.getDirection();
                    switch (d) {
                        case UP:
                            keyboardQueue.put(KeyEvent.VK_LEFT);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_UP);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_RIGHT);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_UP);
                            Thread.sleep(5);
                            break;
                        case DOWN:
                            keyboardQueue.put(KeyEvent.VK_LEFT);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_DOWN);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_RIGHT);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_DOWN);
                            Thread.sleep(5);
                            break;
                        case LEFT:
                            keyboardQueue.put(KeyEvent.VK_UP);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_LEFT);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_DOWN);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_LEFT);
                            Thread.sleep(5);
                            break;
                        case RIGHT:
                            keyboardQueue.put(KeyEvent.VK_UP);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_RIGHT);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_DOWN);
                            Thread.sleep(5);
                            keyboardQueue.put(KeyEvent.VK_RIGHT);
                            Thread.sleep(5);
                            break;
                    }
                }
            } catch (InterruptedException interruptedException) {
                System.err.println(interruptedException.getMessage());
            }
        }
    }
}
