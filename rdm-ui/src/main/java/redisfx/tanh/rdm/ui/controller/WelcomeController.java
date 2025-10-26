package redisfx.tanh.rdm.ui.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redisfx.tanh.rdm.ui.common.Constant;
import redisfx.tanh.rdm.ui.controller.base.BaseWindowController;
import redisfx.tanh.rdm.ui.util.GuiUtil;
import redisfx.tanh.rdm.ui.util.SvgManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 2.3.4
 * @since 2025/9/7 9:41
 */
public class WelcomeController extends BaseWindowController<MainController> implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(WelcomeController.class);

    public StackPane stackPaneNose;
    public StackPane stackPaneFlame;
    public StackPane stackPaneTail;
    public StackPane stackPane0;
    public StackPane stackPane1;
    public GridPane gridPane;
    public Label gitHub;
    public HBox toGithub;
    public Label toStar;
    public HBox rocket;
    private ParallelTransition parallelTransition;
    private Timeline flameAnimation;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initLanguage();
        SvgManager.load(this, gitHub, "/svg/github-mark/github-mark.svg");
        toGithub.setCursor(Cursor.HAND);
        initRocket();


    }

    @Override
    protected void initLanguage() {
        toStar.setText(language("welcome.star"));
    }

    private void initRocket() {
        // 火箭头部（朝左，圆润的形状）
        Path rocketHead = new Path();
        rocketHead.getElements().addAll(
                new MoveTo(-25, -20),
                new CubicCurveTo(-40, -13, -50, -10, -58, -5),
                new CubicCurveTo(-62, -2, -62, 2, -58, 5),
                new CubicCurveTo(-50, 10, -40, 13, -25, 20),
                new ClosePath()
        );
        // 机舱窗口（透明椭圆）
        Ellipse cockpit = new Ellipse(-30, 0, 7, 5);
        cockpit.setFill(Color.DODGERBLUE);
        cockpit.setStroke(null);
        rocketHead.setStroke(null);
        rocketHead.setFill(Color.CRIMSON);
        stackPaneNose.getChildren().addAll(rocketHead, cockpit);

        Rectangle rect0 = new Rectangle(-25, -25, 66, 50);
        rect0.setFill(Color.CRIMSON);
        rect0.setArcHeight(15);
        rect0.setArcWidth(15);
        Text text0 = new Text("Redis");
        text0.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        stackPane0.getChildren().addAll(rect0, text0);

        Rectangle rect1 = new Rectangle(-25, -25, 32, 50);
        rect1.setOnMouseEntered(event -> parallelTransition.play());
        rect1.setArcHeight(15);
        rect1.setArcWidth(15);
        rect1.setFill(Color.CRIMSON);
        Text text = new Text("FX");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 24));


        // 火箭尾部（向右的梯形设计）
        Path rocketTail = new Path();
        rocketTail.getElements().addAll(
                new MoveTo(25, -20),
                new LineTo(35, -15),
                new LineTo(35, 15),
                new LineTo(25, 20),
                new ClosePath()
        );
        rocketTail.setTranslateX(-5);
        // 上尾翼 - 梯形设计
        Path upperFin = new Path();
        upperFin.getElements().addAll(
                new MoveTo(35, -15),
                new LineTo(55, -30),
                new LineTo(50, -15),
                new LineTo(35, -15),
                new ClosePath()
        );
        upperFin.setStroke(null);
        upperFin.setFill(Color.DARKGRAY);
        upperFin.setTranslateY(-30);


// 下尾翼 - 梯形设计（对称）
        Path lowerFin = new Path();
        lowerFin.getElements().addAll(
                new MoveTo(35, 15),
                new LineTo(55, 30),
                new LineTo(50, 15),
                new LineTo(35, 15),
                new ClosePath()
        );
        lowerFin.setStroke(null);
        lowerFin.setFill(Color.DARKGRAY);
        lowerFin.setTranslateY(30);
        rocketTail.setFill(Color.DARKGRAY);
        rocketTail.setStroke(null);
        stackPaneTail.getChildren().addAll(rocketTail, upperFin, lowerFin);

// 主火焰（向右喷射的主火焰）
        Path mainFlame = new Path();
        mainFlame.getElements().addAll(
                new MoveTo(35, -12),
                new QuadCurveTo(50, -8, 65, 0),
                new QuadCurveTo(50, 8, 35, 12),
                new ClosePath()
        );
        mainFlame.setFill(Color.ORANGE);
        mainFlame.setStroke(null);

// 内部火焰（更亮的黄色核心）
        Path innerFlame = new Path();
        innerFlame.getElements().addAll(
                new MoveTo(35, -8),
                new QuadCurveTo(45, -5, 55, 0),
                new QuadCurveTo(45, 5, 35, 8),
                new ClosePath()
        );
        innerFlame.setFill(Color.YELLOW);
        innerFlame.setStroke(null);

// 火焰动画（闪烁效果）
        this.flameAnimation = new Timeline(
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
        stackPaneFlame.getChildren().addAll(mainFlame, innerFlame);
        flameAnimation.play();

        stackPane1.getChildren().addAll(rect1, text);
        FadeTransition fadeTrans = new FadeTransition(Duration.seconds(3), stackPane1);
        fadeTrans.setFromValue(1);
        fadeTrans.setToValue(0.3);
        fadeTrans.setAutoReverse(true);
        FadeTransition fadeTrans2 = new FadeTransition(Duration.seconds(3), stackPane1);
        fadeTrans2.setFromValue(0.3);
        fadeTrans2.setToValue(1);
        fadeTrans2.setAutoReverse(false);

        TranslateTransition translateTran = new TranslateTransition(Duration.seconds(2));
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
                        new KeyValue(rect1.arcWidthProperty(), 20),
                        new KeyValue(rect1.arcHeightProperty(), 20)),
                new KeyFrame(Duration.seconds(5),
                        new KeyValue(rect1.widthProperty(), 50),
                        new KeyValue(rect1.arcWidthProperty(), 20),
                        new KeyValue(rect1.arcHeightProperty(), 20)),
                new KeyFrame(Duration.seconds(8),
                        new KeyValue(rect1.widthProperty(), 32),
                        new KeyValue(rect1.arcWidthProperty(), 15),
                        new KeyValue(rect1.arcHeightProperty(), 15))
        );
        arcAnimation.setAutoReverse(true);
        parallelTransition = new ParallelTransition(stackPane1, fadeTrans,
                translateTran, fadeTrans2, rotateTran, scaleTran, arcAnimation);
        parallelTransition.setCycleCount(1);
        parallelTransition.setAutoReverse(false);
        saveRocketImage();
    }

    @Override
    public void close() {
        super.close();
        if (parallelTransition != null) {
            parallelTransition.stop();
            parallelTransition = null;
        }
        if (flameAnimation != null) {
            flameAnimation.stop();
            flameAnimation = null;
        }
    }
    @FXML
    public void toGithub(MouseEvent mouseEvent) {
        try {
            Desktop.getDesktop().browse(new URI(System.getProperty(Constant.APP_HOME_PAGE)));
        } catch (IOException | URISyntaxException e) {
            log.error("unable to open the browser", e);
            GuiUtil.alert(Alert.AlertType.ERROR, String.format(language("alert.message.help.suggest")+": %s", Constant.APP_HOME_PAGE));
        }
    }

    public void saveRocketImage() {
        // 创建带透明背景的截图参数
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT); // 设置透明背景
        // 设置2倍缩放以获得更高分辨率
        params.setTransform(javafx.scene.transform.Transform.scale(5.0, 5.0));
        // 直接对包含整个火箭的 gridPane 进行截图
        WritableImage image = rocket.snapshot(params, null);

        // 保存到文件
        try {
            File file = new File("rocket.png");
            // 通过 PixelReader 读取数据并创建 BufferedImage
            PixelReader pixelReader = image.getPixelReader();
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
                }
            }

            ImageIO.write(bufferedImage, "png", file);
            System.out.println("Rocket image saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
