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

public class UDPClientReceive implements Runnable {
    private DatagramSocket socket;
    private Vector<UDPSnake> ObjQueue;

    public UDPClientReceive(DatagramSocket socket,Vector<UDPSnake> ObjQueue) {
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

                UDPSnake msgOfSnake = (UDPSnake)os.readObject();
                os.close();
                bs.close();

                //传入消息队列让主进程画
                ObjQueue.add(msgOfSnake);
            }
        } catch (IOException|ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
