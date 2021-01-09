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

    public static final int LENGTH = 40; // 真实长宽

    private final Semaphore mutex; // 用户列表互斥锁

    private final Vector<ClientInfo> clientInfos; // 用户IP PORT列表

    private final BlockingQueue<String> operation; // 操作队列

    private final HashMap<String, Snake> snakes; // 蛇

    private final HashSet<Point> foodPoints; // 食物的点

    private final HashSet<Point> body; // 身体点集

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

        // 地图随机生成食物
        Point foodPoint1 = new Point(10, 10);
        Point foodPoint2 = new Point(20, 20);
        foodPoints.add(foodPoint1);
        foodPoints.add(foodPoint2);
        new Thread(new PushSnakeThread(snakes, foodPoints, body, clientInfos, mutex, sleepTime)).start();
    }


    public void run() {
        while (true) {
            try {
                // 解析一个Snake的name和动作
                NameAndOperation nAo = getNameAndOperation();
                String name = nAo.name;
                String operation = nAo.Op;

                System.out.println("玩家 " + name + "发起了动作: " + operation);
                System.out.println("name.size()" + name.length());
                System.out.println("hashmap size: " + snakes.size());
                // 由name得到蛇
                Snake snake = snakes.get(name);

                if (snake == null) continue;

                System.out.println("snake : " + snake);

                // 由operation 和 具体snake操作蛇
                // 如果snake 撞到body, 则移除snake
                if (!moveSnake(operation, snake)) {

                    int idx = 0;
                    // 删除身体上的点
                    for (var point : snake.getQueue()) {
                        if (idx++ % 2 == 0)
                            foodPoints.add(point);
                        body.remove(point);
                    }

                    snakes.remove(name);
                    System.out.println("玩家 " + name + "死掉了, 已经从表中移除");
                }
                mutex.release();

                // 遍历玩家列表 发送UDPSnakes给玩家们
                SendUDPSnakes();

            } catch (InterruptedException | IOException | CloneNotSupportedException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /* 解析字符串 获得name和op */
    public NameAndOperation getNameAndOperation() throws InterruptedException {
        // 出队一个name 和 op 的 String 并且解析一番
        String s = operation.take();

        String[] tmp = s.split(" ");
        String name = tmp[0];
        String op = tmp[1];
        return new NameAndOperation(name, op);
    }

    /* 根据出队的操作移动某条蛇, 并且改变蛇的方向 */
    @SuppressWarnings("DuplicatedCode")
    public boolean moveSnake(String operation, Snake snake) throws InterruptedException {
        Direction direction = snake.getDirection();

        int x = snake.getHead().x();
        int y = snake.getHead().y();

        // 判断是否方向改变 并且重新改变方向
        mutex.acquire();
        switch (operation) {
            // 0上 1下 2左 3右
            case "left":
                if (direction != Direction.RIGHT) {
                    Point movePoint = new Point(x - 1 < 0 ? LENGTH - Math.abs(--x) : --x, y);
                    snake.setDirection(Direction.LEFT);
                    System.err.println("snake.setDirection(2);" + snake.getDirection());

                    boolean res = snake.move(movePoint, foodPoints, body);
                    // 如果将要遇到的是食物, 则在生成食物
                    if (foodPoints.contains(movePoint)) {
                        GenerateFood();

                        // 表中删除该食物的点
                        foodPoints.remove(movePoint);
                    }

                    // 如果移动蛇前面是body则移动失败
                    // 返回false
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
                    // 如果将要遇到的是食物, 则在生成食物
                    if (foodPoints.contains(movePoint1)) {
                        GenerateFood();

                        // 表中删除该食物的点
                        foodPoints.remove(movePoint1);
                    }

                    // 如果移动蛇前面是body则移动失败
                    // 返回false
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

        // 如果什么都没发生则返回true
        // 比如说蛇的方向是向前, 你按了后, 则什么也没发生
        // 换句话说 蛇不死的话返回true
        return true;
    }

    // 在地图随机一点生成食物
    public void GenerateFood() {
        generateFood(body, foodPoints);
    }

    /* 向每个玩家发送UDPSnakes */
    public void SendUDPSnakes() throws IOException, CloneNotSupportedException, InterruptedException {
        sendUDPSnake(snakes, foodPoints, clientInfos, mutex);
    }

    /* 公用方法sendUDPSnake */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void sendUDPSnake(HashMap<String, Snake> snakes,
                                     HashSet<Point> foodPoints,
                                     Vector<ClientInfo> clientInfos,
                                     Semaphore mutex) throws IOException, InterruptedException {
        UDPSnake UDPsnake = new UDPSnake(snakes, foodPoints);

        /*// 转为字符串
        mutex.acquire(2);
        String msg = UDPsnake.toString();
        mutex.release(2);

        //转为byte
        byte[] msgBody = msg.getBytes(StandardCharsets.UTF_8);*/

        /*// 克隆snakes
        HashMap<String, Snake> tmpSnakes = new HashMap<>();
        for (var entry : snakes.entrySet())
            tmpSnakes.put(entry.getKey(), entry.getValue().clone());

        // 克隆food
        HashSet<Point> tmpFoods = new HashSet<>();
        for (var food : foodPoints)
            tmpFoods.add(food.clone());*/
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

        // 转为Object流
        ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream);

        // 锁同步
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

            packet.setData(arr);//填充DatagramPacket
            socket.send(packet);//发送

            /*//发送给玩家
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

    /* 公用方法generateFood */
    private static void generateFood(HashSet<Point> body, HashSet<Point> foodPoints) {
        int x = (int) (Math.random() * LENGTH);
        int y = (int) (Math.random() * LENGTH);

        Point tmp = new Point(x, y);

        // 食物的点或者身体的点存在, 则在随机一次！
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
                            // 0上 1下 2左 3右
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
                            // 删除身体上的点
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

        /* 向每个玩家发送UDPSnakes */
        public void SendUDPSnakes() throws IOException, CloneNotSupportedException, InterruptedException {
            sendUDPSnake(snakes, foodPoints, clientInfos, mutex);
        }

        // 在地图随机一点生成食物
        public void GenerateFood() throws InterruptedException {
            generateFood(body, foodPoints);
        }
    }
}
