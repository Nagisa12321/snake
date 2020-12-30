import com.jtchen.model.Snake;
import com.jtchen.struct.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author jtchen
 * @version 1.0
 * @date 2020/12/30 15:26
 */
public class test1 {
    private JPanel test1;
    private JButton button1;

    public test1() {
        Snake snake = new Snake(new com.jtchen.struct.Point[]{new com.jtchen.struct.Point(1, 0), new com.jtchen.struct.Point(1, 1),
                new com.jtchen.struct.Point(1, 2), new com.jtchen.struct.Point(1, 3), new com.jtchen.struct.Point(1, 4)}, Color.black);
//        com.jtchen.model.PlayerMap map = new com.jtchen.model.PlayerMap(20, new com.jtchen.model.Snake[]{snake});


        button1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if (e.getKeyChar() == 'w') {
                    int x = snake.getHead().x();
                    int y = snake.getHead().y();
                    snake.move(new com.jtchen.struct.Point(x, ++y));
                } else if (e.getKeyChar() == 's') {
                    int x = snake.getHead().x();
                    int y = snake.getHead().y();
                    snake.move(new com.jtchen.struct.Point(x, --y));
                } else if (e.getKeyChar() == 'a') {
                    int x = snake.getHead().x();
                    int y = snake.getHead().y();
                    snake.move(new com.jtchen.struct.Point(--x, y));
                } else if (e.getKeyChar() == 'd') {
                    int x = snake.getHead().x();
                    int y = snake.getHead().y();
                    snake.move(new Point(++x, y));
                }
//                map.draw();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("test1");
        frame.setContentPane(new test1().test1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocation(500, 250);
        frame.setVisible(true);
    }
}
