package gui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by Sahba on 3/23/2017.
 */
class Main extends AnimationTimer {
    private final Tree[][] JUNGLE;
    private final int SIZE;
    private final double p1;
    private double p2;
    private boolean twoSpecies = false;
    private static final double LIGHT_PROB = 0.001;
    private final Random random;
    private final HashSet<Tree> onFire;
    private final HashSet<Tree> onFireCopy;

    private final int[][] neighbours;
    private int liveCounter;
    private final Terminable terminable;
    private long previousTime;
    private int itr;
    private final GraphicsContext gcx;

    /**
     * Constructor for the animation (simulation class). If this class is used it is assumed that
     * one specie simulation was intended
     *
     * @param size       the size of the board
     * @param p1         the growth probability for species 1
     * @param terminable the call back class to receive the result
     * @param gcx        the graphic content of the GUI canvas
     */
    Main(int size, double p1, Terminable terminable, GraphicsContext gcx) {
        this.p1 = p1;
        this.SIZE = size;
        this.gcx = gcx;
        JUNGLE = new Tree[size][size];
        buildJungle();
        random = new Random();
        onFire = new HashSet<>();
        onFireCopy = new HashSet<>();
        neighbours = new int[][]{{-1, 1}, {-1, -1}, {-1, 0}, {1, 1}, {1, -1}, {1, 0}, {0, 1}, {0, -1}};
        this.terminable = terminable;
        this.twoSpecies = false;
    }

    /**
     * @param size       the size of the board
     * @param p1         the growth probability for species 1
     * @param p2         the growth probability for species 2
     * @param terminable the call back class to receive the result
     * @param gcx        the graphic content of the GUI canvas
     */
    Main(int size, double p1, double p2, Terminable terminable, GraphicsContext gcx) {
        this(size, p1, terminable, gcx);
        this.p2 = p2 + p1;
        this.twoSpecies = true;

    }

    private void buildJungle() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JUNGLE[i][j] = new Tree(i, j, gcx);
            }
        }
        System.out.println("jungle built");
    }

    private void simulate() {

        setOnFire();
        Tree tree;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                tree = JUNGLE[i][j];
                if (random.nextDouble() < LIGHT_PROB) {
                    // XXX it should be either species one or species 2 check
                    if (tree.getState() == State.SPECIES1 || tree.getState() == State.SPECIES2) {
                        tree.setState(State.FIRE);
                        onFire.add(tree);
                        liveCounter--;
                    }
                }
                if (tree.getState() == State.EMPTY)
                    if (random.nextDouble() < p1) {
                        tree.setState(State.SPECIES1);
                        liveCounter++;
                    } else {
                        if (twoSpecies && random.nextDouble() < p2) {
                            tree.setState(State.SPECIES2);
                            liveCounter++;
                        }
                    }

            }
        }
    }

    private void setOnFire() {
        if (!onFire.isEmpty()) {
            onFireCopy.clear();
            onFireCopy.addAll(onFire);
            onFire.clear();
            for (Tree tree : onFireCopy) {
                burnNeighbours(tree);
                JUNGLE[tree.getRow()][tree.getColumn()].setState(State.EMPTY);
            }
            System.out.println("Live Counter: " + liveCounter);
            if (liveCounter < 0) {
                terminable.terminate("Negative");
            }
            if (liveCounter == 0) {
                terminable.terminate("ALL TREES BURNT!");
            }
        }
    }

    private void burnNeighbours(Tree tree) {
        if (liveCounter == 0) {
            terminable.terminate("All burnt!");
        }
        Tree[] neighbours = getNeighbours(tree.getRow(), tree.getColumn());
        Tree neighbour;
        for (int i = 0; i < 8 && neighbours[i] != null; i++) {

            neighbour = neighbours[i];
            if (neighbour.getState() == State.SPECIES1 || neighbour.getState() == State.SPECIES2) {
                liveCounter--;
                neighbour.setState(State.FIRE);
                onFire.add(neighbour);
            }
        }
    }

    private Tree[] getNeighbours(int row, int column) {
        Tree[] neighbourTrees = new Tree[8];
        int nrow;
        int ncolumn;
        for (int i = 0, j = 0; i < neighbours.length; i++) {
            nrow = row + neighbours[i][0];
            ncolumn = column + neighbours[i][1];
            if (nrow < 0 || nrow >= SIZE || ncolumn < 0 || ncolumn >= SIZE) {
                continue;
            }
            neighbourTrees[j] = JUNGLE[nrow][ncolumn];
            j++;
        }
        return neighbourTrees;
    }

    @Override
    public void handle(long now) {
        if (previousTime == 0) {
            previousTime = now;
        } else {

            System.out.format("itr=%d; time=%.5f\n", itr, (now - previousTime) / 1_000_000_000.00);
            previousTime = now;
        }
        itr++;
        simulate();
    }
}