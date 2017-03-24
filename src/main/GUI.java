package main;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GUI extends Application
{
  private static boolean showGUI;
  private final int SIZE = 50;
  private final Tree[][] jungle;


  public GUI()
  {
    super();
    this.jungle = new Tree[SIZE][SIZE];
    buildJungle();
  }

  @Override
  public void start(Stage primaryStage) throws Exception
  {
    GridPane grid = new GridPane();
    for (int i = 0; i < SIZE; i++)
    {
      for (int j = 0; j < SIZE; j++)
      {
        grid.add(jungle[i][j], j, i, 1, 1);
      }
    }


    ScrollPane pane = new ScrollPane(grid);
    primaryStage.setScene(new Scene(pane));
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    primaryStage.setX(bounds.getMinX());
    primaryStage.setY(bounds.getMinY());
    primaryStage.setWidth(bounds.getWidth());
    primaryStage.setHeight(bounds.getHeight());

    primaryStage.show();
    new Main(jungle, SIZE).start();
  }


  private void buildJungle()
  {
    for (int i = 0; i < SIZE; i++)
    {
      for (int j = 0; j < SIZE; j++)
      {
        jungle[i][j] = new Tree(i, j);
      }
    }
    System.out.println("jungle built");
  }

  public static void main(String[] args)
  {

    launch(args);
  }
}