import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.EventQueue;
import javax.swing.JFrame;


public class Main extends JFrame {
    public Board board;

    public Main() {

        initUI();
        KeyListener listener = new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {

                int code = e.getKeyCode();
                String key = KeyEvent.getKeyText(code);

            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                String key = KeyEvent.getKeyText(code);

            }

            @Override
            public void keyTyped(KeyEvent e) {

            }
            private String keyboardLocation(int keybrd) {

                switch (keybrd) {

                    case KeyEvent.KEY_LOCATION_RIGHT:

                        return "Right";

                    case KeyEvent.KEY_LOCATION_LEFT:

                        return "Left";

                    case KeyEvent.KEY_LOCATION_NUMPAD:

                        return "NumPad";

                    case KeyEvent.KEY_LOCATION_STANDARD:

                        return "Standard";

                    case KeyEvent.KEY_LOCATION_UNKNOWN:

                    default:

                        return "Unknown";

                }

            }
        };
        this.addKeyListener(listener);
    }

    private void initUI() {
        board = new Board();
        add(board);

        setResizable(false);
        pack();

        setTitle("Maze To Tree");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            Main main = new Main();
            main.setVisible(true);
        });
    }
}