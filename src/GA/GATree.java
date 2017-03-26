package GA;

/**
 * Created by Sahba on 3/25/17.
 * An abstraction of the trees for the forest fire simulation
 */
class GATree {
    private final int row;
    private final int column;
    /**
     * denotes the state of the tree in the simulation<br>
     * States are 'f'=fire 'e'=empty 's'=species 1 'w' species 2
     */
    private char state;

    GATree(int row, int column) {
        this.row = row;
        this.column = column;
        this.state = 'e';
    }

    char getState() {
        return state;
    }

    void setState(char state) {
        this.state = state;
    }

    int getRow() {
        return row;
    }

    int getColumn() {
        return column;
    }
}
