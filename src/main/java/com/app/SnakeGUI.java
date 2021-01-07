package com.app;

import com.jtchen.Thread.UDPServerMain;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        HOSTAGAMEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HOSTAGAMEButton.setEnabled(false);
                new Thread(new UDPServerMain()).start();
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
