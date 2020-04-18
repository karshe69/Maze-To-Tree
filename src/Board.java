import Cell.*;
import Transition.Transition;
import Transition.MTMTransition;


import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Board extends JPanel implements ActionListener, MouseListener {
    Toolkit tk = Toolkit.getDefaultToolkit();

    private final int B_WIDTH = (int) tk.getScreenSize().getWidth(); // width of the screen
    private final int B_HEIGHT = (int) tk.getScreenSize().getHeight() - 50; // height of the screen

    private final int DELAY1 = 2;
    private final int DELAY2 = 20;
    private final double CellToWallRatio = 0.9;
    private final int CELLSIZE = 60;
    private final int MOVINGRATE = 15;
    private final int GOALSIZE = 10;
    private final double WALLRATE = 0.05;

    private int step = 0;

    private final Color CellColor = new Color(34, 53, 64);
    private final Color WallColor = new Color(169, 184, 201);
    private final Color StartColor = new Color(0, 184, 140);
    private final Color FinishColor = new Color(187, 80, 85);
    private final Color SearchedColor = new Color(79, 124, 165);
    private final Color SearchColor = new Color(116, 174, 144);


    private double mouseX = 0;
    private double mouseY = 0;

    private int startX = 0;
    private int startY = 0;
    private int finishX = 0;
    private int finishY = 0;

    private ArrayList<Cell> trees = new ArrayList<>();

    private Timer timer;

    private MazeCell[][] cells = new MazeCell[B_WIDTH / CELLSIZE][B_HEIGHT / CELLSIZE];

    private ArrayList<Integer> treeSize = new ArrayList<>();

    private ArrayList<MTMTransition> paths = new ArrayList<>();

    private ArrayList<Double> quickHelp = new ArrayList<>();
    private ArrayList<Double> quickHelp1 = new ArrayList<>();


    private ArrayList<TreeCell> searchCells = new ArrayList<>();
    private ArrayList<TreeCell> searchedCells = new ArrayList<>();

    public Board() {
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT)); // sets screen size
        setBackground(CellColor);
        addMouseListener(this);
        setBackground(CellColor);
        timer = new Timer(DELAY1, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (step == 0){
            resetCells();
            resetPaths();
            step = 1;
        }
        if (step == 1)
            kruskalStep();
        if (step == 2){
            while (trees.size() > 1)
                trees.remove(0);
            timer.setDelay(DELAY2);
            step = 3;
        }
        if (step == 3){
            double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            if(0 <= x/CELLSIZE && x/CELLSIZE < cells.length && 0 <= y/CELLSIZE && y/CELLSIZE < cells[0].length) {
                cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(CellColor);
                cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(WallColor);
                mouseX = x;
                mouseY = y;
                cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(StartColor);
                cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(StartColor);
                startX = (int)(mouseX/CELLSIZE);
                startY = (int)(mouseY/CELLSIZE);
            }
        }
        if (step == 4){
            double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            if(0 <= x/CELLSIZE && x/CELLSIZE < cells.length && 0 <= y/CELLSIZE && y/CELLSIZE < cells[0].length){
                if (cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].getCellColor() != StartColor){
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(CellColor);
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(WallColor);
                }
                if (cells[(int)(x/CELLSIZE)][(int)(y/CELLSIZE)].getCellColor() == CellColor){
                    mouseX = x;
                    mouseY = y;
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(FinishColor);
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(FinishColor);
                    finishX = (int)(mouseX/CELLSIZE);
                    finishY = (int)(mouseY/CELLSIZE);
                }
            }
        }
        if (step == 6)
            if (trees.get(0).getCellToWallRatio() == 0){
                trees.add(new TreeCell((TransformingCell) trees.remove(0)));
                step = 7;
            }
        if (step == 8)
            best_first_search();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawStuff(g);
    }

    private void drawStuff(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        for (MazeCell[] cll:cells)
            for (MazeCell cell:cll)
                cell.draw(g2);
        for (Cell cell:trees)
            cell.draw(g2);
        for (Cell cell:trees)
            cell.resetDrawn();
        if (step == 6)
            ((TransformingCell)trees.get(0)).move();
        if (step == 7)
            ((TreeCell)trees.get(0)).move();
    }

    private int quickSearchH(Double in, ArrayList<Double> arr){
        return quickSearch(arr, in,0, quickHelp.size() - 1);
    }

    private int quickSearch(ArrayList<Double> arr, Double in, int start, int finish){
        if (start > finish)
            return start;
        if (start == finish){
            if (in < arr.get(start))
                return start;
            return start + 1;
        }
        if (start + 1 == finish){
            if (in < arr.get(start))
                return start;
            if (in > arr.get(finish))
                return finish + 1;
            return finish;
        }
        int i = (start + finish) / 2;
        if (in > arr.get(i))
            return quickSearch(arr, in, i, finish);
        if (in < arr.get(i))
            return quickSearch(arr, in, start, i);
        return i;
    }

    private void kruskalStep(){
        if (paths.isEmpty()){
            step = 2;
            return;
        }
        int i = 0;
        paths.get(i).getCells()[1].resetSearched();
        boolean flag = paths.get(i).getCells()[0].dfsShell(paths.get(i).getCells()[1]);
        while (flag){
            paths.remove(i);
            if (paths.isEmpty()){
                step = 2;
                return;
            }
            i = 0;
            paths.get(i).getCells()[1].resetSearched();
            flag = paths.get(i).getCells()[0].dfsShell(paths.get(i).getCells()[1]);
        }

        paths.get(i).getCells()[0].setTrans(paths.get(i));
        paths.get(i).getCells()[1].setTrans(paths.get(i));
        trees.add(paths.get(i).getCells()[0]);

        paths.remove(i);
    }

    public void best_first_search() {
        TreeCell searching = searchCells.remove(0);
        if (searching.getArrX() == finishX && searching.getArrY() == finishY) {
            step = 9;
            return;
        }
        searching.colorBackwards(SearchedColor, SearchColor);
        double value = quickHelp.remove(0);
        int index = quickSearchH(value, quickHelp1);
        quickHelp1.add(index, value);
        searchedCells.add(index, searching);
        for (Transition transition : searching.getTransitions())
            if (transition != null)
                if (!treeCellExistsIn((TreeCell)transition.getCells()[1], searchedCells)){
                    value = value1((TreeCell)transition.getCells()[1]);
                    index = quickSearchH(value, quickHelp);
                    quickHelp.add(index, value);
                    searchCells.add(index, (TreeCell)transition.getCells()[1]);
                }
        searchCells.get(0).colorBackwards(SearchColor, WallColor);
        searchCells.get(0).colorBackwards(SearchColor, SearchedColor);
    }

    public boolean treeCellExistsIn(TreeCell query, ArrayList<TreeCell> list){
        return false;
    }

    public double value1(TreeCell query){
        int x = query.getArrX() - finishX;
        int y = query.getArrY() - finishY;
        return x*x + y*y;
    }

    public void resetCells(){
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[i].length; j++)
                cells[i][j] = new MazeCell(i, j, CELLSIZE, CellToWallRatio, WallColor, CellColor);
    }

    public void resetPaths(){
        MTMTransition trn;
        int ind;
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[i].length; j++)
                for (int k = 0; k <= 1; k++)
                    if (i + k < cells.length && j + (1 - k) < cells[i].length){
                        trn = new MTMTransition(cells[i][j], cells[i + k][j + (1 - k)], CellColor,  CELLSIZE, CellToWallRatio);
                        ind = quickSearchH(trn.getWeight(), quickHelp);
                        paths.add(ind, trn);
                        quickHelp.add(ind, trn.getWeight());
                    }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (step == 3){
            double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            if(0 <= x/CELLSIZE && x/CELLSIZE <= cells.length && 0 <= y/CELLSIZE && y/CELLSIZE <= cells[0].length) {
                cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setCellColor(CellColor);
                cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setWallColor(WallColor);
                mouseX = x;
                mouseY = y;
                cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setCellColor(StartColor);
                cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setWallColor(StartColor);
                startX = (int) (mouseX / CELLSIZE);
                startY = (int) (mouseY / CELLSIZE);
                step = 4;
                trees.remove(0);
                trees.add(0, cells[startX][startY]);
                return;
            }
        }
        if (step == 4){
            double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            if(0 <= x/CELLSIZE && x/CELLSIZE <= cells.length && 0 <= y/CELLSIZE && y/CELLSIZE <= cells[0].length){
                if (cells[(int)(x/CELLSIZE)][(int)(y/CELLSIZE)].getCellColor() != StartColor){
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(CellColor);
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(WallColor);
                    mouseX = x;
                    mouseY = y;
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(FinishColor);
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(FinishColor);
                    finishX = (int)(mouseX/CELLSIZE);
                    finishY = (int)(mouseY/CELLSIZE);
                    step = 5;
                    return;
                }
            }
        }
        if (step == 5){
            trees.remove(0);
            trees.add(new TransformingCell(cells[startX][startY], MOVINGRATE, WALLRATE, 0, 0, GOALSIZE, B_HEIGHT, B_WIDTH, treeSize));
            treeSize.add(0, 1);
            for (int i = 0; i < 15; i++)
                treeSize.add(treeSize.size(), 0);
            step = 6;
        }
        if (step == 7){
            boolean flag = true;
            for (MazeCell[] cll:cells)
                for (MazeCell cell:cll)
                    for (Transition trans:cell.getTransitions())
                        if (trans != null) {
                            flag = false;
                            break;
                        }
            if (flag){
                step = 8;
                quickHelp.clear();
                quickHelp.add(value1((TreeCell) trees.get(0)));
                searchCells.add((TreeCell) trees.get(0));
            }
        }
        if (step == 9){
            quickHelp.clear();
            quickHelp1.clear();
            paths.clear();
            searchCells.clear();
            searchedCells.clear();
            treeSize.clear();
            trees.clear();
            timer.setDelay(DELAY1);
            step = 0;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}