package main;

import javafx.scene.control.Button;

/**
 * Created by Sahba on 3/23/2017.
 */
public class Tree extends Button
{
  private final int row;
  private final int column;
  private State state;

  public Tree(int row, int column)
  {
    super();
    this.row = row;
    this.column = column;
    setState(State.EMPTY);
  }

  public void setState(State state)
  {
    this.state = state;
    setStyle(" -fx-base:#" + state.getColor());
  }

  public State getState()
  {
    return state;
  }
}
