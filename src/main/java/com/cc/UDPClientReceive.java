package com.cc;

import com.struct.UDPSnake;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

public class UDPClientReceive implements Runnable {
    private final DatagramSocket socket;
    private final BlockingQueue<UDPSnake> ObjQueue;

    public UDPClientReceive(DatagramSocket socket,BlockingQueue<UDPSnake> ObjQueue) {
        this.socket = socket;
        this.ObjQueue = ObjQueue;
    }

    public void run() {
        try {
            while (true) {
                //接收对象,用byteStream反序列化
                byte[] buff = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                socket.receive(packet);
                ByteArrayInputStream bs = new ByteArrayInputStream(buff);
                ObjectInputStream os = new ObjectInputStream(bs);

                //反序列化对象
                UDPSnake msgOfSnake = (UDPSnake)os.readObject();
                os.close();
                bs.close();

                //传入消息队列让主进程画
                ObjQueue.put(msgOfSnake);
            }
        } catch (IOException|ClassNotFoundException|InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }
}
