package com.cc;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Vector;

public class UDPClientSend implements Runnable {
    private final InetAddress serverIP;
    private final int serverPort;
    private final String playerName;
    private final DatagramSocket socket;
    private final Vector<Integer> keyboardQueue;
    private final HashMap<Integer, String> EventMap = new HashMap<>() {{
        put(KeyEvent.VK_UP, "up");
        put(KeyEvent.VK_DOWN, "down");
        put(KeyEvent.VK_LEFT, "left");
        put(KeyEvent.VK_RIGHT, "right");
    }};

    public UDPClientSend(InetAddress serverIP, int serverPort, String playerName, DatagramSocket socket, Vector<Integer> keyboardQueue) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.playerName = playerName;
        this.socket = socket;
        this.keyboardQueue = keyboardQueue;
    }


    public void run() {
        try {
            while (true) {
                if (keyboardQueue.isEmpty()) continue;
                String msg = playerName + " " + EventMap.get(keyboardQueue.remove(0));
                byte[] msgBody = msg.getBytes(StandardCharsets.UTF_8);
                DatagramPacket msgPacket = new DatagramPacket(msgBody, msgBody.length, serverIP, serverPort);
                socket.send(msgPacket);
            }
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}
