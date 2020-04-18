package Cell;

import Transition.Transition;

import java.awt.*;

public abstract class Cell {
    protected int x;
    protected int y;
    protected int arrX;
    protected int arrY;
    protected boolean drawn = false;
    protected Transition[] transitions = {null, null, null, null};
    protected Color WallColor;
    protected Color CellColor;
    protected boolean searched;

    public Cell(int xs, int ys, int arrXs, int arrYs, Color wallC, Color cellC) {
        x = xs;
        y = ys;
        arrX = arrXs;
        arrY = arrYs;
        WallColor = wallC;
        CellColor = cellC;
    }

    public void resetSearched(){
        searched = false;
        for (Transition transition : transitions)
            if (transition != null){
                if (transition.getCells()[0].isSearched())
                    transition.getCells()[0].resetSearched();
                if (transition.getCells()[1].isSearched())
                    transition.getCells()[1].resetSearched();
            }
    }

    public void resetDrawn(){
        drawn = false;
        for (Transition transition : transitions)
            if (transition != null && transition.isDrawn())
                transition.reset();
    }

    public boolean dfs(Cell cell) {
        if (this.isSearched())
            return false;
        if (this == cell)
            return true;
        searched = true;
        boolean ans = false;
        for (Transition transition : transitions){
            if (transition != null){
                ans |= transition.getCells()[0].dfs(cell);
                ans |= transition.getCells()[1].dfs(cell);
            }
        }
        return ans;
    }

    public void setTrans(Transition transition){
        int i = 0;
        while (transitions[i] != null)
            i++;
        transitions[i] = transition;
    }

    public boolean isSearched() {
        return searched;
    }

    public boolean isDrawn() {
        return drawn;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public abstract void draw(Graphics2D g);

    public void setCellColor(Color cellColor) {
        CellColor = cellColor;
    }

    public void setWallColor(Color wallColor) {
        WallColor = wallColor;
    }

    public Color getCellColor() {
        return CellColor;
    }

    public Color getWallColor() {
        return WallColor;
    }

    public Transition[] getTransitions() {
        return transitions;
    }

    public int getArrX() {
        return arrX;
    }

    public int getArrY() {
        return arrY;
    }

    public abstract double getCellToWallRatio();
}
