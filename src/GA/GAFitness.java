package GA;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Sahba on 3/25/17.
 * This class Runs a simulation and returns the fitness score
 */
public class GAFitness {
    private final int SIZE;
    private final double P;
    private final static double lightP = 0.001;
    private final GATree[][] jungle;
    private final static int MAX_ITR = 5000;
    private final Random random;
    private int liveCounter;
    private int iteration = 0;
    private final HashSet<GATree> onFire;
    private final HashSet<GATree> onFireCopy;
    private boolean terminate = false;
    private int[][] neighbours = new int[][]{{-1, 1}, {-1, -1}, {-1, 0}, {1, 1}, {1, -1}, {1, 0}, {0, 1}, {0, -1}};
    private static final boolean debug = true;

    /**
     * The only constructor of this class
     *
     * @param size the size of the board @todo (Will later be fixed to 250)
     * @param p    the probability of growing species 1. @todo later will have to add species 2
     */
    GAFitness(int size, double p) {
        this.SIZE = size + 2;
        P = p;
        random = new Random();
        jungle = new GATree[SIZE][SIZE];
        for (int i = 1; i < SIZE - 1; i++) {
            for (int j = 1; j < SIZE - 1; j++) {
                jungle[i][j] = new GATree(i, j);
            }
        }
        onFire = new HashSet<>();
        onFireCopy = new HashSet<>();
    }

    /**
     * Any Class that wishes to use this GA Evaluation should call this method
     * It will run the simulation and terminates when either all the tress are burnt
     * or 5000 iterations are done!
     *
     * @return an int array element 0 is the number of iterations until termination
     * and the second element is the number of live trees at termination (if all trees burnt will be 0)
     */
    int[] simulate() {

        GATree tree;
        int[] result = new int[2];
        while (iteration < MAX_ITR) {
            System.out.println("itr: " + iteration);
            setOnFire();
            if (terminate) {
                break;
            }
            for (int i = 1; i < SIZE - 1; i++) {
                for (int j = 1; j < SIZE - 1; j++) {
                    tree = jungle[i][j];
                    if (tree.getState() == 'w' || tree.getState() == 's') {
                        if (random.nextDouble() < lightP) {
                            liveCounter--;
                            tree.setState('f');
                            onFire.add(tree);
                        }
                    } else if (tree.getState() == 'e' && random.nextDouble() < P) {
                        tree.setState('s');
                        liveCounter++;
                    }
                }
            }
            iteration++;
        }
        setOnFire();
        result[0] = iteration;
        result[1] = liveCounter;
        return result;

    }

    /**
     * sets the status of the tress which caught fire in the last iterations to empty
     * also sets the neighbours of such trees on fire by calling the burnNeighbours method!
     */
    private void setOnFire() {
        if (!onFire.isEmpty()) {
            onFireCopy.clear();
            onFireCopy.addAll(onFire);
            onFire.clear();
            for (GATree tree : onFireCopy) {
                burnNeighbours(tree);
                if (terminate) {
                    return;
                }
                jungle[tree.getRow()][tree.getColumn()].setState('e');
            }

            if (debug) {
                System.out.println("Live Counter: " + liveCounter);
            }
            if (liveCounter == 0) {
                terminate("ALL TREES BURNT!");
                return;
            }
        }
    }

    /**
     * ignites the neighbours of a tree that is on fire.
     *
     * @param tree the tree whose neighbours have to catch fire
     */
    private void burnNeighbours(GATree tree) {
        if (liveCounter == 0) {
            terminate("All burnt!");
            return;
        }
        GATree[] neighbours = getNeighbours(tree.getRow(), tree.getColumn());
        GATree neighbour;
        for (int i = 0; i < 8 && neighbours[i] != null; i++) {

            neighbour = neighbours[i];
            if (neighbour.getState() == 's' || neighbour.getState() == 'w') {
                liveCounter--;
                neighbour.setState('f');
                onFire.add(neighbour);
            }
        }
    }

    /**
     * given the row and column number of a tree returns its 8 neighbours
     *
     * @param row    the row of that tree
     * @param column the column of the tree
     * @return an array of neighbours (in case less than 8 neighbours a null element willl signal there are
     * no more neighbours in the array)
     */
    private GATree[] getNeighbours(int row, int column) {
        GATree[] neighbourTrees = new GATree[8];
        int nRow;
        int nColumn;
        for (int i = 0, j = 0; i < neighbours.length; i++) {
            nRow = row + neighbours[i][0];
            nColumn = column + neighbours[i][1];
            if (jungle[nRow][nColumn] != null) {
                neighbourTrees[j] = jungle[nRow][nColumn];
                j++;
            }
        }
        return neighbourTrees;
    }

    /**
     * prints a debug message and signals the simulation to end
     *
     * @param message the debug message to be printed to stdout
     */
    private void terminate(String message) {
        System.out.format("terminated after %d iterations due to:%s\n", iteration, message);
        terminate = true;

    }

    public static void main(String[] args) {
        GAFitness ga = new GAFitness(250, 0.75);
        int[] result = ga.simulate();
        System.out.println(result[0] + ", " + result[1]);
    }
}
