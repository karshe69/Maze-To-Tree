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

    private final int TOPBAR = 30; // size of the top bar of the actual window. used for aesthetic reasons.
    private final int START_WIDTH = 600; // width of the start button
    private final int START_HEIGHT = 300; // height of the start button
    private final int START_FONT_SIZE = 100; // font of the start button
    private final int BUTTON_WIDTH = 200; // width of all of the other buttons
    private final int BUTTON_HEIGHT = 50; // height of all the other buttons
    private final int BUTTON_SPACE = 50; // space between the buttons and the maze
    private final int BUTTON_FONT_SIZE = 30; // font size of the buttons


    private final int M_WIDTH = B_WIDTH - BUTTON_WIDTH - 2 * BUTTON_SPACE; // width of the actual maze part of the screen
    private final int M_HEIGHT = B_HEIGHT - TOPBAR; // height of the actual maze part of the screen

    private final int DELAY = 20; // tick delay for non special cases
    private final int DELAYE = 300; // tick delay while creating an Eller's maze
    private final int DELAYW = 1; // tick delay while creating an Wilson's maze

    private final double CellToWallRatio = 0.9; // the ratio between the maze cell and its wall
    private final int CELLSIZE = 50; // the size of the maze cells
    private final int MOVINGRATE = 15; // amount of pixels that move each tick while creating the tree
    private final int NODESIZE = 10; // tree node size
    private final double WALLRATE = 0.05; // the amount of CellToWallRatio decreas in transforming cells each tick

    private int creationFlag = -1; //type of maze: 0 = prim's, 1 = kruskal's, 2 = random dfs, 3 = eller's, willson's
    private int step = 0; // step of progression in the programer. 0 = start button, 1 = initialization , 2 = creating the maze, 3 & 4 = maze is done, 5 = change the position of the start, 6 = change the position of the end, 7 & 8 = turn maze to tree, 9 = tree is done, 10 = solve maze.

    private final double ellersChance = 0.5; // chance for a connection to be made in an eller's maze


    //colors for the app aesthetic
    private final Color CellColor = new Color(34, 53, 64);
    private final Color WallColor = new Color(169, 184, 201);
    private final Color StartColor = new Color(0, 184, 140);
    private final Color FinishColor = new Color(187, 80, 85);
    private final Color SearchedColor = new Color(79, 124, 165);
    private final Color SearchColor = new Color(116, 174, 144);
    private final Color ButtonColor = new Color(44, 70, 89);

    // current x and y of the mouse
    private double mouseX = 0;
    private double mouseY = 0;


    private int startX; // also used in dfs for remembering which cell im currently on.
    private int startY; // also used in dfs for remembering which cell im currently on and for eller's to remember which row im currently on.
    private int finishX;
    private int finishY;

    private MazeCell[][] cells = new MazeCell[M_WIDTH / CELLSIZE][M_HEIGHT / CELLSIZE]; // the cells of the maze

    private Timer timer; // the program updates itself using this timer every.

    // all of the below are used for maze creating / solving.

    private ArrayList<Cell> trees = new ArrayList<>();

    // for utility:
    private int[][] intCells = new int[cells.length][cells[0].length];

    private ArrayList<Integer> treeSize = new ArrayList<>();

    private ArrayList<MTMTransition> paths = new ArrayList<>();

    private ArrayList<Double> binHelp = new ArrayList<>(); // for anything related to binary searching
    private ArrayList<Double> binHelp1 = new ArrayList<>(); // for anything related to binary searching


    private ArrayList<TreeCell> searchCells = new ArrayList<>();
    private ArrayList<TreeCell> searchedCells = new ArrayList<>();

    Random rnd = new Random();

    public Board() { // creates the jpanel and the timer
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT)); // sets screen size
        setBackground(CellColor);
        addMouseListener(this);
        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) { // every tick this function is called
        boolean stepF = false;
        if (step < -1000) { // if the program is on stop mode and it was pressed on step.
            stepF = true;
            step += 1000;
            step *= -1;
        }

        // explanation for what happends each step is in the step description and in the functions which are used.
        if (step == 1 || step == -1) {
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
            timer.setDelay(DELAY);
            if (creationFlag == 0) {
                startX = rnd.nextInt(cells.length);
                startY = rnd.nextInt(cells[startX].length);
                for (int[] clls : intCells)
                    Arrays.fill(clls, 0);
                intCells[startX][startY] = -1;
                trees.add(cells[startX][startY]);
                addPaths(cells[startX][startY], paths, binHelp);
            }
            if (creationFlag == 1)
                resetPaths();
            if (creationFlag == 2) {
                startX = rnd.nextInt(cells.length);
                startY = rnd.nextInt(cells[startX].length);
                intCells[startX][startY] = -1;
                trees.add(cells[startX][startY]);
            }
            if (creationFlag == 3) {
                for (int i = 0; i < cells.length; i++)
                    for (int j = 0; j < cells[i].length; j++)
                        if (intCells[i][j] == 0)
                            intCells[i][j] = i + j * cells.length + 1;
                timer.setDelay(DELAYE);
            }
            if (creationFlag == 4) {
                for (int[] clls : intCells)
                    Arrays.fill(clls, 0);
                trees.add(0, cells[0][0]);
                intCells[0][0] = -1;
                timer.setDelay(DELAYW);
            }
            if (step == 1)
                step = 2;
        }
        if (step == 2) {
            if (creationFlag == 0)
                primStep();
            if (creationFlag == 1)
                kruskalStep();
            if (creationFlag == 2)
                dfsStep();
            if (creationFlag == 3)
                ellerStep();
            if (creationFlag == 4)
                wilsonStep();
        }

        if (step == 3) {
            while (trees.size() > 1)
                trees.remove(0);
            timer.setDelay(DELAY);
            cells[startX][startY].setCellColor(StartColor);
            cells[startX][startY].setWallColor(StartColor);
            cells[finishX][finishY].setCellColor(FinishColor);
            cells[finishX][finishY].setWallColor(FinishColor);
            step = 4;
        }

        if (step == 5) {
            double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            if ((int) (mouseX / CELLSIZE) != finishX || (int) (mouseY / CELLSIZE) != finishY) {
                cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setCellColor(CellColor);
                cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setWallColor(WallColor);
            }
            if (0 <= x / CELLSIZE && x / CELLSIZE < cells.length && 0 <= y / CELLSIZE && y / CELLSIZE < cells[0].length) {
                mouseX = x;
                mouseY = y;
                if (cells[(int) (x / CELLSIZE)][(int) (y / CELLSIZE)].getCellColor() == CellColor) {
                    cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setCellColor(StartColor);
                    cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setWallColor(StartColor);
                    startX = (int) (mouseX / CELLSIZE);
                    startY = (int) (mouseY / CELLSIZE);
                }
            }
        }

        if (step == 6) {
            double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            if ((int) (mouseX / CELLSIZE) != startX || (int) (mouseY / CELLSIZE) != startY) {
                cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setCellColor(CellColor);
                cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setWallColor(WallColor);
            }
            if (0 <= x / CELLSIZE && x / CELLSIZE < cells.length && 0 <= y / CELLSIZE && y / CELLSIZE < cells[0].length) {
                mouseX = x;
                mouseY = y;
                if (cells[(int) (x / CELLSIZE)][(int) (y / CELLSIZE)].getCellColor() == CellColor) {
                    cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setCellColor(FinishColor);
                    cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setWallColor(FinishColor);
                    finishX = (int) (mouseX / CELLSIZE);
                    finishY = (int) (mouseY / CELLSIZE);
                }
            }
        }

        if (step == 7)
            if (trees.get(0).getCellToWallRatio() == 0) {
                trees.add(new TreeCell((TransformingCell) trees.remove(0)));
                step = 8;
            }

        if (step == 8) {
            boolean flag = true;
            for (MazeCell[] cll : cells)
                for (MazeCell cell : cll)
                    for (Transition trans : cell.getTransitions())
                        if (trans != null) {
                            flag = false;
                            break;
                        }

            if (flag) {
                step = 9;
            }
        }

        if (step == 10)
            best_first_search();

        repaint();
        if (stepF)
            step *= -1;
    }

    @Override
    public void paintComponent(Graphics g) { // calls for the drawing functions to put stuff on screen.
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (step == 0)
            drawStart(g2);
        else{
            drawMaze(g2);
            menuButtons(g2);
        }
    }

    private void drawStart(Graphics2D g){ // draws the start button
        Color wordC = WallColor, inC = ButtonColor, markedC = SearchedColor;
        double mx = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), my = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
        int y = B_HEIGHT / 2 - START_HEIGHT / 2;
        int x = B_WIDTH / 2 - START_WIDTH / 2;
        int arcWidth = 50;
        int arcHeight = 50;
        g.setFont(new Font("Ariel", Font.PLAIN, START_FONT_SIZE));
        int spaceX = (int) (0.5 * g.getFont().getSize());
        int spaceY = START_HEIGHT / 2 + g.getFont().getSize() / 2;
        if (mx >= x && mx <= x + START_WIDTH && my >= y && my <= y + START_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, START_WIDTH, START_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("   START", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, START_WIDTH, START_HEIGHT, arcWidth, arcHeight);
    }

    private void drawMaze(Graphics2D g) { // draws the maze
        if (step == 7 || step == 8 || step == -7 || step == -8)
            for (MazeCell[] cll : cells)
                for (MazeCell cell : cll)
                    cell.draw(g);
        for (Cell cell : trees)
            cell.draw(g);
        for (Cell cell : trees)
            cell.resetDrawn();
        if (step == 7 || step == -7)
            ((TransformingCell) trees.get(0)).move();
        if (step == 8 || step == -8)
            ((TreeCell) trees.get(0)).move();
        g.setColor(WallColor);
        g.drawRect(0, 0, cells.length * CELLSIZE, cells[0].length * CELLSIZE);
    }

    private void menuButtons(Graphics2D g) { //draws the menu
        Color wordC = WallColor, inC = ButtonColor, unavailableC = CellColor, markedC = SearchedColor;
        int x = B_WIDTH - (B_WIDTH - (cells.length * CELLSIZE)) / 2 - BUTTON_WIDTH / 2, y = 0;
        int spaceX = (int) (0.5 * g.getFont().getSize()), spaceY = BUTTON_HEIGHT - (int) (0.3 * g.getFont().getSize()), arcWidth = 10, arcHeight = 10;
        double mx = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX(), my = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

        g.setColor(wordC);
        g.setFont(new Font("Ariel", Font.PLAIN, BUTTON_FONT_SIZE));
        Map attributes = g.getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        g.setFont(g.getFont().deriveFont(attributes));
        y += (int) (0.1 * BUTTON_SPACE);
        g.drawString("Maze Types", x + spaceX, y + spaceY);


        g.setFont(new Font("Ariel", Font.PLAIN, BUTTON_FONT_SIZE));
        y += (int) (0.4 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("Prim's", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("Kruskal's", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("DFS", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("Eller's", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("Wilson's", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);


        g.setFont(new Font("Ariel", Font.PLAIN, BUTTON_FONT_SIZE));
        attributes = g.getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        g.setFont(g.getFont().deriveFont(attributes));
        y += (int) (0.3 * BUTTON_SPACE) + BUTTON_HEIGHT;
        g.drawString("Functions", x + spaceX, y + spaceY);


        g.setFont(new Font("Ariel", Font.PLAIN, BUTTON_FONT_SIZE));
        y += (int) (0.4 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        if (step < 0)
            g.drawString("resume", x + spaceX, y + spaceY);
        else
            g.drawString("stop", x + spaceX, y + spaceY);

        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("step", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("new maze", x + spaceX, y + BUTTON_HEIGHT - (int) (0.2 * g.getFont().getSize()));
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (step != 4)
            g.setColor(unavailableC);
        else if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("reposition start", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (step != 4)
            g.setColor(unavailableC);
        else if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("reposition end", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (step != 4)
            g.setColor(unavailableC);
        else if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("maze to tree", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);

        y += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
        if (step != 9)
            g.setColor(unavailableC);
        else if (mx >= x && mx <= x + BUTTON_WIDTH && my >= y && my <= y + BUTTON_HEIGHT) {
            g.setColor(markedC);
        } else
            g.setColor(inC);
        g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
        g.setColor(wordC);
        g.drawString("solve maze", x + spaceX, y + spaceY);
        g.drawRoundRect(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, arcWidth, arcHeight);
    }

    private int binSearch(Double in, ArrayList<Double> list) { // binary search
        return binSearchH(list, in, 0, list.size() - 1);
    }

    private int binSearchH(ArrayList<Double> list, Double input, int start, int finish) { // helps with binary search
        if (start > finish)
            return start;
        if (start == finish) {
            if (input < list.get(start))
                return start;
            return start + 1;
        }
        if (start + 1 == finish) {
            if (input < list.get(start))
                return start;
            if (input > list.get(finish))
                return finish + 1;
            return finish;
        }
        int i = (start + finish) / 2;
        if (input > list.get(i))
            return binSearchH(list, input, i, finish);
        if (input < list.get(i))
            return binSearchH(list, input, start, i);
        return i;
    }

    public void best_first_search() { // Best First Search over the tree
        TreeCell searching = searchCells.remove(0);
        double value = binHelp.remove(0);
        if (searching.getArrX() == finishX && searching.getArrY() == finishY) {
            step = 9;
            return;
        }
        searching.colorBackwards(SearchedColor, SearchColor);
        int index = binSearch(value, binHelp1);
        binHelp1.add(index, value);
        searchedCells.add(index, searching);
        for (Transition transition : searching.getTransitions())
            if (transition != null)
                if (!treeCellExistsIn((TreeCell) transition.getCells()[1], searchedCells, binHelp1)) {
                    value = evaluate((TreeCell) transition.getCells()[1]);
                    index = binSearch(value, binHelp);
                    binHelp.add(index, value);
                    searchCells.add(index, (TreeCell) transition.getCells()[1]);
                }
        searchCells.get(0).colorBackwards(SearchColor, WallColor);
        searchCells.get(0).colorBackwards(SearchColor, SearchedColor);
    }

    public boolean treeCellExistsIn(TreeCell query, ArrayList<TreeCell> list, ArrayList<Double> arrL) { //finds if a tree cell exsits in list, with arrL as the thing that lets bin search the list
        double value = evaluate(query);
        int index = binSearch(value, arrL);
        if (index >= arrL.size())
            return false;
        for (; index > 0 && arrL.get(index - 1) == value; index--) ;
        for (; arrL.get(index) == value; index++)
            if (list.get(index).equals(query))
                return true;
        return false;
    }

    public double evaluate(TreeCell query) {
        int x = query.getArrX() - finishX;
        int y = query.getArrY() - finishY;
        return x * x + y * y;
    } // evaluation function for the query

    public void resetCells() {
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new MazeCell(i, j, CELLSIZE, CellToWallRatio, WallColor, CellColor);
                cells[i][j].setDrawn(true);
            }
    } // resets the cells of the maze

    public void resetPaths() {
        MTMTransition trn;
        int ind;
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j < cells[i].length; j++)
                for (int k = 0; k <= 1; k++)
                    if (i + k < cells.length && j + (1 - k) < cells[i].length) {
                        trn = new MTMTransition(cells[i][j], cells[i + k][j + (1 - k)], CellColor, CELLSIZE, CellToWallRatio);
                        ind = binSearch(trn.getWeight(), binHelp);
                        paths.add(ind, trn);
                        binHelp.add(ind, trn.getWeight());
                    }
    } // makes a full list of paths filled with all possible paths in the maze

    public void ellersHelper(int a, int b) { // changes all the variables with the value of a to b in intCells
        for (int i = 0; i < cells.length; i++)
            for (int j = 0; j <= startY; j++)
                if (intCells[i][j] == a)
                    intCells[i][j] = b;
    }

    public void addPaths(MazeCell cell, ArrayList<MTMTransition> paths, ArrayList<Double> list) {
        MTMTransition path;
        int x = cell.getArrX(), y = cell.getArrY();
        int i;
        if (x > 0) {
            path = new MTMTransition(cells[x][y], cells[x - 1][y], CellColor, CELLSIZE, CellToWallRatio);
            i = binSearch(path.getWeight(), list);
            list.add(i, path.getWeight());
            paths.add(i, path);
        }
        if (y > 0) {
            path = new MTMTransition(cells[x][y], cells[x][y - 1], CellColor, CELLSIZE, CellToWallRatio);
            i = binSearch(path.getWeight(), list);
            list.add(i, path.getWeight());
            paths.add(i, path);
        }
        if (x + 1 < cells.length) {
            path = new MTMTransition(cells[x][y], cells[x + 1][y], CellColor, CELLSIZE, CellToWallRatio);
            i = binSearch(path.getWeight(), list);
            list.add(i, path.getWeight());
            paths.add(i, path);
        }
        if (y + 1 < cells[0].length) {
            path = new MTMTransition(cells[x][y], cells[x][y + 1], CellColor, CELLSIZE, CellToWallRatio);
            i = binSearch(path.getWeight(), list);
            list.add(i, path.getWeight());
            paths.add(i, path);
        }
    } // adds the paths between cell and his surronding into the global variable paths

    @Override
    public void mouseClicked(MouseEvent e) { // checks all of the button clicks
        double x = e.getX(), y = e.getY();
        if (step == 0){ // start button
            if (B_HEIGHT / 2 - START_HEIGHT / 2 <= y && B_HEIGHT / 2 + START_HEIGHT / 2 >= y && x >= B_WIDTH / 2 - START_WIDTH / 2 && x <= B_WIDTH / 2 + START_WIDTH / 2)
                step = 1;
            return;
        }

        // menu
        int xs = B_WIDTH - (B_WIDTH - (cells.length * CELLSIZE)) / 2 - BUTTON_WIDTH / 2, ys = 0;
        if (x >= xs && x <= xs + BUTTON_WIDTH) {
            for (int i = 0; i < 5; i++) {
                ys += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
                if (y >= ys && y <= ys + BUTTON_HEIGHT) {
                    if (step < 0)
                        step = -1;
                    else
                        step = 1;
                    creationFlag = i;
                }
            }

            ys += (int) (0.7 * BUTTON_SPACE) + 2 * BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT)
                step *= -1;

            ys += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step < 0 && step > -1000)
                step -= 1000;

            ys += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT) {
                if (step < 0)
                    step = -1;
                else
                    step = 1;
            }

            ys += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step == 4) {
                cells[startX][startY].setCellColor(CellColor);
                cells[startX][startY].setWallColor(WallColor);
                step = 5;
                return;
            }

            ys += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step == 4) {
                cells[finishX][finishY].setCellColor(CellColor);
                cells[finishX][finishY].setWallColor(WallColor);
                step = 6;
                return;
            }

            ys += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step == 4) {
                trees.remove(0);
                trees.add(new TransformingCell(cells[startX][startY], MOVINGRATE, WALLRATE, 0, 0, NODESIZE, M_HEIGHT, M_WIDTH, treeSize));
                treeSize.add(0, 1);
                for (int i = 0; i < 15; i++)
                    treeSize.add(treeSize.size(), 0);
                step = 7;
            }

            ys += (int) (0.5 * BUTTON_SPACE) + BUTTON_HEIGHT;
            if (y >= ys && y <= ys + BUTTON_HEIGHT && step == 9) {
                step = 10;
                binHelp.clear();
                binHelp1.clear();
                searchCells.clear();
                searchedCells.clear();
                binHelp.add(evaluate((TreeCell) trees.get(0)));
                searchCells.add((TreeCell) trees.get(0));
                trees.get(0).resetSearched();
                ((TreeCell) trees.get(0)).resetColor(WallColor);
            }
        } else {
            if (step == 5)
                if (0 <= x / CELLSIZE && x / CELLSIZE <= cells.length && 0 <= y / CELLSIZE && y / CELLSIZE <= cells[0].length)
                    if (cells[(int) (x / CELLSIZE)][(int) (y / CELLSIZE)].getCellColor() != FinishColor) {
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
                if (0 <= x / CELLSIZE && x / CELLSIZE <= cells.length && 0 <= y / CELLSIZE && y / CELLSIZE <= cells[0].length) {
                    if (cells[(int) (x / CELLSIZE)][(int) (y / CELLSIZE)].getCellColor() != StartColor) {
                        cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setCellColor(CellColor);
                        cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setWallColor(WallColor);
                        mouseX = x;
                        mouseY = y;
                        cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setCellColor(FinishColor);
                        cells[(int) (mouseX / CELLSIZE)][(int) (mouseY / CELLSIZE)].setWallColor(FinishColor);
                        finishX = (int) (mouseX / CELLSIZE);
                        finishY = (int) (mouseY / CELLSIZE);
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

    // maze creating algorithms: all of this algorithms will do one step in the specified algorithm each time they are called.

    public void primStep() {
        if (paths.isEmpty()) {
            step = 3;
            return;
        }
        int i = 0;
        MTMTransition path = paths.remove(i);
        binHelp.remove(i);
        while (intCells[path.getCells()[0].getArrX()][path.getCells()[0].getArrY()] == -1 && intCells[path.getCells()[1].getArrX()][path.getCells()[1].getArrY()] == -1) {
            path = paths.remove(i);
            binHelp.remove(i);
            if (paths.isEmpty()) {
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

    private void kruskalStep() {
        if (paths.isEmpty()) {
            step = 3;
            return;
        }
        int i = 0;
        paths.get(i).getCells()[1].resetSearched();
        boolean flag = paths.get(i).getCells()[0].dfsShell(paths.get(i).getCells()[1]);
        while (flag) {
            paths.remove(i);
            if (paths.isEmpty()) {
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

    private void dfsStep() {
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
        if (arr.size() == 0) {
            if (temp == -1) {
                startX = 0;
                startY = 0;
                step = 3;
            } else {
                startX = temp % cells.length;
                startY = temp / cells.length;
            }
            return;
        }
        temp = arr.get(rnd.nextInt(arr.size()));
        startX = temp % cells.length;
        startY = temp / cells.length;
        intCells[startX][startY] = cell.getArrX() + cell.getArrY() * cells.length;
        Transition transition = new MTMTransition(cell, cells[startX][startY], CellColor, CELLSIZE, CellToWallRatio);
        cell.setTrans(transition);
        cells[startX][startY].setTrans(transition);
    }

    public void ellerStep() {
        double chance = ellersChance;
        if (startY == cells[0].length - 1) {
            chance = 1;
        }

        if (!(startY == 0)) {
            int[] arr = new int[cells.length * startY];
            for (int i = 0; i < cells.length; i++)
                arr[i] = 0;
            for (int i = 0; i < cells.length; i++) {
                arr[intCells[i][startY - 1] - 1]++;
            }

            ArrayList<Boolean> arr1 = new ArrayList<>();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] != 0) {
                    arr1.add(true);
                    for (int j = 0; j < arr[i] - 1; j++)
                        if (rnd.nextDouble() <= chance)
                            arr1.add(true);
                        else
                            arr1.add(false);
                }

                for (int j = 0; j < cells.length; j++)
                    if (i + 1 == intCells[j][startY - 1])
                        if (arr1.remove(rnd.nextInt(arr1.size())) && intCells[j][startY - 1] != intCells[j][startY]) {
                            Transition transition = new MTMTransition(cells[j][startY], cells[j][startY - 1], CellColor, CELLSIZE, CellToWallRatio);
                            cells[j][startY - 1].setTrans(transition);
                            cells[j][startY].setTrans(transition);
                            intCells[j][startY] = intCells[j][startY - 1];
                        }
            }
        }

        for (int i = 0; i < cells.length - 1; i++)
            if (rnd.nextDouble() <= chance && intCells[i][startY] != intCells[i + 1][startY]) {
                Transition transition = new MTMTransition(cells[i][startY], cells[i + 1][startY], CellColor, CELLSIZE, CellToWallRatio);
                cells[i][startY].setTrans(transition);
                cells[i + 1][startY].setTrans(transition);
                ellersHelper(intCells[i + 1][startY], intCells[i][startY]);
            }

        for (int i = 0; i < cells.length; i++)
            if (intCells[i][startY] > startY * cells.length)
                trees.add(cells[i][startY]);
        startY += 1;
        if (startY == cells[0].length) {
            startY = 0;
            step = 3;
        }
        for (Cell[] clls : cells)
            for (Cell cell : clls)
                cell.resetDrawn();
    }

    public void wilsonStep() {
        if (intCells[startX][startY] == -1) {
            boolean flag = true;
            int starterY, starterX;
            for (starterY = 0; starterY < intCells[0].length && flag; starterY++) {
                for (starterX = 0; starterX < intCells.length; starterX++)
                    if (intCells[starterX][starterY] == 0) {
                        flag = false;
                        if (starterX == 0)
                            intCells[starterX][starterY] = starterX + 1 + (starterY - 1) * intCells.length;
                        else
                            intCells[starterX][starterY] = starterX + starterY * intCells.length;
                        trees.add(1, cells[starterX][starterY]);
                        cells[starterX][starterY].setWallColor(SearchColor);
                        startX = starterX;
                        startY = starterY;
                        break;
                    }
            }
            if (intCells[startX][startY] == -1) {
                startX = 0;
                startY = 0;
                step = 3;
            }
            return;
        }

        int temp = intCells[startX][startY] - 1;
        MazeCell cell = cells[startX][startY];
        ArrayList<Integer> arr = new ArrayList<>();
        if (startX > 0 && temp % cells.length != startX - 1)
            arr.add(startX - 1 + startY * cells.length);
        if (startY > 0 && temp / cells.length != startY - 1)
            arr.add(startX + (startY - 1) * cells.length);
        if (startX + 1 < cells.length && temp % cells.length != startX + 1)
            arr.add(startX + 1 + startY * cells.length);
        if (startY + 1 < cells[startX].length && temp / cells.length != startY + 1)
            arr.add(startX + (startY + 1) * cells.length);
        temp = arr.get(rnd.nextInt(arr.size()));
        startX = temp % cells.length;
        startY = temp / cells.length;
        if (intCells[startX][startY] == 0) {
            intCells[startX][startY] = cell.getArrX() + cell.getArrY() * cells.length + 1;
            cells[startX][startY].setWallColor(SearchColor);
            Transition transition = new MTMTransition(cell, cells[startX][startY], CellColor, CELLSIZE, CellToWallRatio);
            cell.setTrans(transition);
            cells[startX][startY].setTrans(transition);
            return;
        }
        if (intCells[startX][startY] == -1) {
            Transition transition = new MTMTransition(cell, cells[startX][startY], CellColor, CELLSIZE, CellToWallRatio);
            cell.setTrans(transition);
            cells[startX][startY].setTrans(transition);
            int x = cell.getArrX(), y = cell.getArrY();
            while (intCells[x][y] != -1) {
                cell.setWallColor(WallColor);
                cell = cells[(intCells[x][y] - 1) % cells.length][(intCells[x][y] - 1) / cells.length];
                intCells[x][y] = -1;
                x = cell.getArrX();
                y = cell.getArrY();
            }
            startX = x;
            startY = y;
        } else {
            int x = cell.getArrX(), y = cell.getArrY();
            while (x != startX || y != startY) {
                cell.setWallColor(WallColor);
                if (cell.getTransitions()[0] != null)
                    cell.getTransitions()[0].delself();
                cell = cells[(intCells[x][y] - 1) % cells.length][(intCells[x][y] - 1) / cells.length];
                intCells[x][y] = 0;
                x = cell.getArrX();
                y = cell.getArrY();
            }
            startX = x;
            startY = y;
        }
    }
}
