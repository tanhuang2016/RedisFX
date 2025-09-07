package xyz.hashdog.rdm.ui.controller;

import javafx.animation.*;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;

import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeController extends BaseWindowController<MainController> implements Initializable {


    public StackPane stackPaneNose;
    public StackPane stackPaneFlame;
    public StackPane stackPaneTail;
    public StackPane stackPane;
    public StackPane stackPane0;
    public GridPane gridPane;
    private ParallelTransition parallelTransition;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 或者使用

//        pane.setPrefSize(400, 200);
//        pane.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
//        pane.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        // 火箭头部（朝左，圆润的形状）

        Path rocketHead = new Path();
        rocketHead.getElements().addAll(
                new MoveTo(-25, -20),                                     // 起始点
                new CubicCurveTo(-40, -13, -50, -10, -58, -5),           // 上部曲线的第一段
                new CubicCurveTo(-62, -2, -62, 2, -58, 5),               // 头部最尖端的曲线
                new CubicCurveTo(-50, 10, -40, 13, -25, 20),             // 下部曲线
                new ClosePath()
        );
        // 机舱窗口（透明椭圆）
        Ellipse cockpit = new  Ellipse(-30, 0, 7, 5);
        cockpit.setFill(Color.DODGERBLUE);
        cockpit.setStroke(null);
        rocketHead.setStroke(null);  // 去掉边框
        rocketHead.setFill(Color.CRIMSON);
        stackPaneNose.getChildren().addAll(rocketHead,cockpit);

        Rectangle rect0= new Rectangle(-25, -25, 66, 50);
        rect0.setFill(Color.CRIMSON);
        rect0.setArcHeight(15);
        rect0.setArcWidth(15);
        Text text0 = new Text("Redis");
        text0.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        stackPane0.getChildren().addAll(rect0, text0);

        Rectangle rect1= new Rectangle(-25, -25, 32, 50);
        rect1.setOnMouseEntered(event -> {
             parallelTransition.play();
        });
        rect1.setArcHeight(15);
        rect1.setArcWidth(15);
        rect1.setFill(Color.CRIMSON);
        Text text = new Text("FX");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // 火焰（在火箭尾部，向右喷射）
        Polygon flame = new Polygon(
                -25, -10,   // 火箭左侧顶部
                -35, 0,     // 火箭左侧中间（向外喷射）
                -25, 10     // 火箭左侧底部
        );
        // 火箭尾部（向右的梯形设计）
        Path rocketTail = new Path();
        rocketTail.getElements().addAll(
                new MoveTo(25, -20),        // 右侧上部
                new LineTo(35, -15),        // 向右延伸的上部
                new LineTo(35, 15),         // 右侧下部
                new LineTo(25, 20),         // 回到火箭主体
                new ClosePath()
        );
        rocketTail.setTranslateX(-5);
        // 上尾翼 - 梯形设计
        Path upperFin = new Path();
        upperFin.getElements().addAll(
                new MoveTo(35, -15),    // 与火箭尾部连接
                new LineTo(55, -30),    // 尾翼外边缘上点
                new LineTo(50, -15),    // 尾翼外边缘下点
                new LineTo(35, -15),    // 回到连接点
                new ClosePath()
        );
        upperFin.setStroke(null);
        upperFin.setFill(Color.DARKGRAY);
        upperFin.setTranslateY(-30);


// 下尾翼 - 梯形设计（对称）
        Path lowerFin = new Path();
        lowerFin.getElements().addAll(
                new MoveTo(35, 15),     // 与火箭尾部连接
                new LineTo(55, 30),     // 尾翼外边缘下点
                new LineTo(50, 15),     // 尾翼外边缘上点
                new LineTo(35, 15),     // 回到连接点
                new ClosePath()
        );
        lowerFin.setStroke(null);
        lowerFin.setFill(Color.DARKGRAY);
        lowerFin.setTranslateY(30);
        rocketTail.setFill(Color.DARKGRAY);
        rocketTail.setStroke(null);
        stackPaneTail.getChildren().addAll(rocketTail, upperFin,lowerFin);

// 主火焰（向右喷射的主火焰）
        Path mainFlame = new Path();
        mainFlame.getElements().addAll(
                new MoveTo(35, -12),                    // 火焰起点（尾部右侧上部）
                new QuadCurveTo(50, -8, 65, 0),         // 上部火焰曲线（向右喷射）
                new QuadCurveTo(50, 8, 35, 12),         // 下部火焰曲线
                new ClosePath()                         // 闭合路径
        );
        mainFlame.setFill(Color.ORANGE);
        mainFlame.setStroke(null);

// 内部火焰（更亮的黄色核心）
        Path innerFlame = new Path();
        innerFlame.getElements().addAll(
                new MoveTo(35, -8),                     // 内部火焰起点
                new QuadCurveTo(45, -5, 55, 0),         // 上部内部火焰
                new QuadCurveTo(45, 5, 35, 8),          // 下部内部火焰
                new ClosePath()                         // 闭合路径
        );
        innerFlame.setFill(Color.YELLOW);
        innerFlame.setStroke(null);

// 火焰动画（闪烁效果）
        Timeline flameAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(mainFlame.scaleXProperty(), 1),
                        new KeyValue(mainFlame.scaleYProperty(), 1),
                        new KeyValue(innerFlame.scaleXProperty(), 1),
                        new KeyValue(innerFlame.scaleYProperty(), 1)
                ),
                new KeyFrame(Duration.millis(100),
                        new KeyValue(mainFlame.scaleXProperty(), 1.1),
                        new KeyValue(mainFlame.scaleYProperty(), 1.05),
                        new KeyValue(innerFlame.scaleXProperty(), 1.1),
                        new KeyValue(innerFlame.scaleYProperty(), 1.05)
                ),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(mainFlame.scaleXProperty(), 1),
                        new KeyValue(mainFlame.scaleYProperty(), 1),
                        new KeyValue(innerFlame.scaleXProperty(), 1),
                        new KeyValue(innerFlame.scaleYProperty(), 1)
                )
        );
        flameAnimation.setCycleCount(Animation.INDEFINITE);
        stackPaneFlame.getChildren().addAll( mainFlame, innerFlame);
        flameAnimation.play();

//        stackPaneFlame.getChildren().addAll(flame);

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
                        new KeyValue(rect1.widthProperty(), 32),
                        new KeyValue(rect1.arcWidthProperty(), 15),
                        new KeyValue(rect1.arcHeightProperty(), 15)),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(rect1.widthProperty(), 50),
                        new KeyValue(rect1.arcWidthProperty(), 15),
                        new KeyValue(rect1.arcHeightProperty(), 15)),
                new KeyFrame(Duration.seconds(5),
                        new KeyValue(rect1.widthProperty(), 50),
                        new KeyValue(rect1.arcWidthProperty(), 15),
                        new KeyValue(rect1.arcHeightProperty(), 15)),
                new KeyFrame(Duration.seconds(8),
                        new KeyValue(rect1.widthProperty(), 32),
                        new KeyValue(rect1.arcWidthProperty(), 15),
                        new KeyValue(rect1.arcHeightProperty(), 15))
        );
        arcAnimation.setAutoReverse(true);
        parallelTransition = new ParallelTransition(stackPane, fadeTrans,
                translateTran,fadeTrans2, rotateTran, scaleTran,arcAnimation);
        parallelTransition.setCycleCount(1);
        parallelTransition.setAutoReverse(false);

    }


}
