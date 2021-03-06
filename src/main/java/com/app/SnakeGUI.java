package com.app;

import com.formdev.flatlaf.FlatLightLaf;
import com.jtchen.UDPServer;

import com.cc.UDPClient;
import com.machine.MachineClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
    private JTable table1;
    private JTextField textField3;
    private JButton JOINAMICHINEButton;
    private final DefaultTableModel tableModel;

    public SnakeGUI() {

        tableModel = (DefaultTableModel) table1.getModel();
        tableModel.addColumn("player");
        tableModel.addColumn("Score");
        textField1.setText("jt_laptop");
        textField2.setText("cc");
        HOSTAGAMEButton.addActionListener(e -> {
            HOSTAGAMEButton.setEnabled(false);
            new Thread(new UDPServer()).start();
        });
        JOINAGAMEButton.addActionListener(e -> {
            String name = textField2.getText();
            String host = textField1.getText();
            try {
                if (name.isEmpty() || host.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "请好好输入信息O(∩_∩)O", "!!!!!!!", JOptionPane.ERROR_MESSAGE);
                } else {
                    Runnable client = new UDPClient(name, host, tableModel);
                    new Thread(client).start();

                    // 开启音乐
                    /*Music.play("./src/main/resources/bg.ogg",true);*/
                }
            } catch (UnknownHostException a) {
                JOptionPane.showMessageDialog(null, "请好好输入信息O(∩_∩)O", "!!!!!!!", JOptionPane.ERROR_MESSAGE);
                System.err.println(a.getMessage());
            }

        });
        JOINAMICHINEButton.addActionListener(e -> {
            String mcName = textField3.getText();
            String host = textField1.getText();
            try {
                if (mcName.isEmpty() || host.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "请好好输入信息O(∩_∩)O", "!!!!!!!", JOptionPane.ERROR_MESSAGE);
                } else {
                    new Thread(new MachineClient(mcName, host)).start();
                }
            } catch (UnknownHostException a) {
                JOptionPane.showMessageDialog(null, "请好好输入信息O(∩_∩)O", "!!!!!!!", JOptionPane.ERROR_MESSAGE);
                System.err.println(a.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        FlatLightLaf.install();

        JFrame frame = new JFrame("SnakeGUI");
        frame.setContentPane(new SnakeGUI().snake);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(500, 300);
        frame.pack();
        frame.setVisible(true);
    }
}
