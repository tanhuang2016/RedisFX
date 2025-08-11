package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Popover;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.redis.client.RedisMonitor;
import xyz.hashdog.rdm.ui.common.Constant;
import xyz.hashdog.rdm.ui.controller.popover.RefreshPopover;
import xyz.hashdog.rdm.ui.entity.HashTypeTable;
import xyz.hashdog.rdm.ui.entity.InfoTable;
import xyz.hashdog.rdm.ui.entity.TopKeyTable;
import xyz.hashdog.rdm.ui.sampler.event.DefaultEventBus;
import xyz.hashdog.rdm.ui.sampler.event.ThemeEvent;
import xyz.hashdog.rdm.ui.sampler.theme.SamplerTheme;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.Util;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static xyz.hashdog.rdm.ui.sampler.page.Page.FAKER;

public class ReportController extends BaseKeyController<ServerTabController> implements RefreshPopover.IRefreshPopover,Initializable {
    public PieChart memory;
    public PieChart keys;
    public HBox pies;
    public HBox lines;

    public HBox topTables;

    public TitledPane serverInfo;
    public TitledPane memoryInfo;
    public TitledPane statusInfo;
    public LineChart<String, Number> lineKey;
    public AreaChart<String, Number> lineMemory;
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
    public ToggleSwitch floatToggleSwitch;
    public Label barCpu;
    public Label barNet;
    public Label barMemory;
    public Label barKey;
    public Label barConnection;
    public Button barRefresh;
    public ScrollPane scrollPane;
    public HBox trendHBox;
    public Label redisVersion;
    public Label os;
    public Label processId;
    public Label usedMemory;
    public Label usedMemoryPeak;
    public Label usedMemoryLua;
    public Label connectedClients;
    public Label totalConnectionsReceived;
    public Label totalCommandsProcessed;
    public Label capsuleCpu;
    public Label capsuleNet;
    public Label capsuleMemory;
    public Label capsuleKey;
    public Label capsuleConnection;
    private Popover refreshPopover;
    private XYChart.Series<String, Number> memorySeries;
    private Timeline memoryUpdateTimeline;
    private int maxDataPoints = 10;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyTheme();
        initStyle();
        initFontIcon();
        initTextField();
        initLabel();
        initModel();
        inintListener();
        initRefreshPopover();

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
            topTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            );
            infoTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            );


        });






        var x = new CategoryAxis();
        x.setLabel("Month");

        var y = new NumberAxis(0, 80, 10);
        y.setLabel("Value");

        var series1 = new XYChart.Series<String, Number>();
        series1.setName(FAKER.stock().nsdqSymbol());
        IntStream.range(1, 12).forEach(i -> series1.getData().add(
                new XYChart.Data<>(
                        Month.of(i).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        rnd.nextInt(10, 80)
                )
        ));

        memorySeries = new XYChart.Series<String, Number>();
//        series2.setName(FAKER.stock().nsdqSymbol());
//        IntStream.range(1, 120).forEach(i -> series2.getData().add(
//                new XYChart.Data<>(
//                        LocalTime.now().toString(),
//                        rnd.nextInt(10, 80)
//                )
//        ));

        lineKey.setTitle("Stock Monitoring");
        lineKey.setMinHeight(300);
        lineKey.getData().addAll(series1);
        lineKey.setLegendVisible(false);
        lineMemory.setTitle("Stock Monitoring");
        lineMemory.setMinHeight(300);
        lineMemory.getData().addAll(memorySeries);
        lineMemory.setLegendVisible(false);
        NumberAxis yAxis = (NumberAxis)lineMemory.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1000);
        yAxis.setTickUnit(50);
        // 设置X轴样式
        CategoryAxis xAxis = (CategoryAxis) lineMemory.getXAxis();
        xAxis.setAnimated(false); // 关闭X轴动画，避免跳动
        xAxis.tickLabelRotationProperty().set(-45); // 旋转标签避免重叠
        xAxis.setStartMargin(-28);
        xAxis.setEndMargin(-28);
        dataHover();
        // 启动定时更新
        startMemoryMonitoring();

    }

    private void initLabel() {
        capsuleCpu.textProperty().bind(barCpu.textProperty());
        capsuleNet.textProperty().bind(barNet.textProperty());
        capsuleMemory.textProperty().bind(barMemory.textProperty());
        capsuleKey.textProperty().bind(barKey.textProperty());
        capsuleConnection.textProperty().bind(barConnection.textProperty());
    }

    // 启动内存监控
    private void startMemoryMonitoring() {
        if (memoryUpdateTimeline != null) {
            memoryUpdateTimeline.stop();
        }

        memoryUpdateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> updateMemoryData())
        );
        memoryUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        memoryUpdateTimeline.play();
    }

    // 更新内存数据
    private void updateMemoryData() {
        // 这里应该从Redis获取实际的内存数据
        // 模拟数据：
        int memoryValue = getRedisMemoryUsage(); // 你需要实现这个方法

        // 获取当前时间
        String timeLabel = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // 添加新数据点
        XYChart.Data<String, Number> newData = new XYChart.Data<>(timeLabel, memoryValue);
        memorySeries.getData().add(newData);

        // 为新数据点添加交互效果
        Platform.runLater(() -> {
            addDataPointInteraction(newData);
        });

        // 限制数据点数量，避免图表过于拥挤
        if (memorySeries.getData().size() > maxDataPoints) {
            memorySeries.getData().remove(0);
        }

        // 自动滚动X轴（如果需要的话）
        // 这里可以调整X轴显示范围
    }


    // 获取Redis内存使用量的实际实现
    private int getRedisMemoryUsage() {
        // 你需要根据实际的Redis连接来获取内存信息
        // 示例：
        try {
            // 假设你有Redis连接
            // String info = redisClient.info("memory");
            // 从info中解析出used_memory或used_memory_rss的值

            // 临时返回模拟数据
            Random random = new Random();
            return 100 + random.nextInt(900); // 模拟100-1000MB的内存使用
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void inintListener() {
        initFloatToggleSwitchListener();
        initScrollListener();
        super.parameter.addListener((observable, oldValue, newValue) -> {
            refresh();
        });
    }
    private void initScrollListener() {
        if (scrollPane != null) {
            // 监听垂直滚动属性变化
            scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                handleScrollPercentageEvent(oldValue.doubleValue(), newValue.doubleValue());
            });
        }
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
        barNet.setGraphic(new FontIcon(Material2MZ.SPEED ));
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
        for (XYChart.Series<String, Number> series : lineKey.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                addDataPointInteraction(data);
            }
        }

        // 为lineMemory添加交互
        for (XYChart.Series<String, Number> series : lineMemory.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                addDataPointInteraction(data);
            }
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
        barRefresh.getStyleClass().addAll(Styles.ACCENT,Styles.FLAT);
        barRefresh.setCursor(Cursor.HAND);
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

    private void addDataPointInteraction(XYChart.Data<String, Number> data) {
        Popup popup = new Popup();
        Label popupContent = new Label(data.getXValue() + ": " + data.getYValue());
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
            // 放大效果
            data.getNode().setScaleX(1.1);
            data.getNode().setScaleY(1.1);
            // 使用节点自身的填充颜色创建阴影
            Color shadowColor = Color.web("#f3622d");
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
            System.out.println("Clicked: " + data.getXValue() + " - " + data.getYValue());
        });
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

    @FXML
    public void openRefreshPopover(ActionEvent actionEvent) {
        if(refreshPopover!=null&&refreshPopover.isShowing()){
            return;
        }
        if(refreshPopover!=null){
            refreshPopover.show(barRefresh);
        }
    }

    /**
     * 初始化刷新弹出
     */
    private void initRefreshPopover() {
        Tuple2<AnchorPane, RefreshPopover> tuple2 = loadFXML("/fxml/popover/RefreshPopover.fxml");
        AnchorPane root = tuple2.getT1();
        tuple2.getT2().setParentController(this);
        tuple2.getT2().initAutoRefreshState(true);
        var pop = new Popover(root);
        pop.setHeaderAlwaysVisible(false);
        pop.setDetachable(false);
        pop.setArrowLocation(Popover.ArrowLocation.TOP_CENTER);
        refreshPopover= pop;
    }

    @Override
    public void setUpdateRefreshState(boolean b,int rateValue){
        if(b){
            barRefresh.getStyleClass().add(Styles.ACCENT);
            barRefresh.setText(rateValue+"s");
        }else {
            barRefresh.getStyleClass().removeAll(Styles.ACCENT);
            barRefresh.setText("");
        }
        System.out.println("状态"+b+"频率"+rateValue);


    }

    @Override
    public void refresh() {
        asynexec(()->{
            String infoStr = this.redisClient.info();
            List<InfoTable> infos= Util.parseInfoOutput(infoStr);
            Map<String, String> map = infos.stream().filter(e->Constant.REDIS_INFO_KEYS.contains(e.getKey())).collect(Collectors.toMap(InfoTable::getKey, InfoTable::getValue));
            Platform.runLater(()-> {

                barCpu.setText(map.get(Constant.REDIS_INFO_USED_CPU_USER));
                barNet.setText(map.get(Constant.REDIS_INFO_INSTANTANEOUS_OPS_PER_SEC));
                barMemory.setText(map.get(Constant.REDIS_INFO_USED_MEMORY));
                barKey.setText(map.get(Constant.REDIS_INFO_RDB_LAST_LOAD_KEYS_LOADED));
                barConnection.setText(map.get(Constant.REDIS_INFO_CONNECTED_CLIENTS));

                infoTable.getItems().setAll(infos);
                GuiUtil.adjustTableViewHeightPrecise(infoTable);

                redisVersion.setText(map.get(Constant.REDIS_INFO_REDIS_VERSION));
                os.setText(map.get(Constant.REDIS_INFO_OS));
                processId.setText(map.get(Constant.REDIS_INFO_PROCESS_ID));
                usedMemory.setText(map.get(Constant.REDIS_INFO_USED_MEMORY));
                usedMemoryPeak.setText(map.get(Constant.REDIS_INFO_USED_MEMORY_PEAK));
                usedMemoryLua.setText(map.get(Constant.REDIS_INFO_USED_MEMORY_LUA));
                connectedClients.setText(map.get(Constant.REDIS_INFO_CONNECTED_CLIENTS));
                totalConnectionsReceived.setText(map.get(Constant.REDIS_INFO_TOTAL_CONNECTIONS_RECEIVED));
                totalCommandsProcessed.setText(map.get(Constant.REDIS_INFO_TOTAL_COMMANDS_PROCESSED));

            });
        });



        System.out.println(123);
    }
}
