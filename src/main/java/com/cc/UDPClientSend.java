package com.cc;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

public class UDPClientSend implements Runnable {
    private final InetAddress serverIP;
    private final int serverPort;
    private final String playerName;
    private final DatagramSocket socket;
    private final BlockingQueue<Integer> keyboardQueue;
    private  boolean superMode = false;
    private final HashMap<Integer, String> EventMap = new HashMap<>() {{
        put(KeyEvent.VK_UP, "up");
        put(KeyEvent.VK_DOWN, "down");
        put(KeyEvent.VK_LEFT, "left");
        put(KeyEvent.VK_RIGHT, "right");
    }};

    public UDPClientSend(InetAddress serverIP, int serverPort, String playerName, DatagramSocket socket, BlockingQueue<Integer> keyboardQueue) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.playerName = playerName;
        this.socket = socket;
        this.keyboardQueue = keyboardQueue;
    }


    public void run() {
        try {
            while (true) {
                int op = keyboardQueue.take();
                if(op==-1){
                    superMode=!superMode;
                    continue;
                }
                //拼接消息串
                String msg = playerName + " " + EventMap.get(op);
                byte[] msgBody = msg.getBytes(StandardCharsets.UTF_8);

                //发送给服务器
                DatagramPacket msgPacket = new DatagramPacket(msgBody, msgBody.length, serverIP, serverPort);
                socket.send(msgPacket);
                if(superMode){
                    System.out.println("superMode!");
                    for(int i = 0;i<7;++i){
                        Thread.sleep(4);
                        socket.send(msgPacket);
                    }
                }
            }
        }catch (IOException |InterruptedException e){
            System.err.println(e.getMessage());
        }
    }
}
