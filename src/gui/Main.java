package gui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Sahba on 3/23/2017.
 */
class Main extends AnimationTimer implements GUIDataKeeper{
    private final Tree[][] JUNGLE;
    private final int SIZE;
    private final double p1;
    private double p2;
    private boolean twoSpecies = false;
    private static final double LIGHT_PROB = 0.001;
    private final Random random;
    private final ArrayList<Tree> onFire;
    private final ArrayList<Tree> onFireCopy;
    private final boolean useFireFighters;
    private final int[][] neighbours;
    private int liveCounter;
    private final Terminable terminable;
    private long previousTime;
    private int itr;
    private final GraphicsContext gcx;
    private int fireFighterCount;

    /**
     * Constructor for the animation (simulation class). If this class is used it is assumed that
     * one specie simulation was intended
     *
     * @param size       the size of the board
     * @param p1         the growth probability for species 1
     * @param terminable the call back class to receive the result
     * @param gcx        the graphic content of the GUI canvas
     */
    Main(int size, double p1, Terminable terminable, GraphicsContext gcx, int fireFighterCount) {
        this.p1 = p1;
        this.SIZE = size + 2;
        this.gcx = gcx;
        JUNGLE = new Tree[SIZE][SIZE];
        buildJungle();
        random = new Random();
        onFire = new ArrayList<>();
        onFireCopy = new ArrayList<>();
        neighbours = new int[][]{{-1, 1}, {-1, -1}, {-1, 0}, {1, 1}, {1, -1}, {1, 0}, {0, 1}, {0, -1}};
        this.terminable = terminable;
        this.twoSpecies = false;
        if (fireFighterCount > 0) {
            useFireFighters = true;
            this.fireFighterCount = fireFighterCount;
        } else {
            useFireFighters = false;
            fireFighterCount = 0;
        }
    }

    /**
     * @param size       the size of the board
     * @param p1         the growth probability for species 1
     * @param p2         the growth probability for species 2
     * @param terminable the call back class to receive the result
     * @param gcx        the graphic content of the GUI canvas
     */
    Main(int size, double p1, double p2, Terminable terminable, GraphicsContext gcx, int fireFighterCount) {
        this(size, p1, terminable, gcx, fireFighterCount);
        this.p2 = p2 + p1;
        this.twoSpecies = true;

    }

    private void buildJungle() {
        for (int i = 1; i < SIZE - 1; i++) {
            for (int j = 1; j < SIZE - 1; j++) {
                JUNGLE[i][j] = new Tree(i, j, gcx);
            }
        }
        System.out.println("jungle built");
    }

    private void simulate() {

        setOnFire();
        Tree tree;
        for (int i = 1; i < SIZE - 1; i++) {
            for (int j = 1; j < SIZE - 1; j++) {
                tree = JUNGLE[i][j];
                if (random.nextDouble() < LIGHT_PROB) {
                    // XXX it should be either species one or species 2 check
                    if (tree.getState() == State.SPECIES1 || tree.getState() == State.SPECIES2) {
                        tree.setState(State.FIRE);
                        onFire.add(tree);
                        liveCounter--;
                    }
                }
                if (tree.getState() == State.EMPTY || tree.getState() == State.EXTINGUISHED)
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
            if (useFireFighters) {
                if (fireFighterCount >= onFire.size()) {

                    for (Tree anOnFire : onFire) {

                        anOnFire.setState(State.EXTINGUISHED);
                    }
                    System.out.format("all %d fires extinguished!\n", onFire.size());
                    onFire.clear();
                } else {
                    int i = 0;
                    int fires = onFire.size();
                    Tree tree;
                    Collections.shuffle(onFire);
                    for (Iterator<Tree> treeItr = onFire.iterator(); i < fireFighterCount; i++) {
                        tree = treeItr.next();
                        tree.setState(State.EXTINGUISHED);

                        treeItr.remove();
                    }
                    System.out.format("%d fires extinguished out of %d!\n", i, fires);
                }
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
        keepData(itr, liveCounter, onFire.size());
    }

    @Override
    public void keepData(int iteration, int liveTrees, int treesOnFire) {
        try {
            File GAdata = new File("data/GUIdata.csv");
            FileWriter fileWriter;
            StringBuilder sb = new StringBuilder();

            if (!GAdata.isFile()){
                fileWriter = new FileWriter(GAdata);
                sb.append("iteration");
                sb.append(',');
                sb.append("live_trees");
                sb.append(',');
                sb.append("on_fire");
                sb.append('\n');
            } else {
                fileWriter = new FileWriter(GAdata, true);
            }

            sb.append(iteration);
            sb.append(',');
            sb.append(liveTrees);
            sb.append(',');
            sb.append(treesOnFire);
            sb.append('\n');

            fileWriter.write(sb.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}