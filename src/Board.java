import Cell.*;
import Transition.Transition;
import Transition.MTMTransition;


import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javax.swing.*;

public class Board extends JPanel implements ActionListener, MouseListener {
    Toolkit tk = Toolkit.getDefaultToolkit();

    private final int B_WIDTH = (int) tk.getScreenSize().getWidth(); // width of the screen
    private final int B_HEIGHT = (int) tk.getScreenSize().getHeight() - 50; // height of the screen

    private final int DELAY1 = 2;
    private final int DELAY2 = 20;
    private final double CellToWallRatio = 0.9;
    private final int CELLSIZE = 50;
    private final int MOVINGRATE = 15;
    private final int GOALSIZE = 10;
    private final double WALLRATE = 0.05;

    private int creationFlag = 3;

    private int step = 0;

    private final double EllersChance = 0.5;

    private final Color CellColor = new Color(34, 53, 64);
    private final Color WallColor = new Color(169, 184, 201);
    private final Color StartColor = new Color(0, 184, 140);
    private final Color FinishColor = new Color(187, 80, 85);
    private final Color SearchedColor = new Color(79, 124, 165);
    private final Color SearchColor = new Color(116, 174, 144);


    private double mouseX = 0;
    private double mouseY = 0;

    private int startX = 0; // also used in dfs for remembering which cell im currently on.
    private int startY = 0; // also used in dfs for remembering which cell im currently on and for eller's to remember which row im currently on.
    private int finishX = 0;
    private int finishY = 0;

    private ArrayList<Cell> trees = new ArrayList<>();

    private Timer timer;

    private MazeCell[][] cells = new MazeCell[B_WIDTH / CELLSIZE][B_HEIGHT / CELLSIZE];

    private int[][] intCells = new int[cells.length][cells[0].length]; // for dfs

    private ArrayList<Integer> treeSize = new ArrayList<>();

    private ArrayList<MTMTransition> paths = new ArrayList<>(); // for kruskal

    private ArrayList<Double> binHelp = new ArrayList<>(); // for anything related to binary searching
    private ArrayList<Double> binHelp1 = new ArrayList<>(); // for anything related to binary searching


    private ArrayList<TreeCell> searchCells = new ArrayList<>();
    private ArrayList<TreeCell> searchedCells = new ArrayList<>();

    Random rnd = new Random();

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
            if (creationFlag == 1)
                resetPaths();
            if (creationFlag == 2){
                startX = rnd.nextInt(cells.length);
                startY = rnd.nextInt(cells[startX].length);
                intCells[startX][startY] = -1;
                trees.add(cells[startX][startY]);
            }
            if (creationFlag == 3)
                startY = 0;
            step = 1;
        }
        if (step == 1){
            if (creationFlag == 1)
                kruskalStep();
            if (creationFlag == 2)
                dfsStep();
            if (creationFlag == 3)
                ellerStep();
        }

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

    private int binSearchH(Double in, ArrayList<Double> arr){
        return binSearch(arr, in,0, binHelp.size() - 1);
    }

    private int binSearch(ArrayList<Double> arr, Double in, int start, int finish){
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
            return binSearch(arr, in, i, finish);
        if (in < arr.get(i))
            return binSearch(arr, in, start, i);
        return i;
    }

    public void best_first_search() {
        TreeCell searching = searchCells.remove(0);
        if (searching.getArrX() == finishX && searching.getArrY() == finishY) {
            step = 9;
            return;
        }
        searching.colorBackwards(SearchedColor, SearchColor);
        double value = binHelp.remove(0);
        int index = binSearchH(value, binHelp1);
        binHelp1.add(index, value);
        searchedCells.add(index, searching);
        for (Transition transition : searching.getTransitions())
            if (transition != null)
                if (!treeCellExistsIn((TreeCell)transition.getCells()[1], searchedCells)){
                    value = value1((TreeCell)transition.getCells()[1]);
                    index = binSearchH(value, binHelp);
                    binHelp.add(index, value);
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
                        ind = binSearchH(trn.getWeight(), binHelp);
                        paths.add(ind, trn);
                        binHelp.add(ind, trn.getWeight());
                    }
    }

    public void ellersHelper(int a, int b){
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j <= startY; j++)
                if (intCells[i][j] == a)
                    intCells[i][j] = b;
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
                binHelp.clear();
                binHelp.add(value1((TreeCell) trees.get(0)));
                searchCells.add((TreeCell) trees.get(0));
            }
        }
        if (step == 9){
            binHelp.clear();
            binHelp1.clear();
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

    // maze creating algorithms:

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

    private void dfsStep(){
        int temp = intCells[startX][startY];
        MazeCell cell = cells[startX][startY];
        cell.setSearched(true);
        ArrayList<Integer> arr = new ArrayList<>();
        if (startX > 0)
            if (!cells[startX - 1][startY].isSearched())
                arr.add(startX - 1 + startY * cells.length);
        if (startY > 0)
            if (!cells[startX][startY - 1].isSearched())
                arr.add(startX + (startY - 1) * cells.length);
        if (startX + 1 < cells.length)
            if (!cells[startX + 1][startY].isSearched())
                arr.add(startX + 1 + startY * cells.length);
        if (startY + 1 < cells[startX].length)
            if (!cells[startX][startY + 1].isSearched())
                arr.add(startX + (startY + 1) * cells.length);
        if (arr.size() == 0){
            if (temp == -1)
                step = 2;
            else{
                startX = temp%cells.length;
                startY = temp/cells.length;
            }
            return;
        }
        temp = arr.get(rnd.nextInt(arr.size()));
        startX = temp%cells.length;
        startY = temp/cells.length;
        intCells[startX][startY] = cell.getArrX() + cell.getArrY() * cells.length;
        Transition transition = new MTMTransition(cell, cells[startX][startY], CellColor,  CELLSIZE, CellToWallRatio);
        cell.setTrans(transition);
        cells[startX][startY].setTrans(transition);
    }

    public void ellerStep(){
        double chance = EllersChance;
        if (startY == cells[0].length - 1){
            chance = 1;
            step = 2;
        }

        if (!(startY == 0)){
            int[] arr = new int[cells.length * startY];
            for (int i = 0; i < cells.length; i++)
                arr[i] = 0;
            for (int i = 0; i < cells.length; i++){
                arr[intCells[i][startY - 1] - 1] ++;
            }

            ArrayList<Boolean> arr1 = new ArrayList<>();
            for (int i = 0; i < arr.length; i++){
                if (arr[i] != 0){
                    arr1.add(true);
                    for (int j = 0; j < arr[i] - 1; j++)
                        if (rnd.nextDouble() <= chance)
                            arr1.add(true);
                        else
                            arr1.add(false);
                }

                for (int j = 0; j < cells.length; j++)
                    if (i + 1 == intCells[j][startY - 1])
                        if (arr1.remove(rnd.nextInt(arr1.size())) && intCells[j][startY - 1] != intCells[j][startY]){
                            Transition transition = new MTMTransition(cells[j][startY], cells[j][startY - 1], CellColor,  CELLSIZE, CellToWallRatio);
                            cells[j][startY - 1].setTrans(transition);
                            cells[j][startY].setTrans(transition);
                            intCells[j][startY] = intCells[j][startY - 1];
                        }
            }
        }

        for (int i = 0; i < cells.length; i++)
            if (intCells[i][startY] == 0)
                intCells[i][startY] = i + startY * cells.length + 1;

        for (int i = 0; i < cells.length - 1; i++)
            if (rnd.nextDouble() <= chance && intCells[i][startY] != intCells[i + 1][startY]){
                Transition transition = new MTMTransition(cells[i][startY], cells[i + 1][startY], CellColor,  CELLSIZE, CellToWallRatio);
                cells[i][startY].setTrans(transition);
                cells[i + 1][startY].setTrans(transition);
                ellersHelper(intCells[i + 1][startY], intCells[i][startY]);
            }

        for (int i = 0; i < cells.length; i++)
            if (intCells[i][startY] > startY * cells.length)
                trees.add(cells[i][startY]);
        startY += 1;
    }

    public void recursiveDivisionStep(){
        //coming soon
    }

    public void primStep(){
        //coming soon
    }

    public void wilsonStep(){
        //coming soon
    }
}