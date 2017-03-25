package gui;

import javafx.animation.AnimationTimer;

import java.util.HashSet;
import java.util.Random;


/**
 * Created by Sahba on 3/23/2017.
 */
class Main extends AnimationTimer {
    private final Tree[][] JUNGLE;
    private final int SIZE;
    private double p = 0.8;
    private final double LIGHT_PROB = 0.001;
    private final Random random;
    private final HashSet<Pair> onFire;
    private final HashSet<Pair> onFireCopy;
    private final int[][] neighbours;
    private int liveCounter;
    private final Terminatable terminatable;
    private long preiousTime;

    Main(Tree[][] jungle, int size, Terminatable terminatable) {
        JUNGLE = jungle;
        this.SIZE = size;
        random = new Random();
        onFire = new HashSet<>();
        onFireCopy = new HashSet<>();
        neighbours = new int[][]{{-1, 1}, {-1, -1}, {-1, 0}, {1, 1}, {1, -1}, {1, 0}, {0, 1}, {0, -1}};
        this.terminatable = terminatable;
    }


    private void simulate() {

        setonFire();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {

                if (random.nextDouble() < p && JUNGLE[i][j].getState() == State.EMPTY) {
                    JUNGLE[i][j].setState(State.SPECIES1);
                    liveCounter++;
                }
                if (random.nextDouble() < LIGHT_PROB) {
                    // XXX it should be either species one or species 2 check
                    if (JUNGLE[i][j].getState() == State.SPECIES1 || JUNGLE[i][j].getState() == State.SPECIES2) {
                        JUNGLE[i][j].setState(State.FIRE);
                        onFire.add(new Pair(i, j));
                        liveCounter--;
                    }
                }
            }
        }
    }

    private void setonFire() {
        if (!onFire.isEmpty()) {
            onFireCopy.clear();
            onFireCopy.addAll(onFire);
            onFire.clear();
            for (Pair p : onFireCopy) {
                if (p.row < 0 || p.row >= SIZE || p.column < 0 || p.column >= SIZE) {
                    continue;
                }
                burnNeighbours(p.row, p.column);
                JUNGLE[p.row][p.column].setState(State.EMPTY);
            }
            System.out.println("Live Counter: " + liveCounter);
            if (liveCounter < 0) {
                terminatable.terminate("Negative");
            }
            if (liveCounter == 0) {
                terminatable.terminate("ALL TREES BURNT!");
            }
        }
    }

    private void burnNeighbours(int row, int column) {
        if (liveCounter == 0) {
            terminatable.terminate("All burnt!");
        }
        int nRow;
        int nColumn;
        for (Pair pair : getNeighbours(row, column)) {
            nRow = pair.row;
            nColumn = pair.column;
            if (nRow < 0 || nRow >= SIZE || nColumn < 0 || nColumn >= SIZE) {
                continue;
            }
            if (JUNGLE[nRow][nColumn].getState() == State.SPECIES1 || JUNGLE[nRow][nColumn].getState() == State.SPECIES2) {
                liveCounter--;
                JUNGLE[nRow][nColumn].setState(State.FIRE);
                onFire.add(new Pair(nRow, nColumn));
            }
        }
    }

    private Pair[] getNeighbours(int row, int column) {
        Pair[] neighbourTrees = new Pair[8];
        for (int i = 0; i < neighbours.length; i++) {
            neighbourTrees[i] = new Pair(row + neighbours[i][0], column + neighbours[i][1]);
        }
        return neighbourTrees;
    }

    @Override
    public void handle(long now) {
        if (preiousTime == 0) {
            preiousTime = now;
        } else {

            System.out.format("%.5f\n", (now - preiousTime) / 1_000_000_000.00);
            preiousTime = now;
        }
        simulate();
    }


    private class Pair {
        private final int row;
        private final int column;

        private Pair(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }
}