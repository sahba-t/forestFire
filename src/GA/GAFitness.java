package GA;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Sahba on 3/25/17.
 * This class Runs a simulation and returns the fitness score
 */
public class GAFitness implements Runnable {
    private final int SIZE;
    private final boolean twoSpecies;
    private double P1;
    private double P2;
    private GASpecies specie1;
    private GASpecies specie2;

    private final static double lightP = 0.001;
    private final GATree[][] jungle;
    private final static int MAX_ITR = 5000;
    private final Random random;
    private int liveCounter;
    private final HashSet<GATree> onFire;
    private final HashSet<GATree> onFireCopy;
    private boolean terminate = false;
    private int[][] neighbours = new int[][]{{-1, 1}, {-1, -1}, {-1, 0}, {1, 1}, {1, -1}, {1, 0}, {0, 1}, {0, -1}};
    private static final boolean debug = false;
    // private final JungleDataKeeper dataKeeper;
    private int iteration;
    private String identifire;

    GAFitness(int size, boolean twoSpecies) {
        this.SIZE = size + 2;
        this.twoSpecies = twoSpecies;
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

    GAFitness(int size, GASpecies specie1, GASpecies species2, boolean twoSpecies) {
        this(size, twoSpecies);
        setSpecies(specie1, species2);
    }

    private void setSpecies(GASpecies specie1, GASpecies species2) {
        this.P1 = specie1.getP();
        this.specie1 = specie1;
        if (twoSpecies) {
            this.P2 = species2.getP() + P1;
            this.specie2 = species2;
        }
    }

    void resetParameter(GASpecies s1, GASpecies s2) {

        for (int i = 1; i < SIZE - 1; i++) {
            for (int j = 1; j < SIZE - 1; j++) {
                jungle[i][j].setState('e');
            }
        }
        onFire.clear();
        onFireCopy.clear();
        iteration = 0;
        setSpecies(s1, s2);
    }

    /**
     * Any Class that wishes to use this GA Evaluation should call this method
     * It will run the simulation and terminates when either all the tress are burnt
     * or 5000 iterations are done!
     *
     * @return an int array element 0 is the number of iterations until termination
     * and the second element is the number of live trees at termination (if all trees burnt will be 0)
     */
    void simulate() {
        if (specie1 == null || (twoSpecies && specie2 == null)) {
            System.out.println("AT LEAST ONE SPECIES SHOULD BE SET! ERROR!");
        }
        GATree tree;
        liveCounter = 0;
        //it will now overflow value << 5000 * 625 << max double
        double longevity = 0;
        while (iteration < MAX_ITR) {
            if (debug) {
                System.out.println("itr: " + iteration);
            }
            setOnFire();

            if (terminate) {
                break;
            }
            if (iteration > 0) {
                longevity += liveCounter;

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
                    } else if (tree.getState() == 'e') {
                        if (random.nextDouble() < P1) {
                            tree.setState('s');
                            liveCounter++;
                        } else if (twoSpecies && random.nextDouble() < P2) {
                            tree.setState('w');
                            liveCounter++;
                        }

                    }
                }
            }
            iteration++;
        }
        if (!terminate) {
            setOnFire();
            longevity += liveCounter;
        }
        double biomass = longevity / (double) iteration;
        specie1.setBiomass(biomass);
        specie1.setLongevity(iteration);

        System.out.println(specie1);

        if (twoSpecies) {
            specie2.setLongevity(iteration);
            specie2.setBiomass(biomass);
            if (debug) {
                System.out.println(specie2);
            }
        }
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

            if (liveCounter == 0) {
                terminate("ALL TREES BURNT!");
                return;
            }
        }
        if (debug) {
            System.out.println("Live Counter: " + liveCounter);
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
        GASpecies species1 = new GASpecies(0.8);
        GASpecies species2 = new GASpecies(0.5);
        GAFitness ga = new GAFitness(200, species1, species2, false);
        new Thread(ga).start();
    }


//        double[] result = ga.simulate();
//        System.out.println(result[0] + ", " + result[1]);


    @Override
    public void run() {
        simulate();
    }
}
