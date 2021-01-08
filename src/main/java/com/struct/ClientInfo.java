package com.struct;

import java.net.InetAddress;
import java.util.Vector;

public class ClientInfo {
    private final int PORT;
    private final InetAddress IP;

    public ClientInfo(int PORT, InetAddress IP) {
        this.PORT = PORT;
        this.IP = IP;
    }

    public int getPORT() {
        return PORT;
    }

    public InetAddress getIP() {
        return IP;
    }
}
