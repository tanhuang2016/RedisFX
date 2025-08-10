package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import xyz.hashdog.rdm.ui.entity.HashTypeTable;
import xyz.hashdog.rdm.ui.entity.InfoTable;
import xyz.hashdog.rdm.ui.entity.TopKeyTable;
import xyz.hashdog.rdm.ui.sampler.event.DefaultEventBus;
import xyz.hashdog.rdm.ui.sampler.event.ThemeEvent;
import xyz.hashdog.rdm.ui.sampler.theme.SamplerTheme;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.sampler.page.Page.FAKER;

public class ReportController extends BaseKeyController<ServerTabController> implements Initializable {
    public PieChart memory;
    public PieChart keys;
    public HBox pies;
    public HBox lines;

    public HBox topTables;

    public TitledPane serverInfo;
    public TitledPane memoryInfo;
    public TitledPane statusInfo;
    public LineChart lineKey;
    public LineChart lineMemory;
    public TableView<TopKeyTable> topTable;
    public Label top;
    public Label pie;
    public ToggleButton keySize;
    public ToggleButton keyLength;
    public Label trend;
    public HBox infoTables;
    public Label info;
    public CustomTextField findTextField;
    public Button findButton;
    public TableView<InfoTable> infoTable;
    public ModalPane modalPane;
    public HBox topDialog;
    public HBox topDialogContent;
    public TextField rate;
    public ToggleSwitch floatToggleSwitch;
    public Label barCpu;
    public Label barNet;
    public Label barMemory;
    public Label barKey;
    public Label barConnection;
    public Label barRefresh;
    public ScrollPane scrollPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyTheme();
        initStyle();
        initFontIcon();
        initTextField();
        initModel();
        inintListener();
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


        topTable.getStyleClass().addAll(Tweaks.EDGE_TO_EDGE,Styles.STRIPED);
        infoTable.getStyleClass().addAll(Tweaks.EDGE_TO_EDGE,Styles.STRIPED);
        Platform.runLater(() -> {
            GuiUtil.initSimpleTableView(topTable,new TopKeyTable());
            topTable.getItems().addAll(
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5"),
                    new TopKeyTable("1","2","3","4","5")
            );

            GuiUtil.initSimpleTableView(infoTable,new InfoTable());
            infoTable.getItems().addAll(
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","1","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3"),
                    new InfoTable("1","2","3")
            );
            GuiUtil.adjustTableViewHeightPrecise(infoTable);
            topTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            );
            infoTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            );


        });



    }

    private void inintListener() {
        initFloatToggleSwitchListener();
        initScrollListener();
    }
    private void initScrollListener() {
        Platform.runLater(() -> {
            if (scrollPane != null) {
                // 监听垂直滚动属性变化
                scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                    handleScrollPercentageEvent(oldValue.doubleValue(), newValue.doubleValue());
                });
            }
        });
    }

    private void handleScrollPercentageEvent(double oldValue, double newValue) {
        final double THRESHOLD = 0.05;
        // 向下滚动超过20%
        if (newValue > THRESHOLD ) {
            if(floatToggleSwitch.isSelected()){
                modalPane.show(topDialog);
            }
        }else {
            if(floatToggleSwitch.isSelected()){
                modalPane.hide();
            }
        }
    }

    private void initFloatToggleSwitchListener() {
        floatToggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                //这里其实没用了，后面可以删除 todo，现在靠滚动条监听，判断是否显示
                modalPane.hide();
            }
        });
    }

    public void initModel() {
        topDialog.setOpacity(1);
        modalPane.setAlignment(Pos.TOP_CENTER);
        modalPane.usePredefinedTransitionFactories(Side.TOP);
        // 或者设置遮罩层不阻塞鼠标事件
        modalPane.setMouseTransparent(true);
//        modalPane.show(topDialog);
    }


    private void initTextField() {
        findTextField.setRight(findButton);
    }

    private void initFontIcon() {
        serverInfo.setGraphic(new FontIcon( Feather.SERVER));
        memoryInfo.setGraphic(new FontIcon( Material2MZ.STORAGE ));
        statusInfo.setGraphic(new FontIcon( Feather.ACTIVITY));
        top.setGraphic(new FontIcon(Material2MZ.SORT));
        info.setGraphic(new FontIcon(Feather.INFO));
        pie.setGraphic(new FontIcon(Feather.PIE_CHART ));
        trend.setGraphic(new FontIcon(Feather.TRENDING_UP ));
        findButton.setGraphic(new FontIcon(Feather.SEARCH));
        barCpu.setGraphic(new FontIcon(Feather.PERCENT));
        barNet.setGraphic(new FontIcon(Material2MZ.WIFI ));
        barMemory.setGraphic(new FontIcon(Material2MZ.MEMORY ));
        barKey.setGraphic(new FontIcon(Feather.KEY ));
        barConnection.setGraphic(new FontIcon(Feather.LINK ));
        barRefresh.setGraphic(new FontIcon(Material2MZ.REFRESH ));



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
        initLineBarStyle();
        initCardInfoStyle();
        initButtonStyle();
        ToggleGroup toggleGroup = new ToggleGroup();
        keySize.setToggleGroup(toggleGroup);
        keyLength.setToggleGroup(toggleGroup);


    }

    private void initButtonStyle() {
        findButton.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT,Styles.ROUNDED,Styles.SMALL);
        findButton.setCursor(Cursor.HAND);
    }

    private void initLineBarStyle() {
        initLineBarStyle(lineKey,lineMemory);
    }

    private void initLineBarStyle(Node... bars) {
        for (Node bar : bars) {
            // 设置图表绘图区域背景色
            bar.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent");
            // 设置坐标轴区域背景色
            bar.lookup(".axis").setStyle("-fx-background-color: transparent");
        }

    }

    private void initCardInfoStyle() {
        Platform.runLater(()->initCardInfoStyle(serverInfo,memoryInfo,statusInfo));
    }

    private void initCardInfoStyle(TitledPane... infos) {
        for (TitledPane info : infos) {
            info.lookup(".title").setStyle("-fx-background-color: transparent;");
            info.lookup(".content").setStyle(""" 
                                    -fx-background-color: transparent;
                                    -fx-border-width: 0;
                                    -fx-background-insets: 0;
                                    """);
        }
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
        String c2 = colors.get("-color-bg-default");
        pies.setStyle("-fx-background-color:"+c1);
        lines.setStyle("-fx-background-color:"+c1);
        topTables.setStyle("-fx-background-color:"+c1);
        infoTables.setStyle("-fx-background-color:"+c1);
        serverInfo.setStyle("-fx-background-color:"+c1);
        memoryInfo.setStyle("-fx-background-color:"+c1);
        statusInfo.setStyle("-fx-background-color:"+c1);
        topDialogContent.setStyle("-fx-background-color:"+GuiUtil.hexToRgba(c2,0.8));
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

    public void find(ActionEvent actionEvent) {
    }
}
