package main;

import javafx.scene.paint.Color;

/**
 * Created by Sahba on 3/23/2017.
 */
public enum State
{
  EMPTY("8e8e8e"), FIRE("eb3c3c"), EXTINGUISHED("346fe8"), SPECIES1("19592d"), SPECIES2("09e94e");
  private final String COLOR;

  private State(String color)
  {
    COLOR = color;
  }

  public String getColor()
  {
    return COLOR;
  }
}
