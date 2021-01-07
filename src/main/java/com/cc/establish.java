package com.cc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class establish {
    public static DatagramSocket establish(String name){
        try{
            DatagramSocket socket = new DatagramSocket(0);
            byte []sendName = name.getBytes(StandardCharsets.UTF_8);
            DatagramPacket request = new DatagramPacket(sendName,sendName.length);
            DatagramPacket respond = new DatagramPacket(new byte[1],1);

            //poke一下服务器,超时抛异常
            socket.setSoTimeout(5000);
            socket.send(request);
            socket.receive(respond);

            return socket;
        }catch (IOException e){
            System.out.println(e.getMessage()+"(establish)");
            return null;
        }
    }
}
