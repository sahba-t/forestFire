package gui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sahba on 4/9/17.
 * This class does the forest fire simulations without GUI
 */
public class FireFighterSimulation implements Runnable {
    private final Tree[][] JUNGLE;
    private final int SIZE;
    private double p1;
    private double p2;
    private boolean twoSpecies = false;
    private static final double LIGHT_PROB = 0.001;
    private final Random random;
    private final ArrayList<Tree> onFire;
    private final ArrayList<Tree> onFireCopy;
    private boolean useFireFighters;
    private final int[][] neighbours;
    private int liveCounter;
    private double biomass;
    private int iteration;
    private boolean terminate;
    private int fireFighterCount;
    private static final int MAX_ITR = 1000;

    /**
     * Constructor for the animation (simulation class). If this class is used it is assumed that
     * one specie simulation was intended
     *
     * @param size the size of the board
     * @param p1   the growth probability for species 1
     */
    private FireFighterSimulation(int size, double p1, int fireFighterCount) {
        iteration = 0;
        this.p1 = p1;
        this.SIZE = size + 2;
        JUNGLE = new Tree[SIZE][SIZE];
        buildJungle();
        random = new Random();
        onFire = new ArrayList<>();
        onFireCopy = new ArrayList<>();
        neighbours = new int[][]{{-1, 1}, {-1, -1}, {-1, 0}, {1, 1}, {1, -1}, {1, 0}, {0, 1}, {0, -1}};
        this.twoSpecies = false;
        if (fireFighterCount > 0) {
            useFireFighters = true;
            this.fireFighterCount = fireFighterCount;
        } else {
            useFireFighters = false;
        }
    }

    /**
     * Enables to user of this class to reuse this class for simulations without the need to
     * create a new instance. For performance purposes
     *
     * @param p1               the new p1 for the new simulation
     * @param p2               the new p2 for the new simulation
     * @param fireFighterCount the new number of firefighters
     */
    private void resetSimulation(double p1, double p2, int fireFighterCount) {
        for (int i = 1; i < SIZE - 1; i++) {
            for (int j = 1; j < SIZE - 1; j++) {
                JUNGLE[i][j].setState(State.EMPTY);
            }
        }
        if (fireFighterCount > 0) {
            useFireFighters = true;
            this.fireFighterCount = fireFighterCount;
        } else {
            useFireFighters = false;
        }

        this.p1 = p1;
        if (p2 > 0) {
            this.p2 = p2 + p1;
            this.twoSpecies = true;
        } else {
            twoSpecies = false;
        }
    }

    /**
     * @param size the size of the board
     * @param p1   the growth probability for species 1
     * @param p2   the growth probability for species 2
     */
    private FireFighterSimulation(int size, double p1, double p2, int fireFighterCount) {
        this(size, p1, fireFighterCount);
        if (p2 > 0) {
            this.p2 = p2 + p1;
            this.twoSpecies = true;
        }
    }

    /**
     * initializes the forest
     */
    private void buildJungle() {
        for (int i = 1; i < SIZE - 1; i++) {
            for (int j = 1; j < SIZE - 1; j++) {
                JUNGLE[i][j] = new Tree(i, j);
                JUNGLE[i][j].setState(State.EMPTY);
            }
        }
        System.out.println("jungle built");
    }

    /**
     * runs the simulation by generating trees, burning them and recording their data
     */
    private void simulate() {

        System.out.format("simulating for %f, %f, %d\n", p1, p2, fireFighterCount);
        terminate = false;
        iteration = 1;
        biomass = 0;
        liveCounter = 0;
        Tree tree;
        onFire.clear();
        onFireCopy.clear();
        //     double temp;
        while (iteration < MAX_ITR) {
            setOnFire();
            biomass += liveCounter;
            //       temp = liveCounter;
            if (terminate) {
                break;
            }
            for (int i = 1; i < SIZE - 1; i++) {
                for (int j = 1; j < SIZE - 1; j++) {
                    tree = JUNGLE[i][j];
                    if (random.nextDouble() < LIGHT_PROB) {
                        // XXX it should be either species one or species 2 check
                        if (tree.getState() == State.SPECIES1 || tree.getState() == State.SPECIES2 || tree.getState() == State.EXTINGUISHED) {
                            tree.setState(State.FIRE);
                            onFire.add(tree);
                            liveCounter--;
                        }
                    }
                    if (tree.getState() == State.EMPTY) {

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
            iteration++;
            //     temp += liveCounter;
            //    temp /= 2.0;
            //   biomass += temp;
            //biomass += liveCounter;
        }
        if (!terminate) {
            setOnFire();
            biomass += liveCounter;
        }
        biomass /= iteration;
    }

    /**
     * sets the trees that are in the fire set to empty and spreads the fire to their neighbors
     */
    private void setOnFire() {
        if (!onFire.isEmpty()) {
            onFireCopy.clear();
            onFireCopy.addAll(onFire);
            onFire.clear();
            for (Tree tree : onFireCopy) {
                burnNeighbours(tree);
                JUNGLE[tree.getRow()][tree.getColumn()].setState(State.EMPTY);
            }

            if (liveCounter < 0) {
                terminate("Negative");
                return;
            }
            if (liveCounter == 0) {
                terminate("ALL TREES BURNT!");
                return;
            }
            if (useFireFighters) {
                if (fireFighterCount >= onFire.size()) {

                    for (Tree anOnFire : onFire) {

                        anOnFire.setState(State.EXTINGUISHED);
                        liveCounter++;
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
                        liveCounter++;
                        treeItr.remove();
                    }
                    System.out.format("%d fires extinguished out of %d!\n", i, fires);
                }
            }
        }
        System.out.println("Live Counter: " + liveCounter);
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

    /**
     * Spreads fire to the neighbor of the burning tree
     *
     * @param tree the tree whose neighbors have to BURN!
     */
    private void burnNeighbours(Tree tree) {
        if (liveCounter == 0) {
            terminate("All burnt!");
        }
        Tree[] neighbours = getNeighbours(tree.getRow(), tree.getColumn());
        Tree neighbour;
        for (int i = 0; i < 8 && neighbours[i] != null; i++) {

            neighbour = neighbours[i];
            if (neighbour.getState() == State.SPECIES1 || neighbour.getState() == State.SPECIES2 || neighbour.getState() == State.EXTINGUISHED) {
                liveCounter--;
                neighbour.setState(State.FIRE);
                onFire.add(neighbour);
            }
        }
    }

    /**
     * finds the neighbors of a tree
     *
     * @param row    of the tree
     * @param column column of the tree
     * @return an array of the neighboring trees
     */
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
    public void run() {
        simulate();
    }

    /**
     * an inner class to only contain data we need for this simulations
     */
    private class Tree {
        private State state;
        private final int row;
        private final int column;

        private Tree(int row, int column) {
            this.row = row;
            this.column = column;
        }

        private int getRow() {

            return row;
        }

        private int getColumn() {

            return column;
        }

        private State getState() {
            return state;
        }

        private void setState(State state) {
            this.state = state;
        }
    }

    public static void main(String[] args) {

        //fireFighterSimulation();
        fitnessLandscapeSimulation();
    }

    /**
     * simulates the effecgt of firefighters for data output
     */
    private static void fireFighterSimulation() {
        Path dir = Paths.get("./fireFighterData");
        BufferedWriter writer = null;
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            double p1 = 0.99;
            double p2 = 0;
            int threadPoolSize = 5;
            Path file = dir.resolve("firefighter" + p1 + p2 + ".csv");
            writer = Files.newBufferedWriter(file);
            FireFighterSimulation[] simulators = new FireFighterSimulation[threadPoolSize];
            for (int i = 0; i < threadPoolSize; i++) {
                simulators[i] = new FireFighterSimulation(250, p1, p2, i * 50);
            }
            for (int i = 200; i <= 1000; ) {
                ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
                for (int j = 0; j < threadPoolSize; j++) {
                    executor.execute(simulators[j]);
                }
                executor.shutdown();
                while (!executor.isTerminated()) {

                }
                FireFighterSimulation individual;
                for (int j = 0; j < threadPoolSize; j++) {
                    individual = simulators[j];
                    writer.write(individual.p1 + "," + individual.p2 + "," + individual.fireFighterCount + "," +
                            individual.biomass + "," + individual.iteration + "\n");
                    i += 50;
                    simulators[j].resetSimulation(p1, p2, i);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void fitnessLandscapeSimulation() {
        Path dir = Paths.get("./fitnessLandScape");
        BufferedWriter writer = null;
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            double p1 = 0.01;
            double increment = 0.01;
            int threadPoolSize = 10;
            Path file = dir.resolve("landscapelow" + p1 + "_" + increment + ".csv");
            writer = Files.newBufferedWriter(file);
            FireFighterSimulation[] simulators = new FireFighterSimulation[threadPoolSize];
            for (int i = 1; i < threadPoolSize + 1; i++) {
                simulators[i - 1] = new FireFighterSimulation(250, increment * i, 0, 0);
            }
            for (int i = 1; i < 11; i++) {
                ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
                for (int j = 0; j < threadPoolSize; j++) {
                    executor.execute(simulators[j]);
                }
                executor.shutdown();
                while (!executor.isTerminated()) {

                }
                FireFighterSimulation individual;
                for (int j = 1; j < threadPoolSize + 1; j++) {
                    individual = simulators[j - 1];
                    writer.write(individual.p1 + ", 0, " + individual.fireFighterCount + "," +
                            individual.biomass + "," + individual.iteration + "\n");

                    simulators[j - 1].resetSimulation(increment * (10 * i + j), 0, 0);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
