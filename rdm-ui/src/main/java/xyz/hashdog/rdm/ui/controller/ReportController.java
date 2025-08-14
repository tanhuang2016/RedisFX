package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Popover;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.ui.common.Constant;
import xyz.hashdog.rdm.ui.common.RedisDataTypeEnum;
import xyz.hashdog.rdm.ui.controller.popover.RefreshPopover;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

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
    public List<InfoTable> list;
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
    public Button pieRefresh;
    public Button topRefresh;
    private Popover refreshPopover;
    private XYChart.Series<String, Number> memorySeries;
    private XYChart.Series<String, Number> keySeries;
    private static final int MAX_DATA_POINTS = 10;
    private double previousUsedCpu;
    private long previousTime;
    private final Object lock = new Object();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyTheme();
        initStyle();
        initFontIcon();
        initTextField();
        initLabel();
        initLineChar();
        initModel();
        initListener();
        initTable();
        initLineAndAreaChart();
        DefaultEventBus.getInstance().subscribe(ThemeEvent.class, e -> {
            applyTheme();
        });
    }

    private void initLineAndAreaChart() {
        keySeries = new XYChart.Series<>();
        memorySeries = new XYChart.Series<>();
        initLineAndAreaChart(keySeries,lineKey,language("server.report.trend.key"));
        initLineAndAreaChart(memorySeries,lineMemory,language("server.report.trend.memory"));
    }

    private void initLineAndAreaChart(XYChart.Series<String, Number> keySeries, XYChart<String, Number> lineKey, String name) {
        lineKey.setTitle(name);
        lineKey.getData().add(keySeries);
        lineKey.setLegendVisible(false);
    }

    private void initTable() {
        topTable.getStyleClass().addAll(Tweaks.EDGE_TO_EDGE,Styles.STRIPED);
        infoTable.getStyleClass().addAll(Tweaks.EDGE_TO_EDGE,Styles.STRIPED);
        Platform.runLater(() -> {
            GuiUtil.initSimpleTableView(topTable,new TopKeyTable());
            GuiUtil.initSimpleTableView(infoTable,new InfoTable());
            topTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            );
            infoTable.setColumnResizePolicy(
                    TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
            );
        });
    }

    private void initLineChar() {
        initLineChar(lineKey,lineMemory);

    }

    @SafeVarargs
    private void initLineChar(XYChart<String, Number>... lineKey) {
        for (XYChart<String, Number> xyChart : lineKey) {
            NumberAxis yAxis = (NumberAxis)xyChart.getYAxis();
            yAxis.setAnimated(false);
            // 设置X轴样式
            CategoryAxis xAxis = (CategoryAxis) xyChart.getXAxis();
            xAxis.setAnimated(false); // 关闭X轴动画，避免跳动
            xAxis.tickLabelRotationProperty().set(-45); // 旋转标签避免重叠
            xAxis.setStartMargin(-25);
            xAxis.setEndMargin(-25);
        }
    }
    private void initLabel() {
        capsuleCpu.textProperty().bind(barCpu.textProperty());
        capsuleNet.textProperty().bind(barNet.textProperty());
        capsuleMemory.textProperty().bind(barMemory.textProperty());
        capsuleKey.textProperty().bind(barKey.textProperty());
        capsuleConnection.textProperty().bind(barConnection.textProperty());
    }
    private void initListener() {
        initScrollListener();
    }

    @Override
    protected void paramInitEnd() {
//        refresh();默认刷新是true，会自动触发
        initRefreshPopover();
        pieRefresh(null);
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
        pieRefresh.setGraphic(new FontIcon(Material2MZ.REFRESH ));
        topRefresh.setGraphic(new FontIcon(Material2MZ.REFRESH ));
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
        pieRefresh.getStyleClass().addAll(Styles.FLAT,Styles.DANGER);
        topRefresh.getStyleClass().addAll(Styles.FLAT,Styles.DANGER);
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
        initCardInfoStyle(serverInfo,memoryInfo,statusInfo);
    }

    private void initCardInfoStyle(TitledPane... infos) {
        for (TitledPane info : infos) {
            info.getStyleClass().add("report-titled-pane");
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

    private void addDataPointInteraction(XYChart.Data<String, Number> data,Function<Number,String> func) {
        Popup popup =getPopup(func.apply(data.getYValue()));
        dataNodeListener(data.getNode(),popup,Color.web("#f3622d"));
    }

    private void dataNodeListener(Node node, Popup popup,Color shadowColor) {
        node.setOnMouseEntered(event -> {
            // 放大效果
            node.setScaleX(1.1);
            node.setScaleY(1.1);
            // 使用节点自身的填充颜色创建阴影
            node.setEffect(new DropShadow(10, shadowColor));
            popup.show(node, event.getScreenX()+5, event.getScreenY() + 5);
        });

        node.setOnMouseExited(event -> {
            // 恢复正常大小
            node.setScaleX(1.0);
            node.setScaleY(1.0);
            node.setEffect(null);
            popup.hide();
        });
        node.setOnMouseMoved(event -> {
            // 动态更新位置，保持与鼠标的距离
            if (popup.isShowing()) {
                popup.setAnchorX(event.getScreenX() + 5);
                popup.setAnchorY(event.getScreenY() + 5);
            }
        });

    }

    private Popup getPopup(String text) {
        Popup popup = new Popup();
        Label popupContent = new Label(text);
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

        // 使整个 Popup 对鼠标事件透明
        popup.setAutoFix(true);
        // 不自动隐藏，由我们手动控制
        popup.setAutoHide(false);
        popup.setHideOnEscape(true);
        return popup;
    }

    private void setPieUpHoverTooltip(PieChart.Data data, String color, Function<Double,String> func) {
        Popup popup =getPopup(data.getName() + ":" + func.apply(data.getPieValue()));
        dataNodeListener(data.getNode(),popup, Color.web(color));
    }


    @FXML
    public void find(ActionEvent actionEvent) {
        String text = this.findTextField.getText();
        List<InfoTable> newList;
        if (DataUtil.isBlank(text)) {
            text = "*";
        }
        Predicate<InfoTable> nameFilter = createNameFilter(text);
        newList = this.list.stream().filter(nameFilter).collect(Collectors.toList());
        infoTable.getItems().setAll(newList);
    }
    private Predicate<InfoTable> createNameFilter(String query) {
        String regex = query.replace("?", ".?").replace("*", ".*?");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return o -> pattern.matcher(o.getKey()).find();
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
        this.addChild(tuple2.getT2());
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
    }
    @Override
    public void refresh() {
        asynexec(()->{
            String infoStr = null;
            synchronized (lock){
                 infoStr = this.redisClient.info();
            }
            List<InfoTable> infos= Util.parseInfoOutput(infoStr);
            this.list=infos;
            Map<String, String> map = infos.stream().filter(e->Constant.REDIS_INFO_KEYS.contains(e.getKey())).collect(Collectors.toMap(InfoTable::getKey, InfoTable::getValue));
            List<Tuple2<Integer,Integer>> dbSizeList = new ArrayList<>();
            infos.stream().filter(e->Constant.INFO_KEYSPACE.equals(e.getType())).forEach(e->dbSizeList.add(Util.keyspaceParseDb(e.getKey(),e.getValue())));
            Platform.runLater(()-> {
                //bar数据更新
                double cpuUsage = cpuUsage(map);
                barCpu.setText(Util.format(cpuUsage,2)+"%");
                barCpu.setTooltip(GuiUtil.textTooltip(String.format(language("server.report.bar.cpu")+"%s%%",Util.format(cpuUsage,2))));
                barNet.setText(map.get(Constant.REDIS_INFO_INSTANTANEOUS_OPS_PER_SEC));
                String barNetTooltip=String.format(language("server.report.bar.commands")+" %s",map.get(Constant.REDIS_INFO_INSTANTANEOUS_OPS_PER_SEC))
                        + System.lineSeparator()
                        + String.format(language("server.report.bar.input")+" %s",map.get(Constant.REDIS_INFO_INSTANTANEOUS_INPUT_KBPS))
                        + System.lineSeparator()
                        + String.format(language("server.report.bar.output")+" %s",map.get(Constant.REDIS_INFO_INSTANTANEOUS_OUTPUT_KBPS));
                barNet.setTooltip(GuiUtil.textTooltip(barNetTooltip));
                Tuple2<Double, String> barMemoryTu = Util.convertMemorySize(map.get(Constant.REDIS_INFO_USED_MEMORY));
                barMemory.setText(String.format("%s%s",Util.format(barMemoryTu.getT1(),2),barMemoryTu.getT2()));
                barMemory.setTooltip(GuiUtil.textTooltip(String.format(language("server.report.bar.memory")+" %s%s",Util.format(barMemoryTu.getT1(),4),barMemoryTu.getT2())));
                int keyTotalSize = dbSizeList.stream().mapToInt(Tuple2::getT2).sum();
                barKey.setText(String.valueOf(keyTotalSize));
                StringBuilder barKeyTooltip = new StringBuilder(String.format(language("server.report.bar.key")+" %s", keyTotalSize));
                for (Tuple2<Integer, Integer> tus : dbSizeList) {
                    barKeyTooltip.append(System.lineSeparator());
                    barKeyTooltip.append(String.format("DB%s: %s", tus.getT1(), tus.getT2()));
                }
                barKey.setTooltip(GuiUtil.textTooltip(barKeyTooltip.toString()));
                barConnection.setText(map.get(Constant.REDIS_INFO_CONNECTED_CLIENTS));
                barConnection.setTooltip(GuiUtil.textTooltip(String.format(language("server.report.bar.connected")+" %s",map.get(Constant.REDIS_INFO_CONNECTED_CLIENTS))));
                //信息集合表数据更新
                infoTable.getItems().setAll(infos);
                GuiUtil.adjustTableViewHeightPrecise(infoTable);
                //card数据更新
                redisVersion.setText(map.get(Constant.REDIS_INFO_REDIS_VERSION));
                os.setText(map.get(Constant.REDIS_INFO_OS));
                processId.setText(map.get(Constant.REDIS_INFO_PROCESS_ID));
                usedMemory.setText(String.format("%s%s",Util.format(barMemoryTu.getT1(),2),barMemoryTu.getT2()));
                Tuple2<Double, String> usedMemoryPeakTu = Util.convertMemorySize(map.get(Constant.REDIS_INFO_USED_MEMORY_PEAK));
                usedMemoryPeak.setText(String.format("%s%s",Util.format(usedMemoryPeakTu.getT1(),2),usedMemoryPeakTu.getT2()));
                Tuple2<Double, String> usedMemoryLuaTu = Util.convertMemorySize(map.get(Constant.REDIS_INFO_USED_MEMORY_LUA));
                usedMemoryLua.setText(String.format("%s%s",Util.format(usedMemoryLuaTu.getT1(),2),usedMemoryLuaTu.getT2()));
                connectedClients.setText(map.get(Constant.REDIS_INFO_CONNECTED_CLIENTS));
                totalConnectionsReceived.setText(map.get(Constant.REDIS_INFO_TOTAL_CONNECTIONS_RECEIVED));
                totalCommandsProcessed.setText(map.get(Constant.REDIS_INFO_TOTAL_COMMANDS_PROCESSED));
                //趋势图数据更新
                updateLineCharData(Double.parseDouble(map.get(Constant.REDIS_INFO_USED_MEMORY)),keyTotalSize);
            });
        });
    }


    private void updateLineCharData(double memoryValue,int keyTotalSize) {
        // 获取当前时间
        String timeLabel = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        // 添加新数据点
        XYChart.Data<String, Number> newDataArea = new XYChart.Data<>(timeLabel, memoryValue);
        XYChart.Data<String, Number> newDataLine = new XYChart.Data<>(timeLabel, keyTotalSize);
        memorySeries.getData().add(newDataArea);
        keySeries.getData().add(newDataLine);
        // 为新数据点添加交互效果
        Platform.runLater(() -> {
            addDataPointInteraction(newDataArea,e->Util.convertMemorySizeStr(e.longValue(),4));
            addDataPointInteraction(newDataLine,Number::toString);
        });
        // 限制数据点数量，避免图表过于拥挤
        if (memorySeries.getData().size() > MAX_DATA_POINTS) {
            memorySeries.getData().removeFirst();
        }
        if (keySeries.getData().size() > MAX_DATA_POINTS) {
            keySeries.getData().removeFirst();
        }
    }

    /**
     * cpu使用率
     * CPU Usage = (current_used_cpu - previous_used_cpu) / (current_time - previous_time) * 100
     */
    private double cpuUsage(Map<String, String> map) {
       double currentUsedCpu= Double.parseDouble(map.get(Constant.REDIS_INFO_USED_CPU_USER))+ Double.parseDouble(map.get(Constant.REDIS_INFO_USED_CPU_SYS));
       long currentTime = System.currentTimeMillis();
        if(previousTime==0){
            previousUsedCpu=currentUsedCpu;
            previousTime=currentTime;
            return 0;
        }
        double usage=(currentUsedCpu-previousUsedCpu)/((double) (currentTime - previousTime) /1000)*100;
        previousUsedCpu=currentUsedCpu;
        previousTime=currentTime;
        return usage;
    }

    @FXML
    public void pieRefresh(ActionEvent actionEvent) {
        topRefresh(actionEvent);
    }


    @FXML
    public void topRefresh(ActionEvent actionEvent) {
        asynexec(() -> {
            List<TopKeyTable> topKeyTables = new ArrayList<>();
            synchronized (lock){
                List<String> keys = this.redisClient.scanAll(null);
                for (String key : keys) {
                    long memory=this.redisClient.memoryUsage(key,0);
                    String type = this.redisClient.type(key);
                    long ttl = this.redisClient.ttl(key);
                    long length=lengthByType(key,type);
                    TopKeyTable topKeyTable = new TopKeyTable(key, type, ttl, memory, length);
                    topKeyTables.add(topKeyTable);
                }
            }
            updatePiesData(topKeyTables);
            updateTopData(topKeyTables);
        });
    }

    private void updateTopData(List<TopKeyTable> topKeyTables) {
        List<TopKeyTable> top10BySize = topKeyTables.stream()
                .sorted((t1, t2) -> Long.compare(t2.getSize(), t1.getSize()))
                .limit(10)
                .toList();
        keySize.setUserData(top10BySize);
        List<TopKeyTable> top10ByLength = topKeyTables.stream()
                .sorted((t1, t2) -> Long.compare(t2.getLength(), t1.getLength()))
                .limit(10)
                .toList();
        keyLength.setUserData(top10ByLength);
        Platform.runLater(() -> {
            topTable.getItems().setAll(top10BySize);
        });
    }

    private void updatePiesData(List<TopKeyTable> topKeyTables) {
        Map<String, Long> keysData = topKeyTables.stream().collect(Collectors.groupingBy(TopKeyTable::getType, Collectors.counting()));
        Map<String, Long> memoryData = topKeyTables.stream().collect(Collectors.groupingBy(TopKeyTable::getType, Collectors.summingLong(TopKeyTable::getSize)));
        updatePiesData(keysData,keys, d->String.valueOf((long)d.doubleValue()));
        updatePiesData(memoryData,memory,d->Util.convertMemorySizeStr((long)d.doubleValue(),2));
    }

    private void updatePiesData(Map<String, Long> keysData, PieChart keys, Function<Double,String> func) {
        ObservableList<PieChart.Data> keysPieData = FXCollections.observableArrayList();
        List<Tuple2<String, String>> tagList=new ArrayList<>();
        keysData.forEach((type, count) -> {
            Tuple2<String, String> keyTypeTag = GuiUtil.getKeyTypeTag(type);
            tagList.add(keyTypeTag);
            PieChart.Data pieData = new PieChart.Data(keyTypeTag.getT1(), count);
            keysPieData.add(pieData);
        });
        Platform.runLater(() -> {
            keys.setData(keysPieData);
            for (int i = 0; i < keys.getData().size(); i++) {
                PieChart.Data datum = keys.getData().get(i);
                datum.getNode().setStyle("-fx-pie-color: " + GuiUtil.hexToRgba(tagList.get(i).getT2()));
                setPieUpHoverTooltip(datum,tagList.get(i).getT2(),func);
            }
        });
    }

    private long lengthByType(String key, String type) {
        RedisDataTypeEnum byType = RedisDataTypeEnum.getByType(type);
        return switch (byType) {
            case STRING -> this.redisClient.strlen(key);
            case LIST -> this.redisClient.llen(key);
            case HASH -> this.redisClient.hlen(key);
            case SET -> this.redisClient.scard(key);
            case ZSET -> this.redisClient.zcard(key);
            case JSON -> jsonLength(key);
            case STREAM -> this.redisClient.xlen(key);
        };

    }

    private long jsonLength(String key) {
        Class<?> type =this.redisClient.jsonType(key);
        if(type==Object.class){
            return this.redisClient.jsonObjLen(key);
        } else if (type==String.class) {
            return this.redisClient.jsonStrLen(key);
        }else if(type==List.class){
            return this.redisClient.jsonArrLen(key);
        }

        return 0;
    }
    @SuppressWarnings("unchecked")
    public void keySize(ActionEvent actionEvent) {
       topTable.getItems().setAll((List<TopKeyTable>) keySize.getUserData());
    }
    @SuppressWarnings("unchecked")
    public void keyLength(ActionEvent actionEvent) {
        topTable.getItems().setAll((List<TopKeyTable>) keyLength.getUserData());
    }


}
