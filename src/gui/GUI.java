package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GUI extends Application implements Terminable {
    private static int SIZE = 250;
    private static double P1 = 0.01;
    private static double P2 = 0.01;
    private static int FIRE_FIGHTERS = 500;
    // private static final double P1 = 0.023332248569399867;
    // private static final double P2 = 0.029612721932376275;

    //    private double elementWidth;
//    private double elementHeight;
    public GUI() {
        super();

    }

    /**
     * starts the GUI. java fx method
     *
     * @param primaryStage the javafx stage
     * @throws Exception if the graphics cannot be initialized
     */
    @Override
    public void start(Stage primaryStage) throws Exception {


        Canvas canvas = new Canvas(SIZE * 5, SIZE * 5);
        GraphicsContext gcx = canvas.getGraphicsContext2D();
        gcx.setStroke(Color.BLACK);
        ScrollPane pane = new ScrollPane(canvas);
        primaryStage.setScene(new Scene(pane));

        primaryStage.show();
        new Main(SIZE, P1, P2, this, gcx, FIRE_FIGHTERS).start();
    }


    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                P1 = Double.parseDouble(args[0]);
                if (args.length > 1) {
                    P2 = Double.parseDouble(args[1]);
                    if (args.length > 2) {
                        FIRE_FIGHTERS = Integer.parseInt(args[2]);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            System.out.println("BAD ARGUMENT!\n please insert p1(float) p2(float) fireFighterCount(int) ");
        }
        launch(args);


    }

    @Override
    public void terminate(String reason) {
        System.out.println(reason);
        System.exit(0);
    }
}