package com.jtchen;

import java.io.IOException;
import java.net.*;
import java.util.Vector;

public class UDPServerMain {
    public static final int PORT = 8088;

    public static void main(String[] args) {
        Vector<ClientInfo> clientInfos = new Vector<>();
        Vector<String> operation = new Vector<>();

        new Thread(new SendSnakes(clientInfos, operation)).start();
        new Thread(new GetOperation(operation)).start();

        while (true) {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                /*byte[] acceptMsg = new byte[1024];*/
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket.receive(packet);
                int clientPort = packet.getPort();
                InetAddress clientIP = packet.getAddress();

                clientInfos.add(new ClientInfo(clientPort, clientIP));

            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
