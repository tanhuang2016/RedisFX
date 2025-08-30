package xyz.hashdog.rdm.ui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.*;

public class PopupDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        // 关键：设置为不可聚焦，允许鼠标事件穿透
        primaryStage.setResizable(false);

        // 创建主窗口内容
        Button showPopupButton = new Button("显示透明穿透Popup");
        showPopupButton.setOnAction(e -> {
            showTransparentClickThroughPopup2(primaryStage);
//            showOverlayPopup(primaryStage);
        });

        Button testButton = new Button("测试按钮(用来测试鼠标穿透)");
        testButton.setOnAction(e -> {
            System.out.println("测试按钮被点击了!");
        });

        Tooltip tooltip = new Tooltip("我是提示信息");
        tooltip.setAutoHide(false);
        tooltip.setOpacity(0.8);
        Tooltip.install(testButton, tooltip);

        VBox root = new VBox(10);
        root.getChildren().addAll(showPopupButton, testButton);
        root.setStyle("-fx-padding: 20px;");

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Popup Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
private void showOverlayPopup(Window owner) {
    Stage overlayStage = new Stage();
    overlayStage.initOwner(owner);
    overlayStage.initStyle(StageStyle.TRANSPARENT);
    overlayStage.setAlwaysOnTop(true);
    overlayStage.setResizable(false);
    // 创建全屏遮罩但大部分区域透明
    StackPane overlayRoot = new StackPane();
    overlayRoot.setStyle("-fx-background-color: transparent;");

    // 只在需要显示的地方添加内容
    Label messageLabel = new Label("提示信息");
    messageLabel.setStyle(
        "-fx-background-color: transparent;" +
        "-fx-text-fill: white;" +
        "-fx-padding: 10px;"
    );

    // 使用 StackPane 的对齐功能定位内容
    StackPane.setAlignment(messageLabel, Pos.CENTER);
    overlayRoot.getChildren().add(messageLabel);

    // 设置为全屏大小
    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
    Scene overlayScene = new Scene(overlayRoot, screenBounds.getWidth(), screenBounds.getHeight(), Color.TRANSPARENT);

    // 关键：设置鼠标事件穿透
    overlayRoot.setMouseTransparent(true);
    messageLabel.setMouseTransparent(true);
//    overlayScene.setMouseTransparent(true);

    overlayStage.setScene(overlayScene);
    overlayStage.setX(0);
    overlayStage.setY(0);
    overlayStage.show();
}
private void showTransparentClickThroughPopup2(Window owner) {
    // 使用 Stage 替代 Popup
    Stage overlayStage = new Stage();

    // 可选：设置 owner（会随父窗口最小化而隐藏）或不设置（完全独立）
    if (owner != null) {
//        overlayStage.initOwner(owner);
    }

    overlayStage.initStyle(StageStyle.TRANSPARENT);
    overlayStage.setAlwaysOnTop(true);
    overlayStage.setResizable(false);

    Label label = new Label("我是透明且鼠标可穿透的Popup!\n鼠标可以点击到我后面的内容");
    label.setStyle(
        "-fx-background-color: transparent;" +
        "-fx-text-fill: white;"
    );
    label.setMouseTransparent(true);

    StackPane root = new StackPane(label);
    root.setMouseTransparent(true);
    root.setStyle("-fx-background-color: transparent;");

    Scene scene = new Scene(root, 300, 100, Color.TRANSPARENT);
    overlayStage.setScene(scene);

    // 定位到屏幕中央
    javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
    double x = screen.getVisualBounds().getWidth() / 2 - 150;
    double y = screen.getVisualBounds().getHeight() / 2 - 50;

    overlayStage.setX(x);
    overlayStage.setY(y);

    overlayStage.show();
}

    /**
     * 创建并显示一个具有鼠标穿透功能的透明popup
     * @param owner 父窗口
     */
    private void showTransparentClickThroughPopup(Window owner) {
        // 创建popup内容
        Label label = new Label("我是透明且鼠标可穿透的Popup!\n鼠标可以点击到我后面的内容");
        label.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;"
//            "-fx-padding: 20px;" +
//            "-fx-border-color: white;" +
//            "-fx-border-width: 2px;"
        );
        label.setOpacity(0.5);
        // 创建popup并添加内容
        Popup popup = new Popup();
        popup.getContent().add(label);

        // 设置内容鼠标穿透
        label.setMouseTransparent(true);
//        popup.setMouseTransparent(true);

        popup.setAutoHide(false);
        popup.setHideOnEscape(false);

        // 在屏幕中央显示popup
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        double x = screen.getVisualBounds().getWidth() / 2 - 150;
        double y = screen.getVisualBounds().getHeight() / 2 - 50;

        popup.show(owner, x, y);

//        // 5秒后自动关闭popup
//        new Thread(() -> {
//            try {
//                Thread.sleep(5000);
//                javafx.application.Platform.runLater(popup::hide);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }
}
