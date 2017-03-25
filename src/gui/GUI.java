package gui;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GUI extends Application implements Terminable {
    private final int SIZE = 50;
    private final Tree[][] jungle;
    private double elementWidth;
    private double elementHeight;
    private Group[] rows;
    public GUI() {
        super();
        this.jungle = new Tree[SIZE][SIZE];
        buildJungle();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane grid = new GridPane();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid.add(jungle[i][j], j, i, 1, 1);
                if(i>50||j>50){
                    jungle[i][j].setVisible(false);
                }
            }
        }


        ScrollPane pane = new ScrollPane(grid);
        primaryStage.setScene(new Scene(pane));

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        pane.vvalueProperty().addListener((observable, oldValue, newValue) ->
        {
            System.out.println("should shouw rows: " + pane.getHeight() / elementHeight);
            System.out.println("starting from row: " + newValue.doubleValue() * SIZE / pane.getVmax());
        });


        primaryStage.show();
        elementWidth = jungle[0][0].getWidth();
        elementHeight = jungle[0][0].getHeight();
        new Main(jungle, SIZE, this).start();
    }


    private void buildJungle() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                jungle[i][j] = new Tree(i, j);
            }
        }
        System.out.println("jungle built");
    }

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void terminate(String reason) {
        System.out.println(reason);
        System.exit(0);
    }
}