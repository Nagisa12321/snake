package com.jtchen.Thread;

import java.io.IOError;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

public class GetOperation implements Runnable{
    public static final int PORT = 8090;

    private Vector<String> operation;

    public GetOperation( Vector<String> operation) {
        this.operation = operation;
    }

    public void run() {
        // ���ϵȴ���������
        while (true) {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {

                byte[] buff = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buff, 4096);
                socket.receive(packet);

                // �յ�����String �ǲ�����name���,
                String opStr = new String(buff,
                        0, packet.getLength(), StandardCharsets.UTF_8);

                // ���������, ��SendSnakes�����������Ҵ���
                operation.add(opStr);
            } catch (IOException e) {
                System.err.println(e.getMessage() + "(GetOperation)");
            }
        }
    }
}
