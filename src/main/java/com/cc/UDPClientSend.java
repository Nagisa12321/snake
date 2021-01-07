package com.cc;

import java.net.DatagramSocket;

public class UDPClientSend implements Runnable {
    DatagramSocket socket;

    public UDPClientSend(DatagramSocket socket) {
        this.socket = socket;
    }

    public void run() {

    }
}
