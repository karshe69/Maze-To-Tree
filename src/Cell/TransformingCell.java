package Cell;

import Transition.Transition;
import Transition.TTMTransition;
import Transition.TTTreeTransition;

import java.awt.*;
import java.util.ArrayList;

public class TransformingCell extends Cell {
    private int movingRate;
    private double wallRate;
    private double sizeRate;
    private int GoalSize;

    private int index;
    private int row;
    private int b_height;
    private int b_width;
    private int CellSize;
    private double CellToWallRatio;

    private TTTreeTransition prev = null;
    private ArrayList<Integer> treeSize;

    public TransformingCell(MazeCell cell, int movingRate, double wallRate, int index, int row, int goalSize, int height, int width, ArrayList<Integer> treeSize) {
        super(cell.getX(), cell.getY(), cell.getArrX(), cell.getArrY(), cell.getWallColor(), cell.CellColor);
        this.movingRate = movingRate;
        this.wallRate = wallRate;
        this.index = index;
        this.row = row;
        CellSize = cell.getCellSize();
        x = x + CellSize / 2;
        y = y + CellSize / 2;
        GoalSize = goalSize;
        CellToWallRatio = cell.getCellToWallRatio();
        sizeRate = (CellSize - goalSize) / (CellToWallRatio / wallRate);
        this.treeSize = treeSize;
        b_height = height;
        b_width = width;
        for (Transition transition : cell.getTransitions()) {
            if (transition != null){
                if (transition.getCells()[0].equals(cell))
                    setTrans(new TTMTransition(this, (MazeCell)transition.getCells()[1]));
                if (transition.getCells()[1].equals(cell))
                    setTrans(new TTMTransition(this, (MazeCell)transition.getCells()[0]));
                transition.delself();
            }
        }
    }

    public void draw(Graphics2D g) {
        if(isDrawn())
            return;
        drawn = true;
                for (Transition transition : transitions)
            if (transition != null)
                transition.draw(g);
        g.setColor(WallColor);
        g.fillRect(x - CellSize / 2, y - CellSize / 2, CellSize, CellSize);
        g.setColor(CellColor);
        g.fillRect(x - (int)(CellSize*CellToWallRatio) / 2, y - (int)(CellSize*CellToWallRatio) / 2, (int)(CellSize*CellToWallRatio), (int)(CellSize*CellToWallRatio));
    }

    public void move(){
        int xs = (int)((row + 0.5) * b_width / treeSize.size()), ys = (int)((index + 0.5) * b_height / treeSize.get(row));
        double alpha = Math.atan2(ys - y, xs - x);
        if(xs + movingRate > x && xs - movingRate < x)
            x = xs;
        else{
            x += movingRate * Math.cos(alpha);
        }
        if (ys + movingRate > y && ys - movingRate < y)
            y = ys;
        else
            y += movingRate * Math.sin(alpha);
        CellSize -= sizeRate;
        CellToWallRatio -= wallRate;
        if (CellSize < GoalSize){
            CellSize = GoalSize;
            CellToWallRatio = 0;
        }
    }

    public int getCellSize() {
        return CellSize;
    }

    public double getCellToWallRatio() {
        return CellToWallRatio;
    }

    public int getMovingRate() {
        return movingRate;
    }

    public int getIndex() {
        return index;
    }

    public int getRow() {
        return row;
    }

    public int getB_height() {
        return b_height;
    }

    public int getB_width() {
        return b_width;
    }

    public ArrayList<Integer> getTreeSize() {
        return treeSize;
    }

    public double getWallRate() {
        return wallRate;
    }

    public TTTreeTransition getPrev() {
        return prev;
    }

    public void setPrev(TTTreeTransition prev) {
        this.prev = prev;
    }
}
