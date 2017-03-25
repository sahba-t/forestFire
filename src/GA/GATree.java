package GA;

/**
 * Created by root on 3/25/17.
 */
public class GATree {
    private final int row;
    private final int column;
    private char state;

    GATree(int row, int column) {
        this.row = row;
        this.column = column;
        this.state = 'e';
    }

    public char getState() {
        return state;
    }

    public void setState(char state) {
        this.state = state;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
