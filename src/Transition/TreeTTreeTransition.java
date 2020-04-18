package Transition;

import Cell.Cell;
import Cell.TreeCell;

import java.awt.*;

public class TreeTTreeTransition extends Transition{
    private TreeCell[] Cells = new TreeCell[2];

    public TreeTTreeTransition(TreeCell treeCell1, TreeCell treeCell2) {
        super();
        Cells[0] = treeCell1;
        Cells[1] = treeCell2;
    }

    public void reset() {
        drawn = false;
        Cells[1].resetDrawn();
    }

    public void draw(Graphics2D g) {
        drawn = true;
        Cells[1].move();
        g.setStroke(new BasicStroke(3));
        g.setColor(CellColor);
        g.drawLine(Cells[1].getX(), Cells[1].getY(), Cells[0].getX(), Cells[0].getY());
        g.setStroke(new BasicStroke(1));
        Cells[1].draw(g);
    }

    @Override
    public Cell[] getCells() {
        return Cells;
    }

    @Override
    public void delself() {

    }
}
