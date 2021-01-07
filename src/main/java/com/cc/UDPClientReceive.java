package com.cc;

import java.net.DatagramSocket;

public class UDPClientReceive implements Runnable {
    DatagramSocket socket;
    public UDPClientReceive(DatagramSocket socket){
        this.socket = socket;
    }
    public void run() {
        while (true){
            //接收对象

            //画出地图

        }
    }
}
