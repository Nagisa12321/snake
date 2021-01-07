package com.cc;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

public class UDPClient extends Frame {
    Vector<Integer> keyboardQueue;

    public UDPClient(){

    }


    private class KeyMonitor extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            // 空格则退出
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                System.exit(0);

                //加入队列
            } else if(e.getKeyCode()==KeyEvent.VK_UP||e.getKeyCode()==KeyEvent.VK_DOWN||
                      e.getKeyCode()==KeyEvent.VK_LEFT||e.getKeyCode()==KeyEvent.VK_RIGHT ) {
                keyboardQueue.add(e.getKeyCode());
            }
        }
    }
}
