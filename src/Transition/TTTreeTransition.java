package Transition;

import Cell.Cell;
import Cell.TreeCell;
import Cell.TransformingCell;

import java.awt.*;

public class TTTreeTransition extends Transition{ //transforming to tree transition
    private TreeCell treeCell;
    private TransformingCell transformingCell;
    private int directionX = 0;
    private int directionY = 0;

    public TTTreeTransition(TreeCell treeCell, TransformingCell transformingCell) {
        super();
        this.treeCell = treeCell;
        this.transformingCell = transformingCell;
        if (treeCell.getX() < transformingCell.getX())
            directionX = 1;
        else
        if (treeCell.getX() > transformingCell.getX())
            directionX = - 1;
        else
        if (treeCell.getY() < transformingCell.getY())
            directionY = 1;
        else
            directionY = -1;
    }

    public void reset() {
        drawn = false;
        transformingCell.resetDrawn();
    }

    public void draw(Graphics2D g) {
        drawn = true;
        transformingCell.move();
        int[] xs = new int[2];
        int[] ys = new int[2];
        xs[1] = transformingCell.getX() - transformingCell.getCellSize() / 2 + (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())) / 2;
        ys[1] = transformingCell.getY() - transformingCell.getCellSize() / 2 + (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())) / 2;
        xs[0] = transformingCell.getX() + transformingCell.getCellSize() / 2 - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())) / 2;
        ys[0] = transformingCell.getY() + transformingCell.getCellSize() / 2 - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())) / 2;
        if (directionX != 0){
            xs[1] -= (directionX - 1) / 2 * (transformingCell.getCellSize() - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())));
            xs[0] -= (directionX + 1) / 2 * (transformingCell.getCellSize() - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())));
        }
        else{
            ys[1] -= (directionY - 1) / 2 * (transformingCell.getCellSize() - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())));
            ys[0] -= (directionY + 1) / 2 * (transformingCell.getCellSize() - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())));
        }
        g.setStroke(new BasicStroke(3));
        g.setColor(CellColor);
        g.drawLine(xs[1], ys[1], treeCell.getX(), treeCell.getY());
        g.drawLine(xs[0], ys[0], treeCell.getX(), treeCell.getY());
        g.setStroke(new BasicStroke(1));
        transformingCell.draw(g);
    }

    @Override
    public Cell[] getCells() {
        return new Cell[] {treeCell, transformingCell};
    }

    @Override
    public void delself() {

    }
}
