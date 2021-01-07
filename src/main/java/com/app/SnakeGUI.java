package com.app;

import javax.swing.*;

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

    public static void main(String[] args) {
        JFrame frame = new JFrame("SnakeGUI");
        frame.setContentPane(new SnakeGUI().snake);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(500, 300);
        frame.pack();
        frame.setVisible(true);
    }
}
