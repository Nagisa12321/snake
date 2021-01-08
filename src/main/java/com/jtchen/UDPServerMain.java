package com.jtchen;

import com.struct.ClientInfo;
import com.struct.Point;
import com.struct.Snake;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("InfiniteLoopStatement")
public class UDPServerMain implements Runnable {
    public static final int PORT = 8088;

    public static Color randomColor() {

        float x = (float) (Math.random() * 255);
        float y = (float) (Math.random() * 255);
        float z = (float) (Math.random() * 255);

        return Color.getHSBColor(x, y, z);
    }

    @Override
    public void run() {
        System.out.println("�ѿ���������main����!");

        // main��GetOperationά��������б�
        BlockingQueue<ClientInfo> clientInfos = new LinkedBlockingQueue<>();

        // SendSnakes��GetOperationά���Ĳ�������
        BlockingQueue<String> operation = new LinkedBlockingQueue<>();

        // main��SendSnakesά����snakes map
        // SendSnakes���move snakeʧ���˿��Դӱ���ɾ��
        HashMap<String, Snake> snakes = new HashMap<>();

        // main��Snakeά��������㼯
        HashSet<Point> body = new HashSet<>();

        // �������߳�
        new Thread(new SendSnakes(clientInfos, operation, snakes, body)).start();
        new Thread(new GetOperation(operation)).start();

        while (true) {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {

                // ��������
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);
                int clientPort = packet.getPort();
                InetAddress clientIP = packet.getAddress();

                // �������
                clientInfos.add(new ClientInfo(clientPort, clientIP));

                // ������
                String name = new String(packet.getData(),
                        0, packet.getLength(), StandardCharsets.UTF_8);

                System.out.println("��λ��ҽ����˷����� id: " + name);

                Point p1 = new Point(1, 0);
                Point p2 = new Point(1, 1);
                Point p3 = new Point(1, 2);
                Point p4 = new Point(1, 3);

                Snake snake = new Snake(new Point[]{p1, p2, p3, p4}, randomColor());

                // ����㼯
                body.add(p1);
                body.add(p2);
                body.add(p3);
                body.add(p4);

                // ��ֻ����HashMap �д���Snake
                snakes.put(name, snake);

                for (var entry : snakes.entrySet()) {
                    System.out.println("name " + entry.getKey());
                    System.out.println("name.size()" + name.length());
                    System.out.println("snake " + entry.getValue().getDirection());
                }

                // �������ӳɹ�����Ϣ
                socket.send(new DatagramPacket(new byte[1], 1, clientIP, clientPort));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
