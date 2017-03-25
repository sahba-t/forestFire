package GA;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Sahba on 3/25/17.
 */
public class GAFitness {
    private final int SIZE;
    private final double P;
    private final double lightP = 0.001;
    private final GATree[][] jungle;
    private final int MAXITR = 5000;
    private final Random random;
    private int liveCounter;
    private int iteration = 0;
    private final HashSet<GATree> onFire;
    private final HashSet<GATree> onFireCopy;
    private boolean terminate = false;
    int[][] neighbours = new int[][]{{-1, 1}, {-1, -1}, {-1, 0}, {1, 1}, {1, -1}, {1, 0}, {0, 1}, {0, -1}};

    private GAFitness(int size, double p) {
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

    int[] simulate() {

        GATree tree;
        int[] result = new int[2];
        while (iteration < MAXITR) {
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
        result[0] = iteration;
        result[1] = liveCounter;
        return result;

    }

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
            System.out.println("Live Counter: " + liveCounter);
            if (liveCounter == 0) {
                terminate("ALL TREES BURNT!");
                return;
            }
        }
    }

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

    private void terminate(String message) {
        System.out.format("terminated after %d iterations due to:%s\n", iteration, message);
        terminate = true;

    }

    public static void main(String[] args) {
        GAFitness ga = new GAFitness(250, 1);
        int[] result = ga.simulate();
        System.out.println(result[0] + ", " + result[1]);
    }
}