package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GUI extends Application implements Terminable {
    private static final int SIZE = 210;
    private static final double P1 = 0.2;
    private static final double P2 = 0.44;

    //    private double elementWidth;
//    private double elementHeight;
    public GUI() {
        super();

    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        Canvas canvas = new Canvas(SIZE * 5, SIZE * 5);
        GraphicsContext gcx = canvas.getGraphicsContext2D();
        gcx.setStroke(Color.BLACK);
        ScrollPane pane = new ScrollPane(canvas);
        primaryStage.setScene(new Scene(pane));
        //  Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
//        primaryStage.setX(bounds.getMinX());
//        primaryStage.setY(bounds.getMinY());
//        primaryStage.setWidth(bounds.getWidth());
//        primaryStage.setHeight(bounds.getHeight());
//        pane.vvalueProperty().addListener((observable, oldValue, newValue) ->
//        {
//            System.out.println("should shou rows: " + pane.getHeight() / elementHeight);
//            System.out.println("starting from row: " + newValue.doubleValue() * SIZE / pane.getVmax());
//        });

        primaryStage.show();
        new Main(SIZE, P1, P2, this, gcx, 450).start();
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