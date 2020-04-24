package Cell;

import Transition.Transition;
import Transition.MTMTransition;

import java.awt.*;

public class MazeCell extends Cell{
    private int CellSize;
    private double CellToWallRatio;

    public MazeCell(int xs, int ys, int cellSize, double cellWallRatio, Color wallC, Color cellC){
        super(xs*cellSize, ys*cellSize, xs, ys, wallC, cellC);
        CellSize = cellSize;
        CellToWallRatio = cellWallRatio;
    }

    public void draw(Graphics2D g){
        if(isDrawn())
            return;
        drawn = true;
        g.setColor(WallColor);
        g.fillRect(x,  y, CellSize, CellSize);
        for (Transition transition : transitions)
            if (transition != null)
                transition.draw(g);
        g.setColor(CellColor);
        g.fillRect(x + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2), y + (int)Math.round(CellSize*(1 -CellToWallRatio) / 2), (int)(CellSize*CellToWallRatio), (int)(CellSize*CellToWallRatio));
    }

    public void delTrans(MTMTransition transition){
        for (int i = 0; i < transitions.length; i++) {
            if (transitions[i] != null)
                if (transition.equals(transitions[i]))
                    transitions[i] = null;
        }
    }

    public boolean dfsShell(MazeCell cell){
        resetSearched();
        return dfs(cell);
    }

    public int getCellSize() {
        return CellSize;
    }

    public double getCellToWallRatio() {
        return CellToWallRatio;
    }
}
