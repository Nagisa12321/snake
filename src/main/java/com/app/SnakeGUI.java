package com.app;

import com.cc.UDPClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

/**
 * @author jtchen
 * @version 1.0
 * @date 2021/1/7 15:10
 */
public class SnakeGUI {
    private JPanel snake;
    private JButton JOINAGAMEButton;
    private JButton HOSTAGAMEButton;
    private JTextField textField1;
    private JTextField textField2;

    public SnakeGUI() {
        JOINAGAMEButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = textField2.getText();
                String host = textField1.getText();
                try {
                    if (name.isEmpty() || host.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "请好好输入信息O(∩_∩)O", "!!!!!!!", JOptionPane.ERROR_MESSAGE);
                    } else {
                        Runnable client = new UDPClient(name, host);
                        new Thread(client).start();
                    }
                } catch (UnknownHostException a) {
                    JOptionPane.showMessageDialog(null, "请好好输入信息O(∩_∩)O", "!!!!!!!", JOptionPane.ERROR_MESSAGE);
                    System.err.println(a.getMessage());
                }

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SnakeGUI");
        frame.setContentPane(new SnakeGUI().snake);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(500, 300);
        frame.pack();
        frame.setVisible(true);
    }
}
