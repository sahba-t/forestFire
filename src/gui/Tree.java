package gui;

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

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Tree tree = (Tree) o;

    if (row != tree.row) return false;
    return column == tree.column;

  }

  public int getRow()
  {
    return row;
  }

  public int getColumn()
  {
    return column;
  }

  @Override
  public int hashCode()
  {
    int result = row;
    result = 31 * result + column;
    return result;
  }
}
