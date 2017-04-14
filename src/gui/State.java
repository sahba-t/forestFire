package gui;

import javafx.scene.paint.Color;

/**
 * Created by Sahba on 3/23/2017.
 * Inidicates the state of the tree and facilitates automatic color change
 */
public enum State {
    EMPTY(Color.WHITE), FIRE(Color.ORANGERED), EXTINGUISHED(Color.DARKBLUE), SPECIES1(Color.GREEN), SPECIES2(Color.GREENYELLOW);
    private final Color COLOR;

    State(Color color) {
        COLOR = color;
    }

    public Color getColor() {
        return COLOR;
    }
}
