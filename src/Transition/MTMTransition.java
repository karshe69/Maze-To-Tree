package Transition;

import Cell.MazeCell;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class MTMTransition extends Transition{
    private MazeCell[] cells;
    private int CellSize;
    private double CellToWallRatio;
    private double weight;

    public MTMTransition(MazeCell cell1, MazeCell cell2, Color cellC, int cellSize, double cellWallRatio){
        super(cellC);
        cells = new MazeCell[]{cell1, cell2};
        CellToWallRatio = cellWallRatio;
        CellSize = cellSize;
        weight = new Random().nextDouble();
    }

    @Override
    public void draw(Graphics2D g) {
        if(isDrawn())
            return;
        int[] xs = new int[4];
        int[] ys = new int[4];
        xs[0] = cells[0].getX() + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2);
        ys[0] = cells[0].getY() + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2);
        xs[1] = cells[1].getX() + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2);
        ys[1] = cells[1].getY() + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2);
        xs[2] = cells[1].getX() + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2) + (int)(CellSize*CellToWallRatio);
        ys[2] = cells[1].getY() + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2) + (int)(CellSize*CellToWallRatio);
        xs[3] = cells[0].getX() + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2) + (int)(CellSize*CellToWallRatio);
        ys[3] = cells[0].getY() + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2) + (int)(CellSize*CellToWallRatio);

        if (cells[0].getX() > cells[1].getX()){
            xs[3] = xs[0];
            xs[1] = xs[2];
        }
        if (cells[0].getX() < cells[1].getX()){
            xs[0] = xs[3];
            xs[2] = xs[1];
        }
        if (cells[0].getY() > cells[1].getY()){
            ys[3] = ys[0];
            ys[1] = ys[2];
        }
        if (cells[0].getY() < cells[1].getY()){
            ys[0] = ys[3];
            ys[2] = ys[1];
        }

        drawn = true;
        for (MazeCell cell: cells)
            cell.draw(g);
        g.setColor(CellColor);
        g.fillPolygon(xs, ys, 4);

    }

    public void reset(){
        drawn = false;
        for (MazeCell cell: cells)
            if (cell.isDrawn())
                cell.resetDrawn();
    }

    public void delself(){
        cells[0].delTrans(this);
        cells[1].delTrans(this);
    }

    public MazeCell[] getCells() {
        return cells;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MTMTransition that = (MTMTransition) o;
        return (this.cells[0].equals(that.cells[0]) && this.cells[1].equals(that.cells[1])) || (this.cells[0].equals(that.cells[1]) && this.cells[1].equals(that.cells[0]));
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(CellSize, CellToWallRatio);
        result = 31 * result + Arrays.hashCode(getCells());
        return result;
    }

    public double getWeight() {
        return weight;
    }
}
