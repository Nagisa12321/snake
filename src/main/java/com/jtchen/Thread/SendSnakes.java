package com.jtchen.Thread;

import com.jtchen.ClientInfo;
import com.jtchen.PlayerMap;
import com.struct.Point;
import com.struct.Snake;
import com.struct.UDPSnake;

import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class SendSnakes implements Runnable {
    public static final int LENGTH = 30; // ��ʵ����

    private Vector<ClientInfo> clientInfos; // �û�IP PORT�б�

    private Vector<String> operation; // ��������

    private HashMap<String, Snake> snakes; // ��

    private Point foodPoint; // ʳ��ĵ�

    private HashSet<Point> body; // ����㼯

    public SendSnakes(Vector<ClientInfo> clientInfos,
                      Vector<String> operation,
                      HashMap<String, Snake> snakes,
                      HashSet<Point> body) {
        this.clientInfos = clientInfos;
        this.operation = operation;
        this.snakes = snakes;
        this.body = body;

        // ��ͼ�������ʳ��
        GenerateFood();
    }


    public void run() {
        while (true) {
            if (clientInfos.isEmpty()) continue;
            if (!operation.isEmpty()) {
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
                    snakes.remove(name);
                    System.out.println("��� " + name + "������, �Ѿ��ӱ����Ƴ�");
                }

                // ��������б� ����UDPSnakes�������
                try {
                    SendUDPSnakes();
                } catch (IOException e) {
                    System.err.println(e.getMessage() + "����UDPSnakes�������ʧ��!");
                }
            }
        }
    }

    /* �����ַ��� ���name��op */
    public NameAndOperation getNameAndOperation() {
        // ����һ��name �� op �� String ���ҽ���һ��
        String s = operation.remove(0);

        String[] tmp = s.split(" ");
        String name = tmp[0];
        String op = tmp[1];
        return new NameAndOperation(name, op);
    }

    /* ���ݳ��ӵĲ����ƶ�ĳ����, ���Ҹı��ߵķ��� */
    public boolean moveSnake(String operation, Snake snake) {
        int direction = snake.getDirection();

        int x = snake.getHead().x();
        int y = snake.getHead().y();

        // �ж��Ƿ���ı� �������¸ı䷽��
        switch (operation) {
            // 0�� 1�� 2�� 3��
            case "left":
                if (direction != 3) {
                    Point movePoint = new Point(x - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--x) : --x, y);
                    snake.setDirection(2);
                    System.err.println("snake.setDirection(2);" + snake.getDirection());
                    // �����Ҫ��������ʳ��, ��������ʳ��
                    if (movePoint.equals(foodPoint)) GenerateFood();

                    // ����ƶ���ǰ����body���ƶ�ʧ��
                    // ����false
                    return snake.move(movePoint, foodPoint, body);
                }
            case "up":
                if (direction != 1) {
                    Point movePoint = new Point(x, y - 1 < 0 ? PlayerMap.LENGTH - Math.abs(--y) : --y);
                    snake.setDirection(0);
                    if (movePoint.equals(foodPoint)) GenerateFood();
                    return snake.move(movePoint, foodPoint, body);
                }
            case "right":
                if (direction != 2) {
                    Point movePoint = new Point(++x % PlayerMap.LENGTH, y);
                    snake.setDirection(3);
                    if (movePoint.equals(foodPoint)) GenerateFood();
                    return snake.move(movePoint, foodPoint, body);
                }
            case "down":
                if (direction != 0) {
                    Point movePoint = new Point(x, ++y % PlayerMap.LENGTH);
                    snake.setDirection(1);
                    if (movePoint.equals(foodPoint)) GenerateFood();
                    return snake.move(movePoint, foodPoint, body);
                }
        }
        // ���ʲô��û�����򷵻�true
        // ����˵�ߵķ�������ǰ, �㰴�˺�, ��ʲôҲû����
        // ���仰˵ �߲����Ļ�����true
        return true;
    }

    // �ڵ�ͼ���һ������ʳ��
    public void GenerateFood() {
        int x = (int) (Math.random() * LENGTH);
        int y = (int) (Math.random() * LENGTH);

        Point tmp = new Point(x, y);
        if (body.contains(tmp)) {
            GenerateFood();
        } else
            foodPoint = new Point(x, y);
    }

    /* ��ÿ����ҷ���UDPSnakes */
    public void SendUDPSnakes() throws IOException {
        UDPSnake snake = new UDPSnake(snakes, foodPoint);

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

    public static class NameAndOperation {
        private final String name;
        private final String Op;


        public NameAndOperation(String name, String op) {
            this.name = name;
            this.Op = op;
        }
    }
}
