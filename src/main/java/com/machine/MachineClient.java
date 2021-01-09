package com.machine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import static com.cc.UDPClient.getDatagramSocket;

/**
 * @author jtchen
 * @version 1.0
 * @date 2021/1/9 13:06
 */
public class MachineClient implements Runnable {
    private final String playerName;

    private final InetAddress IP;

    public MachineClient(String playerName, String host) throws UnknownHostException {
        this.playerName = playerName;
        IP = InetAddress.getByName(host);
    }

    @Override
    public void run() {
        DatagramSocket socket = establish(IP, playerName);
        if (socket == null) return;

        // 开启发线程
        try {
            while (true) {
                String op = randomAction();

                //拼接消息串
                String msg = playerName + " " + op;
                byte[] msgBody = msg.getBytes(StandardCharsets.UTF_8);

                //发送给服务器
                DatagramPacket msgPacket = new DatagramPacket(msgBody, msgBody.length, IP, 8090);
                socket.send(msgPacket);
                Thread.sleep((int) (Math.random() * 500 + 100));
            }
        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    public String randomAction() {
        String[] action = {"left", "right", "up", "down"};

        int max = 100, min = 1;
        int ran2 = (int) (Math.random() * (max - min) + min);
        int idx = (ran2 % action.length);
        return action[idx];
    }

    private DatagramSocket establish(InetAddress serverIp, String name) {
        return getDatagramSocket(serverIp, name);
    }
}
