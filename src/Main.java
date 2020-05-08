import java.awt.*;
import javax.swing.JFrame;


public class Main extends JFrame {
    public Board board;

    public Main() {
        initUI();
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