package Transition;

import Cell.Cell;
import Cell.MazeCell;
import Cell.TransformingCell;

import java.awt.*;

public class TTMTransition extends Transition {
    private MazeCell mazeCell;
    private TransformingCell transformingCell;
    private int directionX = 0;
    private int directionY = 0;

    public TTMTransition(TransformingCell transformingCell, MazeCell mazeCell) {
        super();
        this.mazeCell = mazeCell;
        this.transformingCell = transformingCell;
        if (mazeCell.getX() < transformingCell.getX() - transformingCell.getCellSize() / 2)
            directionX = 1;
        else
        if ((mazeCell.getX() > transformingCell.getX() - transformingCell.getCellSize() / 2))
            directionX = - 1;
        else
        if (mazeCell.getY() < transformingCell.getY() - transformingCell.getCellSize() / 2)
            directionY = 1;
        else
            directionY = -1;
    }

    public void reset() {
        drawn = false;
        mazeCell.resetDrawn();
    }

    public void draw(Graphics2D g) {
        drawn = true;
        int[] xs = new int[4];
        int[] ys = new int[4];
        xs[0] = mazeCell.getX();
        ys[0] = mazeCell.getY();
        xs[1] = transformingCell.getX() - transformingCell.getCellSize() / 2 + (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())) / 2;
        ys[1] = transformingCell.getY() - transformingCell.getCellSize() / 2 + (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())) / 2;
        xs[2] = mazeCell.getX() + mazeCell.getCellSize();
        ys[2] = mazeCell.getY() + mazeCell.getCellSize();
        xs[3] = transformingCell.getX() + transformingCell.getCellSize() / 2 - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())) / 2;
        ys[3] = transformingCell.getY() + transformingCell.getCellSize() / 2 - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())) / 2;
        if (directionX != 0){
            xs[0] += (directionX + 1) / 2 * mazeCell.getCellSize();
            xs[2] += (directionX - 1) / 2 * mazeCell.getCellSize();
            xs[1] -= (directionX - 1) / 2 * (transformingCell.getCellSize() - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())));
            xs[3] -= (directionX + 1) / 2 * (transformingCell.getCellSize() - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())));
        }
        else{
            ys[0] += (directionY + 1) / 2 * mazeCell.getCellSize();
            ys[2] += (directionY - 1) / 2 * mazeCell.getCellSize();
            ys[1] -= (directionY - 1) / 2 * (transformingCell.getCellSize() - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())));
            ys[3] -= (directionY + 1) / 2 * (transformingCell.getCellSize() - (int)(transformingCell.getCellSize()*(1 - transformingCell.getCellToWallRatio())));
        }
        g.setStroke(new BasicStroke(3));
        g.setColor(CellColor);
        g.drawLine(xs[1], ys[1], xs[0], ys[0]);
        g.drawLine(xs[2], ys[2], xs[3], ys[3]);
        g.setStroke(new BasicStroke(1));
        mazeCell.draw(g);
    }

    @Override
    public Cell[] getCells() {
        return new Cell[] {transformingCell, mazeCell};
    }

    @Override
    public void delself() {

    }
}
