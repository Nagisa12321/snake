package com.jtchen;

import com.struct.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

@SuppressWarnings("InfiniteLoopStatement")
public class UDPServerSend implements Runnable {

    public static final int LENGTH = 40; // ��ʵ����

    private final Semaphore mutex; // �û��б�����

    private final Vector<ClientInfo> clientInfos; // �û�IP PORT�б�

    private final BlockingQueue<String> operation; // ��������

    private final HashMap<String, Snake> snakes; // ��

    private final HashSet<Point> foodPoints; // ʳ��ĵ�

    private final HashSet<Point> body; // ����㼯

    private final SleepTime sleepTime;

    public UDPServerSend(Vector<ClientInfo> clientInfos,
                         BlockingQueue<String> operation,
                         HashMap<String, Snake> snakes,
                         HashSet<Point> body) {
        this.clientInfos = clientInfos;
        this.operation = operation;
        this.snakes = snakes;
        this.body = body;
        this.sleepTime = new SleepTime(201);
        this.foodPoints = new HashSet<>();
        this.mutex = new Semaphore(2);

        // ��ͼ�������ʳ��
        Point foodPoint1 = new Point(10, 10);
        Point foodPoint2 = new Point(20, 20);
        foodPoints.add(foodPoint1);
        foodPoints.add(foodPoint2);
        new Thread(new PushSnakeThread(snakes, foodPoints, body, clientInfos, mutex, sleepTime)).start();
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

                if (snake == null) continue;

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
                mutex.release();

                // ��������б� ����UDPSnakes�������
                SendUDPSnakes();

            } catch (InterruptedException | IOException | CloneNotSupportedException e) {
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
    @SuppressWarnings("DuplicatedCode")
    public boolean moveSnake(String operation, Snake snake) throws InterruptedException {
        Direction direction = snake.getDirection();

        int x = snake.getHead().x();
        int y = snake.getHead().y();

        // �ж��Ƿ���ı� �������¸ı䷽��
        mutex.acquire();
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
            case "m":
                Direction d = snake.getDirection();
                Point movePoint;
                switch (d) {
                    case LEFT:
                        movePoint = new Point(x - 1 < 0 ? LENGTH - Math.abs(--x) : --x, y);
                        if (body.contains(movePoint) || foodPoints.contains(movePoint)) break;

                        foodPoints.add(movePoint);
                        snake.move(movePoint, foodPoints, body);
                        foodPoints.remove(movePoint);
                        break;
                    case UP:
                        movePoint = new Point(x, y - 1 < 0 ? LENGTH - Math.abs(--y) : --y);
                        if (body.contains(movePoint) || foodPoints.contains(movePoint)) break;

                        foodPoints.add(movePoint);
                        snake.move(movePoint, foodPoints, body);
                        foodPoints.remove(movePoint);
                        break;
                    case RIGHT:
                        movePoint = new Point(++x % LENGTH, y);
                        if (body.contains(movePoint) || foodPoints.contains(movePoint)) break;

                        foodPoints.add(movePoint);
                        snake.move(movePoint, foodPoints, body);
                        foodPoints.remove(movePoint);
                        break;
                    case DOWN:
                        movePoint = new Point(x, ++y % LENGTH);
                        if (body.contains(movePoint) || foodPoints.contains(movePoint)) break;
                        foodPoints.add(movePoint);
                        snake.move(movePoint, foodPoints, body);
                        foodPoints.remove(movePoint);
                        break;
                }
                break;
            case "j":
                sleepTime.setTime(sleepTime.getTime() - 50);
                break;
            case "k":
                sleepTime.setTime(sleepTime.getTime() + 50);
                break;
            case "p":
                foodPoints.clear();
                break;
            case "h":
                for (var entry : snakes.entrySet()) {
                    Snake tmpS = entry.getValue();
                    if (!tmpS.equals(snake) && tmpS.getQueue().size() > 0) {
                        Point point1 = tmpS.getQueue().poll();
                        body.remove(point1);
                    }
                }
                break;
            case "e":
                GenerateFood();
                break;
            case "a":
                if (direction != Direction.RIGHT) {
                    x -= 10;
                    Point movePoint1 = new Point(x < 0 ? LENGTH + x : x, y);
                    snake.setDirection(Direction.LEFT);
                    System.err.println("snake.setDirection(2);" + snake.getDirection());

                    boolean res = snake.move(movePoint1, foodPoints, body);
                    // �����Ҫ��������ʳ��, ��������ʳ��
                    if (foodPoints.contains(movePoint1)) {
                        GenerateFood();

                        // ����ɾ����ʳ��ĵ�
                        foodPoints.remove(movePoint1);
                    }

                    // ����ƶ���ǰ����body���ƶ�ʧ��
                    // ����false
                    return res;
                }
                break;
            case "w":
                if (direction != Direction.DOWN) {
                    y -= 10;
                    Point movePoint1 = new Point(x, y < 0 ? LENGTH + y : y);
                    snake.setDirection(Direction.UP);
                    /*Point tmp = new Point(foodPoint.x(), foodPoint.y());*/
                    boolean res = snake.move(movePoint1, foodPoints, body);
                    if (foodPoints.contains(movePoint1)) {
                        GenerateFood();
                        foodPoints.remove(movePoint1);
                    }
                    return res;
                }
                break;
            case "d":
                if (direction != Direction.LEFT) {
                    x += 10;
                    Point movePoint1 = new Point(x % LENGTH, y);
                    snake.setDirection(Direction.RIGHT);
                    boolean res = snake.move(movePoint1, foodPoints, body);
                    if (foodPoints.contains(movePoint1)) {
                        GenerateFood();
                        foodPoints.remove(movePoint1);
                    }
                    return res;
                }
                break;
            case "s":
                if (direction != Direction.UP) {
                    y += 10;
                    Point movePoint1 = new Point(x, y % LENGTH);
                    snake.setDirection(Direction.DOWN);
                    boolean res = snake.move(movePoint1, foodPoints, body);
                    if (foodPoints.contains(movePoint1)) {
                        GenerateFood();
                        foodPoints.remove(movePoint1);
                    }
                    return res;
                }
                break;
            case "t":
                Stack<Point> stack = new Stack<>();
                while (!snake.getQueue().isEmpty())
                    stack.push(snake.getQueue().poll());
                while (!stack.isEmpty())
                    snake.getQueue().offer(stack.pop());
                break;
            case "y":
                Stack<Point> stack1 = new Stack<>();
                Point tmp = new Point(
                        snake.getQueue().peek().x(),
                        snake.getQueue().peek().y());
                while (!snake.getQueue().isEmpty())
                    stack1.push(snake.getQueue().poll());
                while (!stack1.isEmpty())
                    snake.getQueue().offer(stack1.pop());
                snake.getHead().setX(tmp.x());
                snake.getHead().setY(tmp.y());
                switch (snake.getDirection()) {
                    case LEFT:
                        snake.setDirection(Direction.RIGHT);
                        break;
                    case DOWN:
                        snake.setDirection(Direction.UP);
                        break;
                    case RIGHT:
                        snake.setDirection(Direction.LEFT);
                        break;
                    case UP:
                        snake.setDirection(Direction.DOWN);
                        break;
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
    public void SendUDPSnakes() throws IOException, CloneNotSupportedException, InterruptedException {
        sendUDPSnake(snakes, foodPoints, clientInfos, mutex);
    }

    /* ���÷���sendUDPSnake */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void sendUDPSnake(HashMap<String, Snake> snakes,
                                     HashSet<Point> foodPoints,
                                     Vector<ClientInfo> clientInfos,
                                     Semaphore mutex) throws IOException, InterruptedException {
        UDPSnake UDPsnake = new UDPSnake(snakes, foodPoints);

        /*// תΪ�ַ���
        mutex.acquire(2);
        String msg = UDPsnake.toString();
        mutex.release(2);

        //תΪbyte
        byte[] msgBody = msg.getBytes(StandardCharsets.UTF_8);*/

        /*// ��¡snakes
        HashMap<String, Snake> tmpSnakes = new HashMap<>();
        for (var entry : snakes.entrySet())
            tmpSnakes.put(entry.getKey(), entry.getValue().clone());

        // ��¡food
        HashSet<Point> tmpFoods = new HashSet<>();
        for (var food : foodPoints)
            tmpFoods.add(food.clone());*/
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

        // תΪObject��
        ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream);

        // ��ͬ��
        mutex.acquire(2);
        objectStream.writeObject(UDPsnake);
        mutex.release(2);

        byte[] arr = byteArrayStream.toByteArray();

        for (int i = 0; i < clientInfos.size(); i++) {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(
                    new byte[65535],
                    65535,
                    clientInfos.get(i).getIP(),
                    clientInfos.get(i).getPORT());

            packet.setData(arr);//���DatagramPacket
            socket.send(packet);//����

            /*//���͸����
            DatagramPacket msgPacket = new DatagramPacket(
                    msgBody,
                    msgBody.length,
                    clientInfos.get(i).getIP(),
                    clientInfos.get(i).getPORT());
            socket.send(msgPacket);*/

        }
        objectStream.close();
        byteArrayStream.close();


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
        private final Vector<ClientInfo> clientInfos;
        private final Semaphore mutex;
        private final SleepTime sleepTime;

        public PushSnakeThread(HashMap<String, Snake> snakes,
                               HashSet<Point> foodPoints,
                               HashSet<Point> body,
                               Vector<ClientInfo> clientInfos,
                               Semaphore mutex,
                               SleepTime sleepTime) {
            this.snakes = snakes;
            this.foodPoints = foodPoints;
            this.body = body;
            this.clientInfos = clientInfos;
            this.mutex = mutex;
            this.sleepTime = sleepTime;
        }


        @Override
        public void run() {
            while (true) {
                // push snakes

                List<String> diePlayer = new ArrayList<>(100);
                try {
                    mutex.acquire();
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
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
                for (var name : diePlayer) {
                    snakes.remove(name);
                }
                try {
                    mutex.release();
                    SendUDPSnakes();
                    Thread.sleep(sleepTime.getTime());
                } catch (InterruptedException | IOException | CloneNotSupportedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        /* ��ÿ����ҷ���UDPSnakes */
        public void SendUDPSnakes() throws IOException, CloneNotSupportedException, InterruptedException {
            sendUDPSnake(snakes, foodPoints, clientInfos, mutex);
        }

        // �ڵ�ͼ���һ������ʳ��
        public void GenerateFood() throws InterruptedException {
            generateFood(body, foodPoints);
        }
    }
}
