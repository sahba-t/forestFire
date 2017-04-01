package gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;

/**
 * Created by Sahba on 3/23/2017.
 */
public class Tree extends Button {
    private final int row;
    private final int column;
    private State state;
    private GraphicsContext gcx;
    private static final int CELL_SIZE = 5;

    Tree(int row, int column, GraphicsContext gcx) {
        super();
        this.row = row;
        this.column = column;
        this.gcx = gcx;
        setState(State.EMPTY);
    }

    public void setState(State state) {
        this.state = state;
        gcx.setFill(state.getColor());

        gcx.fillRect(getColumn() * CELL_SIZE, getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        gcx.strokeRect(getColumn() * CELL_SIZE, getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    public State getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tree tree = (Tree) o;

        if (row != tree.row) return false;
        return column == tree.column;

    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }
}
