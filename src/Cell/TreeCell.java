package Cell;

import Transition.Transition;
import Transition.TTTreeTransition;
import Transition.TreeTTreeTransition;

import java.awt.*;
import java.util.ArrayList;

public class TreeCell extends Cell {
    private int movingRate;
    private int index;
    private int row;
    private int b_height;
    private int b_width;
    private int CellSize;
    private TreeTTreeTransition prev;
    private ArrayList<Integer> treeSize;

    public TreeCell(TransformingCell cell) {
        super(cell.getX(), cell.getY(), cell.getArrX(), cell.getArrY(), cell.getWallColor(), cell.getCellColor());
        movingRate = cell.getMovingRate();
        index = cell.getIndex();
        row = cell.getRow();
        b_height = cell.getB_height();
        b_width = cell.getB_width();
        CellSize = cell.getCellSize();
        treeSize = cell.getTreeSize();
        if (treeSize.size() == row + 1)
            treeSize.add(row + 1, 0);
        int ind = treeSize.get(row + 1);
        for (Transition transition : cell.getTransitions()) {
            if (transition != null){
                setTrans(new TTTreeTransition(this, new TransformingCell((MazeCell)transition.getCells()[1], movingRate, cell.getWallRate(), ind, row + 1, CellSize, b_height, b_width, treeSize)));
                transition.delself();
                ind++;
            }
        }
        TransformingCell cll;
        for (Transition transition: transitions)
            if (transition != null) {
            cll = (TransformingCell) transition.getCells()[1];
            cll.setPrev((TTTreeTransition) transition);
            }
        treeSize.set(row + 1, ind);
    }

    @Override
    public void draw(Graphics2D g) {
        if(isDrawn())
            return;
        drawn = true;
        for (Transition transition : transitions)
            if (transition != null)
                transition.draw(g);
        g.setColor(WallColor);
        g.fillRect(x - CellSize / 2, y - CellSize / 2, CellSize, CellSize);
    }

    public void move(){
        TreeCell temp;
        if (transitions[0] != null)
            if (transitions[0].getCells()[1].getCellToWallRatio() == 0)
                for (int i = 0; i < transitions.length; i++)
                    if (transitions[i] != null){
                        temp = new TreeCell((TransformingCell)transitions[i].getCells()[1]);
                        transitions[i] = new TreeTTreeTransition(this, temp);
                        temp.setPrev((TreeTTreeTransition) transitions[i]);
                    }
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
    }

    public void colorBackwards(Color c1, Color c2){
        if (WallColor.equals(c2))
            WallColor = c1;
        if (prev == null)
            return;
        prev.setCellColor(c1);
        ((TreeCell)prev.getCells()[0]).colorBackwards(c1, c2);
    }

    @Override
    public double getCellToWallRatio() {
        return 1;
    }

    public void setPrev(TreeTTreeTransition prev) {
        this.prev = prev;
    }
}
