package com.jtchen;

import java.util.Vector;

public class SendSnakes implements Runnable{
    private Vector<ClientInfo> clientInfos;
    private Vector<String> operation;

    public SendSnakes(Vector<ClientInfo> clientInfos, Vector<String> operation) {
        this.clientInfos = clientInfos;
        this.operation = operation;
    }


    public void run() {
        while (true) {
            if (clientInfos.isEmpty()) continue;
            if (!operation.isEmpty()) {

            }
            // ...
        }
    }
}
