package xyz.hashdog.rdm.ui.controller;

import javafx.animation.*;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;

import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeController extends BaseWindowController<MainController> implements Initializable {


    public StackPane stackPane;
    public StackPane stackPane0;
    private ParallelTransition parallelTransition;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        pane.setPrefSize(400, 200);
//        pane.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
//        pane.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        Rectangle rect0= new Rectangle(-25, -25, 80, 50);
        rect0.setFill(Color.CRIMSON);
        Text text0 = new Text("Redis");
        text0.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        stackPane0.getChildren().addAll(rect0, text0);

        Rectangle rect1= new Rectangle(-25, -25, 50, 50);
        rect1.setOnMouseEntered(event -> {
             parallelTransition.play();
        });
        rect1.setArcHeight(0);
        rect1.setArcWidth(0);
        rect1.setFill(Color.CRIMSON);
        Text text = new Text("FX");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        stackPane.getChildren().addAll(rect1, text);
//        stackPane.setTranslateX(50);
//        stackPane.setTranslateY(75);

//        Rectangle rect = new Rectangle(-25,-25,50, 50);
//        rect.setArcHeight(15);
//        rect.setArcWidth(15);
//        rect.setFill(Color.CRIMSON);
//        stackPane.setTranslateX(50);
//        stackPane.setTranslateY(75);
        // create parallel transition to do all 4 transitions at the same time
        FadeTransition fadeTrans = new FadeTransition(Duration.seconds(3), stackPane);
        fadeTrans.setFromValue(1);
        fadeTrans.setToValue(0.3);
        fadeTrans.setAutoReverse(true);
        FadeTransition fadeTrans2= new FadeTransition(Duration.seconds(3), stackPane);
        fadeTrans2.setFromValue(0.3);
        fadeTrans2.setToValue(1);
        fadeTrans2.setAutoReverse(false);

        TranslateTransition translateTran = new TranslateTransition(Duration.seconds(2));
//        translateTran.setFromX(50);
        translateTran.setToX(320);
        translateTran.setToY(-220);
        translateTran.setCycleCount(2);
        translateTran.setAutoReverse(true);

        RotateTransition rotateTran = new RotateTransition(Duration.seconds(3));
        rotateTran.setByAngle(180);
        rotateTran.setCycleCount(4);
        rotateTran.setAutoReverse(true);


        ScaleTransition scaleTran = new ScaleTransition(Duration.seconds(2));
        scaleTran.setToX(2);
        scaleTran.setToY(2);
        scaleTran.setCycleCount(2);
        scaleTran.setAutoReverse(true);
        Timeline arcAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(rect1.arcWidthProperty(), 0),
                        new KeyValue(rect1.arcHeightProperty(), 0)),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(rect1.arcWidthProperty(), 15),
                        new KeyValue(rect1.arcHeightProperty(), 15)),
                new KeyFrame(Duration.seconds(5),
                        new KeyValue(rect1.arcWidthProperty(), 15),
                        new KeyValue(rect1.arcHeightProperty(), 15)),
                new KeyFrame(Duration.seconds(8),
                        new KeyValue(rect1.arcWidthProperty(), 0),
                        new KeyValue(rect1.arcHeightProperty(), 0))
        );
        arcAnimation.setAutoReverse(true);
        parallelTransition = new ParallelTransition(stackPane, fadeTrans,
                translateTran,fadeTrans2, rotateTran, scaleTran,arcAnimation);
        parallelTransition.setCycleCount(1);
        parallelTransition.setAutoReverse(false);

    }


}
