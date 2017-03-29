package main;

import gui.State;
import gui.Tree;
import javafx.animation.AnimationTimer;

import java.util.Random;


/**
 * Created by Sahba on 3/23/2017.
 */
public class Main extends AnimationTimer
{
  private final Tree[][] JUNGLE;
  private final int SIZE;
  private double p = 0.004;
  private final Random random;

  public Main(Tree[][] jungle, int size)
  {
    JUNGLE = jungle;
    this.SIZE = size;
    random = new Random();

  }


  private void simulate()
  {
    for (int i = 0; i < SIZE; i++)
    {
      for (int j = 0; j < SIZE; j++)
      {
        if (JUNGLE[i][j].getState() == State.EMPTY && random.nextDouble() < p)
        {
          JUNGLE[i][j].setState(State.SPECIES1);
        }
      }
    }
  }

  @Override
  public void handle(long now)
  {
    simulate();
  }
}
