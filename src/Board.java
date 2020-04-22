import Cell.*;
import Transition.Transition;
import Transition.MTMTransition;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import javax.swing.*;

public class Board extends JPanel implements ActionListener, MouseListener {
    Toolkit tk = Toolkit.getDefaultToolkit();

    private final int B_WIDTH = (int) tk.getScreenSize().getWidth(); // width of the screen
    private final int B_HEIGHT = (int) tk.getScreenSize().getHeight(); // height of the screen

    private final int TOPBAR = 30;
    private final int BUTTON_WIDTH = 200;
    private final int BUTTON_HEIGHT = 50;
    private final int BUTTON_SPACE = 50;
    private final int BUTTON_FONT_SIZE = 30;

    private final int M_WIDTH = B_WIDTH - BUTTON_WIDTH - 2 * BUTTON_SPACE;
    private final int M_HEIGHT = B_HEIGHT - TOPBAR;

    private final int DELAY1 = 20;
    private final int DELAY2 = 20;
    private final int DELAYE = 300;

    private final double CellToWallRatio = 0.9;
    private final int CELLSIZE = 50;
    private final int MOVINGRATE = 15;
    private final int GOALSIZE = 10;
    private final double WALLRATE = 0.05;

    private int creationFlag = 0;

    private int step = 1;

    private final double EllersChance = 0.5;

    private final Color CellColor = new Color(34, 53, 64);
    private final Color WallColor = new Color(169, 184, 201);
    private final Color StartColor = new Color(0, 184, 140);
    private final Color FinishColor = new Color(187, 80, 85);
    private final Color SearchedColor = new Color(79, 124, 165);
    private final Color SearchColor = new Color(116, 174, 144);
    private final Color ButtonColor = new Color(44, 70, 89);

    private double mouseX = 0;
    private double mouseY = 0;


    private int startX; // also used in dfs for remembering which cell im currently on.
    private int startY; // also used in dfs for remembering which cell im currently on and for eller's to remember which row im currently on.
    private int finishX;
    private int finishY;

    private MazeCell[][] cells = new MazeCell[M_WIDTH / CELLSIZE][M_HEIGHT / CELLSIZE];

    private ArrayList<Cell> trees = new ArrayList<>();

    private Timer timer;

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
        timer = new Timer(DELAY1, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean stepF = false;
        if (step<-1000){
            stepF = true;
            step += 1000;
            step *= -1;
        }
        if (step == 1 || step == -1){
            binHelp.clear();
            binHelp1.clear();
            paths.clear();
            treeSize.clear();
            trees.clear();
            resetCells();
            startX = 0;
            startY = 0;
            finishX = cells.length - 1;
            finishY = cells[0].length - 1;
            timer.setDelay(DELAY1);
            if (creationFlag == 0){
                startX = rnd.nextInt(cells.length);
                startY = rnd.nextInt(cells[startX].length);
                for (int[] clls:intCells)
                    Arrays.fill(clls, 0);
                intCells[startX][startY] = -1;
                trees.add(cells[startX][startY]);
                addPaths(cells[startX][startY], paths, binHelp);
            }
            if (creationFlag == 1)
                resetPaths();
            if (creationFlag == 2){
                startX = rnd.nextInt(cells.length);
                startY = rnd.nextInt(cells[startX].length);
                intCells[startX][startY] = -1;
                trees.add(cells[startX][startY]);
            }
            if (creationFlag == 3){
                for (int[] clls:intCells)
                    Arrays.fill(clls, 0);
                timer.setDelay(DELAYE);
            }
            if (step == 1)
                step = 2;
        }
        if (step == 2){
            if (creationFlag == 0)
                primStep();
            if (creationFlag == 1)
                kruskalStep();
            if (creationFlag == 2)
                dfsStep();
            if (creationFlag == 3)
                ellerStep();
            if (creationFlag == 4)
                recursiveDivisionStep();
        }

        if (step == 3){
            while (trees.size() > 1)
                trees.remove(0);
            timer.setDelay(DELAY2);
            cells[startX][startY].setCellColor(StartColor);
            cells[startX][startY].setWallColor(StartColor);
            cells[finishX][finishY].setCellColor(FinishColor);
            cells[finishX][finishY].setWallColor(FinishColor);
            step = 4;
        }

        if (step == 5){
            double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            if ((int)(mouseX/CELLSIZE) != finishX || (int)(mouseY/CELLSIZE) != finishY){
                cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(CellColor);
                cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(WallColor);
            }
            if(0 <= x/CELLSIZE && x/CELLSIZE < cells.length && 0 <= y/CELLSIZE && y/CELLSIZE < cells[0].length) {
                mouseX = x;
                mouseY = y;
                if (cells[(int)(x/CELLSIZE)][(int)(y/CELLSIZE)].getCellColor() == CellColor){
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(StartColor);
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(StartColor);
                    startX = (int)(mouseX/CELLSIZE);
                    startY = (int)(mouseY/CELLSIZE);
                }
            }
        }

        if (step == 6){
            double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            if ((int)(mouseX/CELLSIZE) != startX || (int)(mouseY/CELLSIZE) != startY){
                cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(CellColor);
                cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(WallColor);
            }
            if(0 <= x/CELLSIZE && x/CELLSIZE < cells.length && 0 <= y/CELLSIZE && y/CELLSIZE < cells[0].length){
                mouseX = x;
                mouseY = y;
                if (cells[(int)(x/CELLSIZE)][(int)(y/CELLSIZE)].getCellColor() == CellColor){
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setCellColor(FinishColor);
                    cells[(int)(mouseX/CELLSIZE)][(int)(mouseY/CELLSIZE)].setWallColor(FinishColor);
                    finishX = (int)(mouseX/CELLSIZE);
                    finishY = (int)(mouseY/CELLSIZE);
                }
            }
        }

        if (step == 8)
            if (trees.get(0).getCellToWallRatio() == 0){
                trees.add(new TreeCell((TransformingCell) trees.remove(0)));
                step = 9;
            }

        if (step == 9){
            boolean flag = true;
            for (MazeCell[] cll:cells)
                for (MazeCell cell:cll)
                    for (Transition trans:cell.getTransitions())
                        if (trans != null) {
                            flag = false;
                            break;
                        }

            if (flag){
                step = 10;
            }
        }

        if (step == 11)
            best_first_search();

        repaint();
        if (stepF)
            step *= -1;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        drawMaze(g2);
        drawButtons(g2);
    }

    private void drawMaze(Graphics2D g) {
        if (step == 8 || step == 9)
            for (MazeCell[] cll:cells)
                for (MazeCell cell:cll)
                    cell.draw(g);
        for (Cell cell:trees)
            cell.draw(g);
        for (Cell cell:trees)
            cell.resetDrawn();
        if (step == 8)
            ((TransformingCell)trees.get(0)).move();
        if (step == 9)
            ((TreeCell)trees.get(0)).move();
        g.setColor(WallColor);
        g.drawRect(0, 0, cells.length * CELLSIZE, cells[0].length * CELLSIZE);
    }

    private void drawButtons(Graphics2D g){
        Color wordC = WallColor, inC = ButtonColor, unavailableC = CellColor, markedC = SearchedColor;
        int x = B_WIDTH - (B_WIDTH - (cells.length * CELLSIZE)) / 2 - BUTTON_WIDTH / 2, y = 0;
        int spaceX = (int)(0.5 * g.getFont().getSize()), spaceY = BUTTON_HEIGHT - (int)(0.3 * g.getFont().getSize()), arcWidth = 10, arcHeight = 10;
        double mx = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), my = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

        g.setColor(wordC);
        g.setFont(new Font("Ariel", Font.PLAIN, BUTTON_FONT_SIZE));
        Map attributes = g.getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        g.setFont(g.getFont().deriveFont(attributes));
        y += (int)(0.1 * BUTTON_SPACE);
        g.drawString("Maze Types", x + spaceX, y + spaceY);


        g.setFont(new Font("Ariel", Font.PLAIN, BUTTON_FONT_SIZE));
        y += (int)(0.4 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("Prim's", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("Kruskal's", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("DFS", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("Eller's", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("Rec Dev", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);


        g.setFont(new Font("Ariel", Font.PLAIN, BUTTON_FONT_SIZE));
        attributes = g.getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        g.setFont(g.getFont().deriveFont(attributes));
        y += (int)(0.3 * BUTTON_SPACE) + BUTTON_HEIGHT;
        g.drawString("Functions", x + spaceX, y + spaceY);


        g.setFont(new Font("Ariel", Font.PLAIN, BUTTON_FONT_SIZE));
        y += (int)(0.4 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        if (step < 0)
            g.drawString("resume", x + spaceX, y + spaceY);
        else
            g.drawString("stop", x + spaceX, y + spaceY);

        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("step", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("new maze", x + spaceX, y + BUTTON_HEIGHT - (int)(0.2 * g.getFont().getSize()));
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (step != 4)
            g.setColor(unavailableC);
        else if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("reposition start", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (step != 4)
            g.setColor(unavailableC);
        else if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("reposition end", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (step != 4)
            g.setColor(unavailableC);
        else if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("maze to tree", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (step != 10)
            g.setColor(unavailableC);
        else if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT){
            g.setColor(markedC);
        }
        else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("solve maze", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
    }

    private int binSearchH(Double in, ArrayList<Double> arr){
        return binSearch(arr, in,0, arr.size() - 1);
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
        double value = binHelp.remove(0);
        if (searching.getArrX() == finishX && searching.getArrY() == finishY) {
            step = 10;
            return;
        }
        searching.colorBackwards(SearchedColor, SearchColor);
        int index = binSearchH(value, binHelp1);
        binHelp1.add(index, value);
        searchedCells.add(index, searching);
        for (Transition transition : searching.getTransitions())
            if (transition != null)
                if (!treeCellExistsIn((TreeCell)transition.getCells()[1], searchedCells, binHelp1)){
                    value = evaluate((TreeCell)transition.getCells()[1]);
                    index = binSearchH(value, binHelp);
                    binHelp.add(index, value);
                    searchCells.add(index, (TreeCell)transition.getCells()[1]);
                }
        searchCells.get(0).colorBackwards(SearchColor, WallColor);
        searchCells.get(0).colorBackwards(SearchColor, SearchedColor);
    }

    public boolean treeCellExistsIn(TreeCell query, ArrayList<TreeCell> list, ArrayList<Double> arrL){
        double value = evaluate(query);
        int index = binSearchH(value, arrL);
        if (index >= arrL.size())
            return false;
        for (;index > 0 && arrL.get(index - 1) == value; index--);
        for (;arrL.get(index) == value; index++)
            if (list.get(index).equals(query))
                return true;
        return false;
    }

    public double evaluate(TreeCell query){
        int x = query.getArrX() - finishX;
        int y = query.getArrY() - finishY;
        return x*x + y*y;
    }

    public void resetCells(){
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[i].length; j++){
                cells[i][j] = new MazeCell(i, j, CELLSIZE, CellToWallRatio, WallColor, CellColor);
                cells[i][j].setDrawn(true);
            }
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

    public void addPaths(MazeCell cell, ArrayList<MTMTransition> paths, ArrayList<Double> list){
        MTMTransition path;
        int x = cell.getArrX(), y = cell.getArrY();
        int i;
        if (x > 0){
            path = new MTMTransition(cells[x][y], cells[x - 1][y], CellColor, CELLSIZE, CellToWallRatio);
            i = binSearchH(path.getWeight(), list);
            list.add(i, path.getWeight());
            paths.add(i, path);
        }
        if (y > 0){
            path = new MTMTransition(cells[x][y], cells[x][y - 1], CellColor, CELLSIZE, CellToWallRatio);
            i = binSearchH(path.getWeight(), list);
            list.add(i, path.getWeight());
            paths.add(i, path);
        }
        if (x + 1 < cells.length){
            path = new MTMTransition(cells[x][y], cells[x + 1][y], CellColor, CELLSIZE, CellToWallRatio);
            i = binSearchH(path.getWeight(), list);
            list.add(i, path.getWeight());
            paths.add(i, path);
        }
        if (y + 1 < cells[0].length){
            path = new MTMTransition(cells[x][y], cells[x][y + 1], CellColor, CELLSIZE, CellToWallRatio);
            i = binSearchH(path.getWeight(), list);
            list.add(i, path.getWeight());
            paths.add(i, path);
        };
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
        int xs = B_WIDTH - (B_WIDTH - (cells.length * CELLSIZE)) / 2 - BUTTON_WIDTH / 2, ys = 0;
        if (x >= xs && x <= xs + BUTTON_WIDTH){
            for (int i = 0; i < 5; i++) {
                ys += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
                if (y >= ys && y <= ys + BUTTON_HEIGHT){
                    if (step < 0)
                        step = -1;
                    else
                        step = 1;
                    creationFlag = i;
                }
            }

            ys += (int)(0.7 * BUTTON_SPACE) + 2 * BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT)
                step *= -1;

            ys += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step < 0 && step > -1000)
                step -= 1000;

            ys += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT){
                if (step < 0)
                    step = -1;
                else
                    step = 1;
            }

            ys += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step == 4){
                cells[startX][startY].setCellColor(CellColor);
                cells[startX][startY].setWallColor(WallColor);
                step = 5;
                return;
            }

            ys += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step == 4){
                cells[finishX][finishY].setCellColor(CellColor);
                cells[finishX][finishY].setWallColor(WallColor);
                step = 6;
                return;
            }

            ys += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step == 4){
                trees.remove(0);
                trees.add(new TransformingCell(cells[startX][startY], MOVINGRATE, WALLRATE, 0, 0, GOALSIZE, M_HEIGHT, M_WIDTH, treeSize));
                treeSize.add(0, 1);
                for (int i = 0; i < 15; i++)
                    treeSize.add(treeSize.size(), 0);
                step = 8;
            }

            ys += (int)(0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step == 10){
                step = 11;
                binHelp.clear();
                binHelp1.clear();
                searchCells.clear();
                searchedCells.clear();
                binHelp.add(evaluate((TreeCell) trees.get(0)));
                searchCells.add((TreeCell) trees.get(0));
                trees.get(0).resetSearched();
                ((TreeCell) trees.get(0)).resetColor(WallColor);
            }
        }
        else{
            if (step == 5)
                if(0 <= x/CELLSIZE && x/CELLSIZE <= cells.length && 0 <= y/CELLSIZE && y/CELLSIZE <= cells[0].length)
                    if (cells[(int)(x/CELLSIZE)][(int)(y/CELLSIZE)].getCellColor() != FinishColor){
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
                }
            if (step == 6)
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
                        step = 4;
                    }
                }
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

    public void primStep(){
        if (paths.isEmpty()){
            step = 3;
            return;
        }
        int i = 0;
        MTMTransition path = paths.remove(i);
        binHelp.remove(i);
        while (intCells[path.getCells()[0].getArrX()][path.getCells()[0].getArrY()] == -1 && intCells[path.getCells()[1].getArrX()][path.getCells()[1].getArrY()] == -1){
            path = paths.remove(i);
            binHelp.remove(i);
            if (paths.isEmpty()){
                step = 3;
                startY = 0;
                startX = 0;
                return;
            }
        }
        intCells[path.getCells()[0].getArrX()][path.getCells()[0].getArrY()] = -1;
        intCells[path.getCells()[1].getArrX()][path.getCells()[1].getArrY()] = -1;
        path.getCells()[0].setTrans(path);
        path.getCells()[1].setTrans(path);
        addPaths(path.getCells()[1], paths, binHelp);
        addPaths(path.getCells()[0], paths, binHelp);
    }

    private void kruskalStep(){
        if (paths.isEmpty()){
            step = 3;
            return;
        }
        int i = 0;
        paths.get(i).getCells()[1].resetSearched();
        boolean flag = paths.get(i).getCells()[0].dfsShell(paths.get(i).getCells()[1]);
        while (flag){
            paths.remove(i);
            if (paths.isEmpty()){
                step = 3;
                return;
            }
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
                step = 3;
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
        if (startY == cells[0].length){
            startY = 0;
            step = 3;
        }
        for (Cell[] clls:cells)
            for (Cell cell:clls)
                cell.resetDrawn();
    }

    public void recursiveDivisionStep(){
        System.out.println(2);
    }

    public void wilsonStep(){
        int temp = intCells[startX][startY];
        MazeCell cell = cells[startX][startY];
        ArrayList<Integer> arr = new ArrayList<>();
        if (startX > 0)
            arr.add(startX - 1 + startY * cells.length);
        if (startY > 0)
            arr.add(startX + (startY - 1) * cells.length);
        if (startX + 1 < cells.length)
            arr.add(startX + 1 + startY * cells.length);
        if (startY + 1 < cells[startX].length)
            arr.add(startX + (startY + 1) * cells.length);
        if (arr.size() == 0){
            if (temp == -1)
                step = 3;
            else{
                cell.setSearched(false);
                intCells[startX][startY] = 0;
                startX = temp%cells.length;
                startY = temp/cells.length;

            }
            return;
        }
        temp = arr.get(rnd.nextInt(arr.size()));
        startX = temp%cells.length;
        startY = temp/cells.length;
        if (intCells[startX][startY] == 0){
            intCells[startX][startY] = cell.getArrX() + cell.getArrY() * cells.length;
            Transition transition = new MTMTransition(cell, cells[startX][startY], CellColor,  CELLSIZE, CellToWallRatio);
            cell.setTrans(transition);
            cells[startX][startY].setTrans(transition);
            return;
        }
        if (intCells[startX][startY] == -1){
            Transition transition = new MTMTransition(cell, cells[startX][startY], CellColor,  CELLSIZE, CellToWallRatio);
            cell.setTrans(transition);
            cells[startX][startY].setTrans(transition);
            int x = cell.getArrX(), y = cell.getArrY();
            while (intCells[x][y] != -1){
                cell = cells[x][y];
                intCells[x][y] = -1;
                x = cell.getArrX();
                y = cell.getArrY();
            }
        }
        else{
            int x = cell.getArrX(), y = cell.getArrY();
            while (intCells[x][y] != -1){
                cell.delTrans((MTMTransition)cell.getTransitions()[0]);
                cell = cells[x][y];
                intCells[x][y] = 0;
                x = cell.getArrX();
                y = cell.getArrY();
            }
        }
//        for (int i = 0; i < cells.length; i++)
//            for (int j = 0; j < cells[i].length; j++)

    }
}