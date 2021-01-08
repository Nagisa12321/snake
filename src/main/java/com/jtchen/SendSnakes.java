package com.jtchen;

import com.struct.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.BlockingQueue;

@SuppressWarnings("InfiniteLoopStatement")
public class SendSnakes implements Runnable {
    public static final int LENGTH = 40; // ��ʵ����

    private final BlockingQueue<ClientInfo> clientInfos; // �û�IP PORT�б�

    private final BlockingQueue<String> operation; // ��������

    private final HashMap<String, Snake> snakes; // ��

    private final HashSet<Point> foodPoints; // ʳ��ĵ�

    private final HashSet<Point> body; // ����㼯

    public SendSnakes(BlockingQueue<ClientInfo> clientInfos,
                      BlockingQueue<String> operation,
                      HashMap<String, Snake> snakes,
                      HashSet<Point> body) {
        this.clientInfos = clientInfos;
        this.operation = operation;
        this.snakes = snakes;
        this.body = body;
        foodPoints = new HashSet<>();

        // ��ͼ�������ʳ��
        Point foodPoint1 = new Point(10, 10);
        Point foodPoint2 = new Point(20, 20);
        foodPoints.add(foodPoint1);
        foodPoints.add(foodPoint2);
        new Thread(new PushSnakeThread(snakes, foodPoints, body, clientInfos)).start();
    }


    public void run() {
        while (true) {
            try {
                // ����һ��Snake��name�Ͷ���
                NameAndOperation nAo = getNameAndOperation();
                String name = nAo.name;
                String operation = nAo.Op;

                System.out.println("��� " + name + "�����˶���: " + operation);
                System.out.println("name.size()" + name.length());
                System.out.println("hashmap size: " + snakes.size());
                // ��name�õ���
                Snake snake = snakes.get(name);

                System.out.println("snake : " + snake);

                // ��operation �� ����snake������
                // ���snake ײ��body, ���Ƴ�snake
                if (!moveSnake(operation, snake)) {

                    int idx = 0;
                    // ɾ�������ϵĵ�
                    for (var point : snake.getQueue()) {
                        if (idx++ % 2 == 0)
                            foodPoints.add(point);
                        body.remove(point);
                    }

                    snakes.remove(name);
                    System.out.println("��� " + name + "������, �Ѿ��ӱ����Ƴ�");
                }

                // ��������б� ����UDPSnakes�������
                SendUDPSnakes();

            } catch (InterruptedException | IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /* �����ַ��� ���name��op */
    public NameAndOperation getNameAndOperation() throws InterruptedException {
        // ����һ��name �� op �� String ���ҽ���һ��
        String s = operation.take();

        String[] tmp = s.split(" ");
        String name = tmp[0];
        String op = tmp[1];
        return new NameAndOperation(name, op);
    }

    /* ���ݳ��ӵĲ����ƶ�ĳ����, ���Ҹı��ߵķ��� */
    public boolean moveSnake(String operation, Snake snake) {
        Direction direction = snake.getDirection();

        int x = snake.getHead().x();
        int y = snake.getHead().y();

        // �ж��Ƿ���ı� �������¸ı䷽��
        switch (operation) {
            // 0�� 1�� 2�� 3��
            case "left":
                if (direction != Direction.RIGHT) {
                    Point movePoint = new Point(x - 1 < 0 ? LENGTH - Math.abs(--x) : --x, y);
                    snake.setDirection(Direction.LEFT);
                    System.err.println("snake.setDirection(2);" + snake.getDirection());

                    boolean res = snake.move(movePoint, foodPoints, body);
                    // �����Ҫ��������ʳ��, ��������ʳ��
                    if (foodPoints.contains(movePoint)) {
                        GenerateFood();

                        // ����ɾ����ʳ��ĵ�
                        foodPoints.remove(movePoint);
                    }

                    // ����ƶ���ǰ����body���ƶ�ʧ��
                    // ����false
                    return res;
                }
                break;
            case "up":
                if (direction != Direction.DOWN) {
                    Point movePoint = new Point(x, y - 1 < 0 ? LENGTH - Math.abs(--y) : --y);
                    snake.setDirection(Direction.UP);
                    /*Point tmp = new Point(foodPoint.x(), foodPoint.y());*/
                    boolean res = snake.move(movePoint, foodPoints, body);
                    if (foodPoints.contains(movePoint)) {
                        GenerateFood();
                        foodPoints.remove(movePoint);
                    }
                    return res;
                }
                break;
            case "right":
                if (direction != Direction.LEFT) {
                    Point movePoint = new Point(++x % LENGTH, y);
                    snake.setDirection(Direction.RIGHT);
                    boolean res = snake.move(movePoint, foodPoints, body);
                    if (foodPoints.contains(movePoint)) {
                        GenerateFood();
                        foodPoints.remove(movePoint);
                    }
                    return res;
                }
                break;
            case "down":
                if (direction != Direction.UP) {
                    Point movePoint = new Point(x, ++y % LENGTH);
                    snake.setDirection(Direction.DOWN);
                    boolean res = snake.move(movePoint, foodPoints, body);
                    if (foodPoints.contains(movePoint)) {
                        GenerateFood();
                        foodPoints.remove(movePoint);
                    }
                    return res;
                }
                break;
        }
        // ���ʲô��û�����򷵻�true
        // ����˵�ߵķ�������ǰ, �㰴�˺�, ��ʲôҲû����
        // ���仰˵ �߲����Ļ�����true
        return true;
    }

    // �ڵ�ͼ���һ������ʳ��
    public void GenerateFood() {
        generateFood(body, foodPoints);
    }

    /* ��ÿ����ҷ���UDPSnakes */
    public void SendUDPSnakes() throws IOException {
        sendUDPSnake(snakes, foodPoints, clientInfos);
    }

    /* ���÷���sendUDPSnake */
    private static void sendUDPSnake(HashMap<String, Snake> snakes, HashSet<Point> foodPoints, BlockingQueue<ClientInfo> clientInfos) throws IOException {
        UDPSnake snake = new UDPSnake(snakes, foodPoints);

        for (var c : clientInfos) {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(new byte[4096], 4096, c.getIP(), c.getPORT());
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

            // תΪObject��
            ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream);
            objectStream.writeObject(snake);
            byte[] arr = byteArrayStream.toByteArray();
            packet.setData(arr);//���DatagramPacket
            socket.send(packet);//����
            objectStream.close();
            byteArrayStream.close();
        }
    }

    /* ���÷���generateFood */
    private static void generateFood(HashSet<Point> body, HashSet<Point> foodPoints) {
        int x = (int) (Math.random() * LENGTH);
        int y = (int) (Math.random() * LENGTH);

        Point tmp = new Point(x, y);

        // ʳ��ĵ��������ĵ����, �������һ�Σ�
        if (body.contains(tmp) || foodPoints.contains(tmp)) {
            generateFood(body, foodPoints);
        } else foodPoints.add(tmp);
    }

    public static class NameAndOperation {
        private final String name;
        private final String Op;


        public NameAndOperation(String name, String op) {
            this.name = name;
            this.Op = op;
        }
    }

    @SuppressWarnings("BusyWait")
    public static class PushSnakeThread implements Runnable {
        private final HashMap<String, Snake> snakes;
        private final HashSet<Point> foodPoints;
        private final HashSet<Point> body;
        private final BlockingQueue<ClientInfo> clientInfos;

        public PushSnakeThread(HashMap<String, Snake> snakes,
                               HashSet<Point> foodPoints,
                               HashSet<Point> body,
                               BlockingQueue<ClientInfo> clientInfos) {
            this.snakes = snakes;
            this.foodPoints = foodPoints;
            this.body = body;
            this.clientInfos = clientInfos;
        }


        @Override
        public void run() {
            while (true) {
                // push snakes

                List<String> diePlayer = new ArrayList<>(100);
                for (var entry : snakes.entrySet()) {
                    Snake snake = entry.getValue();

                    int x = snake.getHead().x();
                    int y = snake.getHead().y();
                    Direction direction = snake.getDirection();
                    boolean success = false;
                    Point movePoint;
                    switch (direction) {
                        // 0�� 1�� 2�� 3��
                        case LEFT:
                            movePoint = new Point(x - 1 < 0 ? LENGTH - Math.abs(--x) : --x, y);
                            success = snake.move(movePoint, foodPoints, body);
                            if (foodPoints.contains(movePoint)) {
                                GenerateFood();
                                foodPoints.remove(movePoint);
                            }
                            break;
                        case UP:
                            movePoint = new Point(x, y - 1 < 0 ? LENGTH - Math.abs(--y) : --y);
                            success = snake.move(movePoint, foodPoints, body);
                            if (foodPoints.contains(movePoint)) {
                                GenerateFood();
                                foodPoints.remove(movePoint);
                            }
                            break;
                        case RIGHT:
                            movePoint = new Point(++x % LENGTH, y);
                            success = snake.move(movePoint, foodPoints, body);
                            if (foodPoints.contains(movePoint)) {
                                GenerateFood();
                                foodPoints.remove(movePoint);
                            }
                            break;
                        case DOWN:
                            movePoint = new Point(x, ++y % LENGTH);
                            success = snake.move(movePoint, foodPoints, body);
                            if (foodPoints.contains(movePoint)) {
                                GenerateFood();
                                foodPoints.remove(movePoint);
                            }
                            break;
                    }
                    if (!success) {
                        int idx = 0;
                        // ɾ�������ϵĵ�
                        for (var point : snake.getQueue()) {
                            if (idx++ % 2 == 0)
                                foodPoints.add(point);
                            body.remove(point);

                            body.remove(point);
                            diePlayer.add(entry.getKey());
                        }
                    }
                }
                for (var name : diePlayer) {
                    snakes.remove(name);
                }
                try {
                    SendUDPSnakes();
                    Thread.sleep(200);
                } catch (InterruptedException | IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        /* ��ÿ����ҷ���UDPSnakes */
        public void SendUDPSnakes() throws IOException {
            sendUDPSnake(snakes, foodPoints, clientInfos);
        }

        // �ڵ�ͼ���һ������ʳ��
        public void GenerateFood() {
            generateFood(body, foodPoints);
        }
    }
}
