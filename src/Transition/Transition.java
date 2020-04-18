package Transition;

import Cell.Cell;

import java.awt.*;

public abstract class Transition {
    protected boolean drawn = false;
    protected Color CellColor;

    public Transition(){
        CellColor = new Color(169, 184, 201);
    }

    public Transition(Color cellC){
        CellColor = cellC;
    }

    public abstract void reset();

    public abstract void draw(Graphics2D g);

    public abstract Cell[] getCells();

    public boolean isDrawn() {
        return drawn;
    }

    public abstract void delself();

    public void setCellColor(Color cellColor) {
        CellColor = cellColor;
    }
}
