package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.Card;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.ui.sampler.event.DefaultEventBus;
import xyz.hashdog.rdm.ui.sampler.event.ThemeEvent;
import xyz.hashdog.rdm.ui.sampler.theme.SamplerTheme;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.sampler.page.Page.FAKER;

public class ReportController extends BaseKeyController<ServerTabController> implements Initializable {
    public PieChart memory;
    public PieChart keys;
    public HBox pies;
    public BarChart bar;
    public LineChart line;
    public VBox cardVbox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyTheme();
        initStyle();
        DefaultEventBus.getInstance().subscribe(ThemeEvent.class, e -> {
            applyTheme();
        });



        final var rnd = FAKER.random();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30))
        );
        ObservableList<PieChart.Data> data2 = FXCollections.observableArrayList(
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30))
        );
        keys.setData(data);
        memory.setData(data2);
        dataHover();
    }

    private void dataHover() {
        for (PieChart.Data data1 : keys.getData()) {
            setUpHoverEffectWithTooltip(data1);
        }
        for (PieChart.Data data1 : memory.getData()) {
            setUpHoverEffectWithTooltip(data1);
        }
    }

    private void initStyle() {
        // 设置图表绘图区域背景色
        bar.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent");
        // 设置坐标轴区域背景色
        bar.lookup(".axis").setStyle("-fx-background-color: transparent");
        line.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent");
        // 设置坐标轴区域背景色
        line.lookup(".axis").setStyle("-fx-background-color: transparent");
        keys.setAnimated(true);
        keys.setLabelsVisible(true);
        memory.setAnimated(true);
        memory.setLabelsVisible(true);

        Platform.runLater(() -> {
            for (Node child : cardVbox.getChildren()) {
                if(child instanceof HBox hBox){
                    for (Node hBoxChild : hBox.getChildren()) {
                        if(hBoxChild instanceof TitledPane titledPane){
                            titledPane.setGraphic(new FontIcon(Feather.MONITOR));
                            titledPane.lookup(".title").setStyle("-fx-background-color: transparent;");
                            titledPane.lookup(".content").setStyle(""" 
                                    -fx-background-color: transparent;
                                    -fx-border-width: 0;
                                    -fx-background-insets: 0;
                                    """);
                        }
                    }
                }
            }
        });

    }

    private void applyTheme() {
        SamplerTheme theme = ThemeManager.getInstance().getTheme();
        Map<String, String> colors = null;
        try {
            colors = theme.parseColors();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String c1 = colors.get("-color-bg-subtle");
        line.setStyle("-fx-background-color:"+c1);
        pies.setStyle("-fx-background-color:"+c1);
        bar.setStyle("-fx-background-color:"+c1);
        for (Node child : cardVbox.getChildren()) {
            if(child instanceof HBox hBox){
                hBox.getChildren().forEach(e->e.setStyle("-fx-background-color:"+c1));
            }
        }
    }


    private void setUpHoverEffectWithTooltip(PieChart.Data data) {
        Popup popup = new Popup();
        Label popupContent = new Label(data.getName() + ": " + String.format("%.1f", data.getPieValue()));
        popupContent.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.8);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5px 10px;" +
                        "-fx-background-radius: 4px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);"
        );

        // 关键设置：使 Popup 内容对鼠标事件透明
        popupContent.setMouseTransparent(true);
        popup.getContent().add(popupContent);

        // 关键设置：使整个 Popup 对鼠标事件透明
        popup.setAutoFix(true);
        popup.setAutoHide(false); // 不自动隐藏，由我们手动控制
        popup.setHideOnEscape(true);
        data.getNode().setOnMouseEntered(event -> {
            System.out.println("Hovered: " + data.getName() + " - " + data.getPieValue());
            // 放大效果
            data.getNode().setScaleX(1.1);
            data.getNode().setScaleY(1.1);
            // 使用节点自身的填充颜色创建阴影
            Color shadowColor = getRegionBackgroundColor(data);
            data.getNode().setEffect(new DropShadow(10, shadowColor));
            popup.show(data.getNode(), event.getScreenX()+5, event.getScreenY() + 5);
        });

        data.getNode().setOnMouseExited(event -> {
            // 恢复正常大小
            data.getNode().setScaleX(1.0);
            data.getNode().setScaleY(1.0);
            data.getNode().setEffect(null);
            popup.hide();
        });
        data.getNode().setOnMouseMoved(event -> {
            // 动态更新位置，保持与鼠标的距离
            if (popup.isShowing()) {
                popup.setAnchorX(event.getScreenX() + 5);
                popup.setAnchorY(event.getScreenY() + 5);
            }
        });
        data.getNode().setOnMouseClicked(event -> {
            System.out.println("Clicked: " + data.getName() + " - " + data.getPieValue());
        });
    }
    private Color getRegionBackgroundColor(PieChart.Data data) {
        // JavaFX PieChart 使用 CSS 样式来设置颜色
        // 默认颜色序列
        Color[] defaultColors = {
                Color.web("#f3622d"), // 橙色
                Color.web("#fba71b"), // 黄色
                Color.web("#57b757"), // 绿色
                Color.web("#41a9c9"), // 蓝色
                Color.web("#4258c9"), // 深蓝色
                Color.web("#9a42c8"), // 紫色
                Color.web("#c84164"), // 粉色
                Color.web("#888888")  // 灰色
        };

        // 获取数据在数据集中的索引
        PieChart chart = data.getChart();
        if (chart != null) {
            int index = chart.getData().indexOf(data);
            if (index >= 0) {
                return defaultColors[index % defaultColors.length];
            }
        }

        // 默认返回第一个颜色
        return defaultColors[0];
    }

}
