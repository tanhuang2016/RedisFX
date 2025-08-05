package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SamplerTheme theme = ThemeManager.getInstance().getTheme();
        int fontSize = ThemeManager.getInstance().getFontSize();
        String fontFamily = ThemeManager.getInstance().getFontFamily();
        Map<String, String> colors = null;
        try {
            colors = theme.parseColors();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String c1 = colors.get("-color-bg-subtle");
        pies.setStyle("-fx-background-color:"+c1);
        bar.setStyle("-fx-background-color:"+c1);
        // 设置图表绘图区域背景色
        bar.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent");

// 设置坐标轴区域背景色
        bar.lookup(".axis").setStyle("-fx-background-color: transparent");
        line.setStyle("-fx-background-color:"+c1);


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
        keys.getStyleClass().add("donut-chart");
        keys.setAnimated(true);
        keys.setLabelsVisible(true);
        for (PieChart.Data data1 : keys.getData()) {
            setUpHoverEffectWithTooltip(data1);
        }
        memory.setData(data2);
    }


    private void setUpHoverEffectWithTooltip(PieChart.Data data) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(data.getName() + ": " + data.getPieValue());
//        Tooltip.install(data.getNode(), tooltip);

        data.getNode().setOnMouseEntered(event -> {
            // 放大效果
            data.getNode().setScaleX(1.1);
            data.getNode().setScaleY(1.1);
            // 使用节点自身的填充颜色创建阴影
            Color shadowColor = getRegionBackgroundColor(data);
            data.getNode().setEffect(new DropShadow(10, shadowColor));
            tooltip.show(data.getNode(), event.getScreenX(), event.getScreenY() + 10);
        });

        data.getNode().setOnMouseExited(event -> {
            // 恢复正常大小
            data.getNode().setScaleX(1.0);
            data.getNode().setScaleY(1.0);
            data.getNode().setEffect(null);
            tooltip.hide();
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
