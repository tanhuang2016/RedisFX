package redisfx.tanh.rdm.ui.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redisfx.tanh.rdm.common.pool.ThreadPool;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.common.util.DataUtil;
import redisfx.tanh.rdm.common.util.FileUtil;
import redisfx.tanh.rdm.common.util.TUtil;
import redisfx.tanh.rdm.redis.client.RedisClient;
import redisfx.tanh.rdm.redis.client.RedisKeyScanner;
import redisfx.tanh.rdm.ui.Main;
import redisfx.tanh.rdm.ui.common.*;
import redisfx.tanh.rdm.ui.common.*;
import redisfx.tanh.rdm.ui.controller.base.BaseClientController;
import redisfx.tanh.rdm.ui.entity.DBNode;
import redisfx.tanh.rdm.ui.entity.KeyTreeNode;
import redisfx.tanh.rdm.ui.entity.PassParameter;
import redisfx.tanh.rdm.ui.entity.config.KeyTabPaneSetting;
import redisfx.tanh.rdm.ui.exceptions.GeneralException;
import redisfx.tanh.rdm.ui.util.GuiUtil;
import redisfx.tanh.rdm.ui.util.RecentHistory;
import redisfx.tanh.rdm.ui.util.SvgManager;
import redisfx.tanh.rdm.ui.util.Util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;
/**
 * 服务tab页
 *
 * @author th
 */
public class ServerTabController extends BaseClientController<MainController> {


    private static final Logger log = LoggerFactory.getLogger(ServerTabController.class);
    /**
     * 搜索的内容
     */
    @FXML
    public CustomTextField searchText;

    /**
     * 右键菜单
     */
    @FXML
    public ContextMenu contextMenu;
    @FXML
    public MenuButton newKey;
    @FXML
    public Button search;
    public HBox searchHbox;
    public MenuButton searchOptionsButton;
    public Button reset;
    public ToggleButton isLike;

    public ContextMenu history;
    public MenuItem delete;
    public MenuItem open;
    public ProgressBar progressBar;
    public Label progressText;

    public Button loadMore;
    public Button locationButton;
    public Button expandedButton;
    public Button collapseButton;
    public Button optionsButton;
    public Button hideButton;
    public HBox toolBar;
    public Button showButton;
    public Button loadAll;
    public HBox toolBarRight;
    public MenuItem refresh;
    public MenuItem flush;
    public MenuItem console;
    public MenuItem monitor;
    public MenuItem pubsub;
    public MenuItem report;
    public CheckMenuItem autoSearch;
    public Menu searchTypeMenu;
    public CheckMenuItem checkBox;
    public Button boxDelete;
    public Button boxExport;
    public Button boxCancel;
    public HBox boxToolBar;
    public CheckBox boxSelectAll;
    public MenuItem export;
    @FXML
    private TreeView<KeyTreeNode> treeView;
    @FXML
    private ChoiceBox<DBNode> choiceBox;
    /**
     * tab页容器
     */
    @FXML
    public TabPane dbTabPane;

    private RecentHistory<String> recentHistory ;
    private MenuItem clearItem;

    /**
     * 缓存的图标加载任务，用于批量处理类型tag，避免线程切换的开销
     */
    private final Queue<TreeItem<KeyTreeNode>> iconLoadQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isLoading = new AtomicBoolean(false);

    private final static int SCAN_COUNT = 500;
    private RedisKeyScanner scanner;
    private ToggleGroup searchTypeMenuGroup;

    private static final int DEBOUNCE_DELAY_MILLIS = 500;
    /**
     * 防抖用的 Timeline
     */
    private final Timeline debounceTimeline = new Timeline();


    /**
     * 最后一个选中节点，可能是目录哦
     */
    private TreeItem<KeyTreeNode> lastSelectedNode;
    /**
     * 缓存已打开的key节点，用户删除的时候提高性能，避免从根节点递归
     * 删除缓存的策略没有想好，目前考虑只存放10个，一般不会打开超过10个窗口
     * 存储的时候先进先出，保留最新的10个(todo 10个的数量后期可能优化成，tabs运行打开的数量，要做成可配置)
     * 用
     */
    private final LinkedHashSet<WeakReference<TreeItem<KeyTreeNode>>> openTreeItems = new LinkedHashSet<>(10);
    /**
     * 当前打开窗口的key
     */
    private String selectTabKey;

    private static final String ALL_TYPES = "All Types";
    private static  SoftReference<File> lastFile;


    @FXML
    public void initialize() {
        initRecentHistory();
        initNewKey();
        initAutoWah();
        initTreeViewRoot();
        initButton();
        initSearchTypeMenu();
        initTextField();
        initListener();
        initTabPane();
        progressBar.getStyleClass().add(Styles.SMALL);
        initLanguage();



    }

    /**
     * 初始化搜索类型菜单
     */
    private void initSearchTypeMenu() {
        this.searchTypeMenuGroup = new ToggleGroup();
        ObservableList<MenuItem> items = searchTypeMenu.getItems();
        for (RedisDataTypeEnum value : RedisDataTypeEnum.values()) {
            if(value==RedisDataTypeEnum.UNKNOWN){
                continue;
            }
            Label tag = GuiUtil.getKeyColorFontIcon(value.type);
            RadioMenuItem radioMenuItem = new RadioMenuItem(value.type,tag);
            radioMenuItem.setToggleGroup(searchTypeMenuGroup);
            items.add(radioMenuItem);
        }
        RadioMenuItem allType = new RadioMenuItem("All Types", GuiUtil.getKeyColorFontIcon(null));
        allType.setToggleGroup(searchTypeMenuGroup);
        items.addFirst(allType);
        allType.setSelected(true);
        searchTypeMenuGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue==null){
                return;
            }
            RadioMenuItem radioMenuItem = (RadioMenuItem) newValue;
            String tag = radioMenuItem.getText();
            if(ALL_TYPES.equals(tag)){
                searchText.setPromptText("Search "+ALL_TYPES);
                search.getGraphic().getStyleClass().remove("tag-icon");
            }else {
                searchText.setPromptText("Search %s Type".formatted(tag));
                if(!search.getGraphic().getStyleClass().contains("tag-icon")){
                    search.getGraphic().getStyleClass().add("tag-icon");
                }
                GuiUtil.getSetFontIconColorByKeyType(tag,search);
            }
        });
    }

    /**
     * 显示历史搜索记录
     */
    private void showHistoryPopup() {
        history.show(search,
                searchText.localToScreen(0, 0).getX(),
                searchText.localToScreen(0, 0).getY() + searchText.getHeight());
    }

    @Override
    protected void initLanguage() {
        search.setTooltip(GuiUtil.hintTooltip(language("server.search"),language("server.search.hint")));
        reset.setTooltip(GuiUtil.textTooltip(language("server.reset")));
        isLike.setTooltip(GuiUtil.textTooltip(language("server.like")));
        searchOptionsButton.setTooltip(GuiUtil.textTooltip(language("server.search.option")));
        progressBarLanguage();
        locationButton.setTooltip(GuiUtil.textTooltip(language("server.toolBar.location")));
        expandedButton.setTooltip(GuiUtil.textTooltip(language("server.toolBar.expanded")));
        collapseButton.setTooltip(GuiUtil.textTooltip(language("server.toolBar.collapse")));
        hideButton.setTooltip(GuiUtil.textTooltip(language("server.toolBar.hide")));
        showButton.setTooltip(GuiUtil.textTooltip(language("server.toolBar.show")));
        open.setText(language("server.open"));
        refresh.setText(language("server.refresh"));
        delete.setText(language("server.delete"));
        export.setText(language("key.string.export"));
        flush.setText(language("server.flush"));
        checkBox.setText(language("server.box"));
        console.setText(language("server.console"));
        monitor.setText(language("server.monitor"));
        pubsub.setText(language("server.pubsub"));
        report.setText(language("server.report"));
        loadMore.setText(language("server.toolBar.loadMore"));
        loadAll.setText(language("server.toolBar.loadAll"));
        boxDelete.setText(language("key.delete"));
        boxExport.setText(language("key.string.export"));
        boxCancel.setText(language("common.cancel"));
        newKey.setText(language("server.new"));
        clearItem.setText(language("server.clear"));
    }

    private void progressBarLanguage() {
        if(scanner==null){
            return;
        }
        progressBar.setTooltip(GuiUtil.textTooltip(String.format(language("server.toolBar.progress"),scanner.getSum())));
        progressText.setTooltip(GuiUtil.textTooltip(String.format(language("server.toolBar.progress"),scanner.getSum())));
    }

    private void initTabPane() {
        KeyTabPaneSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAB_PANE.name);
        this.dbTabPane.setSide(Side.valueOf(setting.getSide()));
        this.dbTabPane.setTabMaxWidth(200);
    }

    /**
     * 最近使用搜索记录初始化
     */
    private void initRecentHistory() {
        history = new ContextMenu();
        history.getItems().add(new SeparatorMenuItem());
        this.clearItem = new MenuItem(language("server.clear"));
        clearItem.setOnAction(this::clearHistory);
        history.getItems().add(clearItem);
        recentHistory = new RecentHistory<>(5, this::doRecentHistory);
        doRecentHistory(recentHistory.get());
    }

    /**
     * 创建搜索记录的菜单项
     * @param str 历史记录名称
     * @return 菜单项
     */
    private MenuItem createSearchHistoryMenuItem(String str) {
        MenuItem menuItem = new MenuItem(str);
        menuItem.setOnAction(event -> searchText.setText(menuItem.getText()));
        return menuItem;
    }

    /**
     * 搜索记录变更，需要更新menuButton的显示内容
     * @param list 最新历史记录列表
     */
    private void doRecentHistory(List<String> list) {
        ObservableList<MenuItem> items = history.getItems();
        //为了一致性，直接清空在重新赋值，虽然单个元素增加会减少消耗，但是复杂度增加，暂时不考虑
        items.remove(0,items.size() - 2);
        List<String> reversed = list.reversed();
        Platform.runLater(() -> {
            for (String s : reversed) {
                items.addFirst(createSearchHistoryMenuItem(s));
            }
        });

    }

    private void initTextField() {
        searchText.setRight(searchHbox);
        searchText.setLeft(search);
    }

    private void initButton() {
        initButtonIcon();
        initButtonStyles();

    }
    private void initButtonStyles() {
        search.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT,Styles.BUTTON_CIRCLE);
        isLike.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT, UiStyles.MINI,UiStyles.SEMI_CIRCLE);
        reset.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT,UiStyles.MINI,Styles.ROUNDED);
        searchOptionsButton.getStyleClass().addAll(Styles.BUTTON_ICON,Tweaks.NO_ARROW,Styles.FLAT,UiStyles.MINI,UiStyles.SEMI_CIRCLE);
        search.setCursor(Cursor.HAND);
        reset.setCursor(Cursor.HAND);
        isLike.setCursor(Cursor.HAND);
        searchOptionsButton.setCursor(Cursor.HAND);
        newKey.getStyleClass().addAll(Tweaks.NO_ARROW);
        initToolBarButtonStyles(locationButton,expandedButton,collapseButton,optionsButton,hideButton,showButton);
        loadMore.getStyleClass().addAll(Styles.SMALL, UiStyles.MINI);
        loadAll.getStyleClass().addAll(Styles.SMALL,Styles.DANGER, UiStyles.MINI);
        boxDelete.getStyleClass().addAll(Styles.SMALL,Styles.DANGER, UiStyles.MINI);
        boxExport.getStyleClass().addAll(Styles.SMALL,Styles.ACCENT, UiStyles.MINI);
        boxCancel.getStyleClass().addAll(Styles.SMALL,Styles.ACCENT, UiStyles.MINI,Styles.BUTTON_OUTLINED);
    }

    private void initToolBarButtonStyles(Button... buttons) {
        for (Button button : buttons) {
            button.getStyleClass().addAll(Styles.FONT_ICON,Styles.FLAT,Styles.SMALL,Styles.ROUNDED);
        }

    }

    private void initButtonIcon() {
        search.setGraphic(new FontIcon(Feather.SEARCH));
        reset.setGraphic(new FontIcon(Material2AL.CLEAR));
        SvgManager.loadMini(this,isLike,"/svg/regex/regex.svg");
        GuiUtil.setIcon(searchOptionsButton,new FontIcon((Material2MZ.MORE_VERT)));
        GuiUtil.setIcon(locationButton,new FontIcon((Material2MZ.RADIO_BUTTON_CHECKED)));
        GuiUtil.setIcon(expandedButton,new FontIcon((Material2MZ.UNFOLD_MORE)));
        GuiUtil.setIcon(collapseButton,new FontIcon((Material2MZ.UNFOLD_LESS)));
        GuiUtil.setIcon(optionsButton,new FontIcon((Material2MZ.MORE_VERT)));
        GuiUtil.setIcon(hideButton,new FontIcon((Material2MZ.REMOVE)));
        GuiUtil.setIcon(showButton,new FontIcon((Material2MZ.PLUS)));

    }

    /**
     * 初始化新增类型
     */
    private void initNewKey() {
        ObservableList<MenuItem> items = newKey.getItems();
        items.clear();
        for (RedisDataTypeEnum value : RedisDataTypeEnum.values()) {
            if(value==RedisDataTypeEnum.UNKNOWN){
                continue;
            }
            Label tag = GuiUtil.getKeyColorFontIcon(value.type);
            items.add(new MenuItem(value.type,tag));
        }
    }

    /**
     * 初始化监听时间
     */
    private void initListener() {
        userDataPropertyListener();
        choiceBoxSelectedListener();
        treeViewListener();
        newKeyListener();
        searchTextListener();
        searchTypeMenuGroupListener();
    }

    private void searchTypeMenuGroupListener() {
        searchTypeMenuGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null && !newValue.equals(oldValue)){
                search(null);
            }
        });
    }


    private void searchTextListener() {
        searchText.textProperty().addListener((observable, oldValue, newValue) -> {
            // 判断 TextField 是否为空
            if (newValue == null || newValue.trim().isEmpty()) {
                reset.setVisible(false);
                reset.setManaged(false);
            } else {
                reset.setVisible(true);
                reset.setManaged(true);
                //触发自动搜索
                if(!newValue.equals(oldValue)&&autoSearch.isSelected()){
                    // 每次文本变化时，先停止之前的定时任务
                    debounceTimeline.stop();
                    // 设置新的任务，在延迟时间后执行搜索
                    debounceTimeline.getKeyFrames().clear(); // 清除之前的 KeyFrame
                    debounceTimeline.getKeyFrames().add(
                            new KeyFrame(Duration.millis(DEBOUNCE_DELAY_MILLIS), event -> {
                                // 在延迟结束后执行搜索
                                search(null);
                            })
                    );
                    // 启动新的定时任务
                    debounceTimeline.playFromStart();
                }
            }
        });
        // 添加键盘事件监听,alt+down 显示历史搜索记录
        searchText.setOnKeyPressed(event -> {
            if (event.isAltDown() && event.getCode() == KeyCode.DOWN) {
                showHistoryPopup();
                // 阻止事件继续传播
                event.consume();
            }
            if (event.getCode() == KeyCode.ENTER) {
                // 停止任何待处理的自动搜索
                debounceTimeline.stop();
                // 立即执行搜索
                search(null);
                event.consume();
            }
        });
    }

    /**
     * 新增key的点击事件
     */
    private void newKeyListener() {
        for (MenuItem item : newKey.getItems()) {
            item.setOnAction(e->{
                MenuItem selectedItem = (MenuItem) e.getSource();
                String text = selectedItem.getText();
                RedisDataTypeEnum byType = RedisDataTypeEnum.getByType(text);
                Tuple2<AnchorPane,NewKeyController> tuple2 = loadFxml("/fxml/NewKeyView.fxml");
                AnchorPane anchorPane = tuple2.t1();
                NewKeyController controller = tuple2.t2();
                PassParameter passParameter = new PassParameter(byType.tabType);
                passParameter.setDb(this.currentDb);
                //这里设置null,是怕忘记
                passParameter.setKey(null);
                passParameter.setKeyType(byType.type);
                passParameter.setRedisClient(redisClient);
                passParameter.setRedisContext(redisContext);
                controller.setParameter(passParameter);
                Stage stage= GuiUtil.createSubStage(String.format(Main.RESOURCE_BUNDLE.getString(Constant.TITLE_NEW_KEY),text ),anchorPane,root.getScene().getWindow());
                controller.setCurrentStage(stage);
                stage.show();

            });
        }
    }




    /**
     * 根节点初始化一个空的
     */
    private void initTreeViewRoot() {
        treeView.setRoot(new TreeItem<>());
        // 隐藏根节点
        treeView.setShowRoot(false);
        //默认根节点为选中节点
        treeView.getSelectionModel().select(treeView.getRoot());
        treeView.setCellFactory(tv -> new CheckBoxTreeCell<>() {

            @Override
            public void updateItem(KeyTreeNode item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 设置基本文本
                    setText(item.toString());
                    // 只有叶子节点首次显示时才加载图标
                    if (item.getLeaf() && !item.isInitialized()) {
                        loadNodeGraphicIfNeeded(this.getTreeItem());
                        item.setInitialized(true);
                    }
                    // 如果图标已经加载过，直接显示
                    if (getTreeItem().getGraphic() != null) {
                        Node graphic = getTreeItem().getGraphic();
                        if(!checkBox.isSelected()){
                            setGraphic(graphic);
                            return;
                        }
                        Node box = getGraphic();
                        if(box instanceof CheckBox cb){
                            cb.setText(" ");
                            cb.setGraphicTextGap(0);
                            cb.setGraphic(graphic);
                        }
                        setGraphic(box);
                    }
                }
            }


        });
    }

    /**
     * 加兹安节点图标
     * 先加入队列，再批量触发加载，避免线程频繁切换带来的开销
     * @param treeItem 节点
     */
    private void loadNodeGraphicIfNeeded(TreeItem<KeyTreeNode> treeItem) {
        if (!treeItem.getValue().getLeaf()) {
            return;
        }
        // 将节点加入队列
        iconLoadQueue.offer(treeItem);

        // 尝试触发加载（使用CAS确保只有一个线程能触发加载）
        if (isLoading.compareAndSet(false, true)) {
            triggerBatchLoad();
        }
    }

    /**
     * 触发批量加载图标
     */
    private void triggerBatchLoad() {
        async(() -> {
            try {
                AtomicInteger n= new AtomicInteger();
                if(iconLoadQueue.isEmpty()){
                    return;
                }
                List<TreeItem<KeyTreeNode>> treeItems = new ArrayList<>();
                List<Object> pipelineResults =exeRedis(j -> j.executePipelined(commands -> {
                    // 批量处理队列中的所有节点
                    TreeItem<KeyTreeNode> treeItem;
                    // 取出队列中所有待加载的节点
                    while ((treeItem = iconLoadQueue.poll()) != null) {
                        KeyTreeNode item = treeItem.getValue();
                        if(item.getType()==null){
                            commands.type(item.getKey());
                            treeItems.add(treeItem);
                        }else {
                            Label keyTypeLabel = GuiUtil.getKeyTypeLabel(item.getType());
                            treeItem.setGraphic(keyTypeLabel);
                            // 标记节点已初始化
                            treeItem.getValue().setInitialized(true);
                        }
                        n.getAndIncrement();
                    }
                },this.currentDb));
                //管道查询的结果，需要更新到树里面
               if(!treeItems.isEmpty()){
                   for (int i = 0; i < treeItems.size(); i++) {
                       TreeItem<KeyTreeNode> keyTreeNodeTreeItem = treeItems.get(i);
                       KeyTreeNode value = keyTreeNodeTreeItem.getValue();
                       value.setType(pipelineResults.get(i).toString());
                       value.setInitialized(true);
                       Label keyTypeLabel = GuiUtil.getKeyTypeLabel(value.getType());
                       keyTreeNodeTreeItem.setGraphic(keyTypeLabel);
                   }
               }

                // 只在需要时刷新
                if (n.get() >0) {
                    // 在UI线程中更新所有图标
                    Platform.runLater(() -> treeView.refresh());
                }

            } catch (Exception e) {
                log.error("triggerBatchLoad Exception", e);
            } finally {
                // 释放锁，允许下一次加载
                isLoading.set(false);
                // 检查队列是否还有待处理的节点，如果有则继续触发加载
                if (!iconLoadQueue.isEmpty()) {
                    if (isLoading.compareAndSet(false, true)) {
                        triggerBatchLoad();
                    }
                }
            }
        });
    }



    /**
     * key树的监听
     */
    private void treeViewListener() {
        initTreeViewMultiple();
        buttonIsShowAndSetSelectNode();
        doubleClicked();
    }

    /**
     * 监听treeView选中事件,判断需要显示和隐藏的按钮/菜单
     * 将选中的节点,缓存到类
     */
    private void buttonIsShowAndSetSelectNode() {
        contextMenu.setOnShowing(event -> {
            if(treeView.getSelectionModel().getSelectedItems().isEmpty()){
                this.delete.setVisible(false);
                this.open.setVisible(false);
                return;
            }
            this.open.setVisible(true);
            //只要有一个选择节点是目录，有选中目录就不能删除
            boolean isDir = treeView.getSelectionModel().getSelectedItems()
                    .stream()
                    .anyMatch(item -> !item.isLeaf());
            this.delete.setVisible(!isDir);

        });
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                newValue = treeView.getRoot();
            }
            //设置最后一个选中节点
            this.lastSelectedNode = newValue;

        });
    }

    /**
     * treeView双击事件
     * 如果双击节点为连接,则打开链接
     */
    private void doubleClicked() {
        // 添加鼠标点击事件处理器
        treeView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                // 获取选中的节点
                TreeItem<KeyTreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.isLeaf()) {
                    open(null);
                }
            }
        });
    }

    /**
     * db选择框监听
     * db切换后,更新key节点
     */
    private void choiceBoxSelectedListener() {

        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue==null){
                return;
            }
            this.currentDb=newValue.getDb();
            resetToolBar();
            Future<Boolean> submit = ThreadPool.getInstance().submit(() -> this.redisClient.select(this.currentDb), true);
            try {
                if (submit.get()!=null) {
                    search(null);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("choiceBoxSelectedListener Exception", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 当key首次加载就完全加载完，就不显示工具栏
     */
    private void resetToolBar() {
        boolean scanAll = this.choiceBox.getSelectionModel().getSelectedItem().getDbSize() <= SCAN_COUNT;
        this.toolBar.setVisible(!scanAll);
        this.toolBar.setManaged(!scanAll);
        this.showButton.setVisible(scanAll);
    }

    /**
     * 父层传送的数据监听
     * 监听到进行db选择框的初始化
     */
    private void userDataPropertyListener() {
        super.parameter.addListener((observable, oldValue, newValue) -> {
            initDbSelects();
            initScanner();
        });
    }

    /**
     * 初始化key扫描器
     */
    private void initScanner() {
        this.scanner=this.redisClient.getRedisKeyScanner();
    }


    /**
     * 初始化db选择框
     */
    private void initDbSelects() {
        ObservableList<DBNode> items = choiceBox.getItems();
        async(() -> {
            Map<Integer, Integer> map = this.redisClient.dbSize();
            Platform.runLater(() -> {
                for (Map.Entry<Integer, Integer> en : map.entrySet()) {
                    items.add(new DBNode( en.getKey(),en.getValue()));
                }
                //默认选中第一个
                choiceBox.setValue(choiceBox.getItems().getFirst());
            });
        });

    }

    /**
     * 重置db数量
     */
    private void resetDbSelects(){
        DBNode selectedItem = choiceBox.getSelectionModel().getSelectedItem();
        ObservableList<DBNode> items= FXCollections.observableArrayList();
        async(() -> {
            Map<Integer, Integer> map = this.redisClient.dbSize();
            Platform.runLater(() -> {
                for (Map.Entry<Integer, Integer> en : map.entrySet()) {
                    DBNode dbNode = new DBNode( en.getKey(),en.getValue());
                    items.add(dbNode);
                }
                choiceBox.setItems(items);
                choiceBox.setValue(items.get(selectedItem.getDb()));
            });
        });


    }

    /**
     * 节点多选设置
     */
    private void initTreeViewMultiple() {
        // 启用多选功能
        treeView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        //shift 或则ctrl+鼠标单机为选取操作,会触发选中,选择父节点会同步选中子节点
        treeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                if (event.isShiftDown() || event.isControlDown()) {
                    List<TreeItem<KeyTreeNode>> list = new ArrayList<>();
                    for (TreeItem<KeyTreeNode> selectedItem : treeView.getSelectionModel().getSelectedItems()) {
                        if (!selectedItem.isLeaf()) {
                            list.add(selectedItem);

                        }
                    }
                    for (TreeItem<KeyTreeNode> selectedItem : list) {
                        //设置选中
                        selectChildren( selectedItem);
                    }
                }

            }
        });
    }

    /**
     * 数据已经有了,直接更新到视图
     */
    private void initTreeView(List<String> keys) {
        ObservableList<TreeItem<KeyTreeNode>> children = treeView.getRoot().getChildren();
        children.clear();
        loadIntoTreeView(keys);
    }

    /**
     * 把查询到的key加载到视图，可以增量加载
     */
    private void loadIntoTreeView(List<String> keys) {
        Platform.runLater(() -> {
            if(this.redisContext.getRedisConfig().isTreeShow()){
                buildTreeView(keys);
            }else {
                buildListView(keys);
            }
        });
    }

    /**
     * key构建列表
     * @param keys key列表
     */
    private void buildListView( List<String> keys) {
        ObservableList<TreeItem<KeyTreeNode>> children = treeView.getRoot().getChildren();
        List<TreeItem<KeyTreeNode>> list = new ArrayList<>();
        for (String key : keys) {
            list.add(new CheckBoxTreeItem<>(KeyTreeNode.leaf(key)));
        }
        children.addAll( list);
    }

    /**
     * key排序
     * 目录排前，key排后面，如果有创建时间，则根据创建时间新建的key排前面
     * 这样是避免定位的时候滚动条滑动，造成不必要的图标加载
     * @return 排序规则
     */
    private Comparator<TreeItem<KeyTreeNode>> treeItemSortComparator(){
        return (o1, o2) -> {
            if (o1.getValue().getLeaf() && o2.getValue().getLeaf()) {
                long newKeyTime1 = o1.getValue().getNewKeyTime();
                long newKeyTime2 = o2.getValue().getNewKeyTime();
                if (newKeyTime1 > 0 && newKeyTime2 > 0) {
                    int timeComparison = Long.compare(newKeyTime2, newKeyTime1);
                    if (timeComparison != 0) {
                        return timeComparison;
                    }
                }// 如果只有node1是新key，node1排前面
                else if (newKeyTime1 > 0) {
                    return -1;
                }
                // 如果只有node2是新key，node2排前面
                else if (newKeyTime2 > 0) {
                    return 1;
                }
                return o1.getValue().getKey().compareTo(o2.getValue().getKey());
            } else if (o1.getValue().getLeaf()) {
                return 1;
            } else if (o2.getValue().getLeaf()) {
                return -1;
            } else {
                return o1.getValue().getName().compareTo(o2.getValue().getName());
            }
        };
    }




    /**
     * 控件换时间，利用缓存优化了key的树形结构构造，速度提升了10倍不止
     * @param keys key列表
     */
    private void buildTreeView(List<String> keys) {
        TreeItem<KeyTreeNode> root = treeView.getRoot();
        Map<String, TreeItem<KeyTreeNode>> treeItemDirMap = findTreeItemDir(root);
        String keySeparator = this.redisContext.getRedisConfig().getKeySeparator();
        for (String key : keys) {
            String[] parts = key.split(keySeparator);
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                //叶子节点是key类型
                boolean isLeaf = (i == parts.length - 1);
                TreeItem<KeyTreeNode> childNode;
                TreeItem<KeyTreeNode> hasRoot;
                //找到父节点，如果没有就是根目录
                hasRoot = treeItemDirMap.get(Util.join(Constant.KEY_SEPARATOR, i - 1, parts));
                if(hasRoot==null){
                    hasRoot=root;
                }
                if (isLeaf) {
                    childNode = new CheckBoxTreeItem<>(KeyTreeNode.leaf(key));
                    if(hasRoot.getValue()!=null){
                        childNode.getValue().setParent(hasRoot.getValue());
                        hasRoot.getValue().addChildKeyCount();
                    }
                }else {
                    String thisPrefix = Util.join(Constant.KEY_SEPARATOR, i, parts);
                    childNode = treeItemDirMap.get(thisPrefix);
                    //是目录先从缓存取，可能已经存在，存在的话不用做任何操作
                    if(childNode!=null){
                      continue;
                    }
                    //目录的话，直接设置图标
                    childNode = new CheckBoxTreeItem<>(KeyTreeNode.dir(part),new FontIcon(Feather.FOLDER));
                    treeItemDirMap.put(thisPrefix,childNode);
                    if(hasRoot.getValue()!=null){
                        childNode.getValue().setParent(hasRoot.getValue());
                    }
                }
                hasRoot.getChildren().add(childNode);
            }
        }
        sortTreeItems(root);
    }

    /**
     * 找到根节点下的所有子目录
     * @param root 根节点
     * @return 子目录节点
     */
    private Map<String, TreeItem<KeyTreeNode>> findTreeItemDir(TreeItem<KeyTreeNode> root) {
        Map<String, TreeItem<KeyTreeNode>> directoryNodes = new HashMap<>();
        Deque<TreeItem<KeyTreeNode>> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeItem<KeyTreeNode> node = stack.pop();
            if (!node.isLeaf()) {
                if(node.getValue()!=null){
                    directoryNodes.put(node.getValue().getPrefix(), node);
                }
                List<TreeItem<KeyTreeNode>> children = node.getChildren();
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(children.get(i));
                }
            }
        }
        return directoryNodes;
    }

    /**
     * 递归查子节点
     * @param parent 父节点
     * @param part 子节点名称
     * @return 子节点
     */
    private TreeItem<KeyTreeNode> findChild(TreeItem<KeyTreeNode> parent, String part) {
        for (TreeItem<KeyTreeNode> child : parent.getChildren()) {
            if (part.equals(child.getValue().getName())) {
                return child;
            }
        }
        return null;
    }


    /**
     * 递归树节点，将所有目录下存在子节点的进行排序
     * @param node 节点
     */
    private void sortTreeItems(TreeItem<KeyTreeNode> node) {
        if (node != null && !node.getChildren().isEmpty()) {
            node.getChildren().sort(treeItemSortComparator());
            // 递归排序子节点
            for (TreeItem<KeyTreeNode> child : node.getChildren()) {
                sortTreeItems(child);
            }
        }
    }


    /**
     * 自适应宽高
     */
    private void initAutoWah() {
        // 设置ChoiceBox的宽度自适应
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        newKey.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(choiceBox, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(newKey, javafx.scene.layout.Priority.ALWAYS);
        //搜索按钮不需要绑定宽度了，现在改为了CustomTextField内嵌按钮
//        search.prefWidthProperty().bind(newKey.widthProperty());
    }

    /**
     * 选中父节点就把子节点全选
     * FIXME 待完善 目前没生效，可能是为刷新的缘故，但是目前也不需要选择所有子节点
     * @param parent 父节点
     */
    private void selectChildren(TreeItem<KeyTreeNode> parent) {
        if (parent == null) {
            return;
        }
        if (!parent.isLeaf()) {
            parent.setExpanded(true);
            for (TreeItem<KeyTreeNode> child : parent.getChildren()) {
                treeView.getSelectionModel().select(child);
                selectChildren(child);
            }
        }
    }


    private boolean showReport;

    /**
     * 模糊搜索
     *
     * @param actionEvent 触发事件
     */
    public void search(ActionEvent actionEvent) {
        async(() -> {
            resetScanner();
            List<String> keys = scanner.scan();
            //key已经查出来,只管展示
            initTreeView(keys);
            //得刷新一下，不然会出现目录和叶子节点未对齐的显示问题
            treeView.refresh();
            updateProgressBar();
            //搜索不是空，就加入历史记录
            if(DataUtil.isNotEmpty(searchText.getText())){
                recentHistory.add(searchText.getText());
            }
            if(!showReport){
                showReport=true;
                PauseTransition delay = new PauseTransition(Duration.millis(100));
                delay.setOnFinished(event -> Platform.runLater(() -> report(null)));
                delay.play();

            }
        });

    }

    /**
     * 加载进度更新
     */
    private void updateProgressBar() {
        int dbSize = this.choiceBox.getSelectionModel().getSelectedItem().getDbSize();
        final double progress = (double) scanner.getSum() / dbSize;
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            // 如果你有Label用于显示百分比，也可以在这里更新
            progressText.setText(String.format("%.1f%%", progress * 100));
            progressBarLanguage();
        });
    }

    /**
     * 重置查询器
     */
    private void resetScanner() {
        String type=null;
        if(this.searchTypeMenuGroup.getSelectedToggle()!=null){
            type = ((RadioMenuItem) this.searchTypeMenuGroup.getSelectedToggle()).getText();
            if(ALL_TYPES.equals(type)){
                type=null;
            }
        }
        scanner.init(searchText.getText(),SCAN_COUNT,type,this.isLike.isSelected());
        Platform.runLater(() -> {
            progressBar.setProgress(0);
            progressText.setText(String.format("%.1f%%", 0d));
        });

    }

    /**
     * 打开key
     *
     * @param actionEvent 触发事件
     */
    public void open(ActionEvent actionEvent)  {
        if(!this.lastSelectedNode.isLeaf()){
            this.lastSelectedNode.setExpanded(true);
            return;
        }
        String key = this.lastSelectedNode.getValue().getKey();
        String keyType = exeRedis(j -> j.type(key));
        String type = RedisDataTypeEnum.getByType(keyType).type;
        if (Objects.equals(type, RedisDataTypeEnum.UNKNOWN.type)) {
            throw new GeneralException("This type is not supported " + keyType);
        }
        Tuple2<AnchorPane, BaseClientController<?>> tuple2 = loadFxml("/fxml/KeyTabView.fxml");
        AnchorPane borderPane = tuple2.t1();
        BaseClientController<?> controller = tuple2.t2();
        PassParameter passParameter = new PassParameter(PassParameter.NONE);
        passParameter.setDb(this.currentDb);
        passParameter.setKey(key);
        passParameter.setKeyType(type);
        passParameter.setRedisClient(redisClient);
        passParameter.setRedisContext(redisContext);
        StringProperty keySend = passParameter.keyProperty();
        //操作的kye和子界面进行绑定,这样更新key就会更新树节点
        keySend.addListener((observable, oldValue, newValue) -> {
            KeyTreeNode value = this.lastSelectedNode.getValue();
            value.setKey(newValue);
            //列表可以直接更新
            if(!this.redisContext.getRedisConfig().isTreeShow()){
                updateNodeAddress(value);
                return;
            }
            //数列表要特殊处理咯
            treeItemRename(treeView.getRoot(), this.lastSelectedNode,oldValue);
        });
        controller.setParameter(passParameter);
        Tab tab = new Tab(String.format("%s|%s|%s", this.currentDb,type, key));
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
              this.selectTabKey=controller.getParameter().getKey();
            }
        });
        tab.setGraphic(GuiUtil.creatKeyIcon());
        setTab(tab,tuple2);
        addOpenTreeItems();
    }

    /**
     * 打开的key添加到缓存
     */
    private void addOpenTreeItems() {
        if(this.openTreeItems.size()>=10){
            this.openTreeItems.removeFirst();
        }
        this.openTreeItems.add(new WeakReference<>(this.lastSelectedNode));

    }

    /**
     * 当一个树节点，重命名了，那有可能成层级变更，需要移动位置了
     * 也有可能出现新增层级，因为是根据:分隔符来进行判断是否有层级
     * @param root 树根
     * @param renameItem 已经重命名的节点
     * @param oldValue 重命名节点的旧名字
     */
    private void treeItemRename(TreeItem<KeyTreeNode> root, TreeItem<KeyTreeNode> renameItem, String oldValue) {

        String key = renameItem.getValue().getKey();
        String keySeparator = this.redisContext.getRedisConfig().getKeySeparator();
        String[] oldSplit = oldValue.split(keySeparator);
        String[] newSplit = key.split(keySeparator);
        //都不存在父目录的情况，直接更新
        if(oldSplit.length==1&&newSplit.length==1){
            updateNodeAddress(renameItem.getValue());
            return;
        }
        boolean isSameLevel = true;
        if(oldSplit.length==newSplit.length){
            for (int i = 0; i < oldSplit.length-1; i++) {
                if(!oldSplit[i].equals(newSplit[i])){
                    isSameLevel = false;
                    break;
                }
            }
        }else {
            isSameLevel = false;
        }
        //同级也直接更新
        if(isSameLevel){
            updateNodeAddress(renameItem.getValue());
            return;
        }
        TreeItem<KeyTreeNode> parent = renameItem.getParent();
        // 从旧位置删除节点
        subChildKeyCount(parent);
        renameItem.getParent().getChildren().remove(renameItem);
        // 如果旧位置的父节点没有其他子节点，则递归删除空的父目录
        removeEmptyParent(parent);
        // 将节点添加到新位置
        TreeItem<KeyTreeNode> newTreeItem = treeNodePutDir(root, renameItem.getValue());
        // 更新选中节点
        if (newTreeItem != null) {
            selectAndScrollTo(newTreeItem);
        } else {
            updateNodeAddress(renameItem.getValue());
        }

    }

    /**
     * 选中和定位
     * @param newTreeItem 新节点
     */
    private void selectAndScrollTo(TreeItem<KeyTreeNode> newTreeItem) {
        TreeItem<KeyTreeNode> newParent = newTreeItem.getParent();
        treeView.getSelectionModel().clearSelection();
        treeView.refresh();

        // 展开从根节点到目标节点的所有父节点
        while (newParent != null) {
            newParent.setExpanded(true);
            newParent = newParent.getParent();
        }
        this.lastSelectedNode = newTreeItem;
        treeView.getSelectionModel().select(this.lastSelectedNode);
        //列表直接定位到第一个，树节点需要滚动定位
        if(redisContext.getRedisConfig().isTreeShow()){
            Platform.runLater(() -> treeView.scrollTo(treeView.getRow(this.lastSelectedNode)));
        }

    }

    /**
     * 给节点切换成新地址
     * @param newNode 新节点
     */
    private void updateNodeAddress( KeyTreeNode newNode) {
        KeyTreeNode keyTreeNode = new KeyTreeNode();
        //新对象触发ui更新，应该是equals判断，但是这里sheKey导致equals也是相当的，还是得负责对象，不如直接copy一个新对象
        TUtil.copyProperties(newNode,keyTreeNode);
        this.lastSelectedNode.setValue(keyTreeNode);
    }

    /**
     * 树节点减除子节点数量
     * @param dirNode 目录节点
     */
    private void subChildKeyCount(TreeItem<KeyTreeNode> dirNode) {
        if(dirNode!=null&&dirNode.getValue()!=null){
            dirNode.getValue().subChildKeyCount();
        }
    }

    /**
     * 树节点增加子节点数量
     * @param dirNode 目录节点
     */
    private void addChildKeyCount(TreeItem<KeyTreeNode> dirNode) {
        if(dirNode!=null&&dirNode.getValue()!=null){
            dirNode.getValue().addChildKeyCount();
        }
    }

    /**
     * 递归删除空的父目录节点
     * @param dir 父节点,只能是目录
     */
    private void removeEmptyParent(TreeItem<KeyTreeNode> dir) {
        // 如果是根节点或叶子节点，不处理
        if (dir == null || dir == treeView.getRoot()) {
            return;
        }
        // 如果父节点没有子节点了，并且属于目录类型，就删掉
        if (dir.getChildren().isEmpty()) {
            TreeItem<KeyTreeNode> grandParent = dir.getParent();
            if (grandParent != null) {
                grandParent.getChildren().remove(dir);
                // 递归检查上层节点是否也需要删除
                removeEmptyParent(grandParent);
            }
        }
    }

    /**
     * 树节点尝试放入目录
     * 当节点重命名、新增的时候，节点位置可能存在变更
     * @param root 树根
     * @param keyTreeNode 节点数据
     * @return 新创建的树节点或null（如果未创建）
     */
    private TreeItem<KeyTreeNode> treeNodePutDir(TreeItem<KeyTreeNode> root, KeyTreeNode keyTreeNode) {
        if(!this.redisContext.getRedisConfig().isTreeShow()){
            TreeItem<KeyTreeNode> keyTreeNodeTreeItem = new CheckBoxTreeItem<>(keyTreeNode);
            root.getChildren().addFirst(keyTreeNodeTreeItem);
            return  keyTreeNodeTreeItem;
        }
        String key = keyTreeNode.getKey();
        String keySeparator = this.redisContext.getRedisConfig().getKeySeparator();
        String[] parts = key.split(keySeparator);
        TreeItem<KeyTreeNode> current = root;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            // 叶子节点是key类型
            boolean isLeaf = (i == parts.length - 1);

            TreeItem<KeyTreeNode> childNode = findChild(current, part);
            if (childNode == null || isLeaf) {
                if (isLeaf) {
                    childNode = new CheckBoxTreeItem<>(keyTreeNode);
                } else {
                    childNode = new CheckBoxTreeItem<>(KeyTreeNode.dir(part), new FontIcon(Feather.FOLDER));
                }
                TreeItem<KeyTreeNode> finalChildNode = childNode;

                if (isLeaf) {
                    current.getChildren().add(finalChildNode);
                    if (current.getValue() != null) {
                        finalChildNode.getValue().setParent(current.getValue());
                        current.getValue().addChildKeyCount();
                    }
                } else {
                    current.getChildren().addFirst(finalChildNode);
                    if (current.getValue() != null) {
                        // 不是叶子节点，不用计数
                        finalChildNode.getValue().setParent(current.getValue());
                    }
                }
                current.getChildren().sort(treeItemSortComparator());
            }

            current = childNode;
        }

        return current;
    }


    /**
     * 控制台
     * @param actionEvent 触发事件
     */
    @FXML
    public void console(ActionEvent actionEvent) throws IOException {
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadClientFxml("/fxml/ConsoleView.fxml",PassParameter.CONSOLE);
        Tab tab = new Tab(Constant.CONSOLE_TAB_NAME);
        tab.setGraphic(GuiUtil.creatConsoleIcon());
        setTab(tab,tuple2);
    }

    /**
     * 根据名字找tab
     * @param tabName tab名字
     * @return tab
     */
    private Tab findTabByName(String tabName) {
        return this.dbTabPane.getTabs().stream().filter(tab -> tab.getText().equals(tabName)).findFirst().orElse(null);
    }

    /**
     * 设置Tab 封装对新建tab的设置
     * @param tab tab
     * @param tuple2 tuple2
     */
    private void setTab(Tab tab, Tuple2<? extends Node, ? extends BaseClientController<?>> tuple2) {
        GuiUtil.setTab(tab,this.dbTabPane,tuple2);
    }

    @FXML
    public void monitor(ActionEvent actionEvent) {
        Tab tab=findTabByName(Constant.MONITOR_TAB_NAME);
        if(tab!=null){
            this.dbTabPane.getSelectionModel().select(tab);
            return;
        }
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadClientFxml("/fxml/MonitorView.fxml",PassParameter.MONITOR);
        tab = new Tab(Constant.MONITOR_TAB_NAME);
        tab.setGraphic(GuiUtil.creatMonitorIcon());
        setTab(tab,tuple2);
    }

    @FXML
    public void report(ActionEvent actionEvent)  {
        Tab tab=findTabByName(Constant.REPORT_TAB_NAME);
        if(tab!=null){
            this.dbTabPane.getSelectionModel().select(tab);
            return;
        }
        Tuple2<ScrollPane,ReportController> tuple2 = loadClientFxml("/fxml/ReportView.fxml",PassParameter.REPORT);
        tab = new Tab(Constant.REPORT_TAB_NAME);
        tab.setGraphic(GuiUtil.creatInfoIcon());
        setTab(tab,tuple2);
    }

    @FXML
    public void pubsub(ActionEvent actionEvent) {
        Tab tab=findTabByName(Constant.PUBSUB_TAB_NAME);
        if(tab!=null){
            this.dbTabPane.getSelectionModel().select(tab);
            return;
        }
        Tuple2<AnchorPane,PubSubController> tuple2 = loadClientFxml("/fxml/PubSubView.fxml",PassParameter.PUBSUB);
        tab = new Tab(Constant.PUBSUB_TAB_NAME);
        tab.setGraphic(GuiUtil.creatPubSubIcon());
        setTab(tab,tuple2);
    }

    /**
     * 删除key,包括多选的
     *
     */
    @FXML
    public void delete(ActionEvent actionEvent) {
        final List<String> delKeys=new ArrayList<>();
        // 获取选中的节点
        final List<TreeItem<KeyTreeNode>> delItems =new ArrayList<>();
        if(!checkBox.isSelected()){
            delItems.addAll(getSelectionLeafNodes());
            delItems.forEach(item -> delKeys.add(item.getValue().getKey()));
            //选择多个key，要弹出列表确认
            if(delItems.size()>1){
                if(!keyConfirm(delItems.stream().map(TreeItem::getValue).toList(), MultipleKeyController.DELETE).t1()){
                    return;
                }
            }else {
                if(GuiUtil.alertRemove(delKeys.getFirst())){
                    return;
                }
            }
        }else {
            delItems.addAll(getCheckLeafNodes());
            List<KeyTreeNode> list = delItems.stream().map(TreeItem::getValue).toList();
            delKeys.addAll(list.stream().map(KeyTreeNode::getKey).toList());
            if(!keyConfirm(list, MultipleKeyController.DELETE).t1()){
                return;
            }
        }
        if(delKeys.isEmpty()){
            return;
        }
        deleteTreeItems(delItems);
        //删除服务器的key
        async(()-> exeRedis(j -> j.del(delKeys.toArray(new String[0]))));
        //删除对应打开的tab
        removeTabByKeys(delKeys);
    }

    /**
     * 获取选中的叶子节点
     * @return 选中的叶子节点
     */
    private List<TreeItem<KeyTreeNode>> getSelectionLeafNodes() {
        List<TreeItem<KeyTreeNode>> delItems =new ArrayList<>();
        treeView.getSelectionModel().getSelectedItems().forEach(item -> {
            if (item != treeView.getRoot()) {
                //叶子节点是连接,需要删除redis上的key
                if(item.isLeaf()){
                    delItems.add(item);
                }

            }
        });
        return delItems;
    }

    /**
     * 从tree列表中去掉需要删除的key
     * @param delItems 需要删除的节点
     */
    private void deleteTreeItems(List<TreeItem<KeyTreeNode>> delItems) {
        //如果是列表，那都是同一个父节点，直接删除
        if(!this.redisContext.getRedisConfig().isTreeShow()){
            TreeItem<KeyTreeNode> parent = delItems.getFirst().getParent();
            delItems.forEach(item -> parent.getChildren().remove(item));
            return;
        }
        //树节点需要，特殊处理层级关系
        for (TreeItem<KeyTreeNode> delItem : delItems) {
            TreeItem<KeyTreeNode> parent = delItem.getParent();
            //删除选中节点
            parent.getChildren().remove(delItem);
            //计数器-1
            subChildKeyCount(parent);
            //删除控目录
            removeEmptyParent(parent);
        }
    }

    /**
     * 删除对应key的tab
     * @param delKeys 删除的key
     */
    public void removeTabByKeys(List<String> delKeys) {
        List<Tab> delTabs = new ArrayList<>();
        for (Tab tab : dbTabPane.getTabs()) {
            BaseClientController<?> controller =(BaseClientController<?>) tab.getContent().getUserData();
            String key = controller.getParameter().getKey();
            if(delKeys.contains(key)){
                delTabs.add(tab);
            }
        }
        dbTabPane.getTabs().removeAll(delTabs);
    }

    /**
     * 清空
     *
     * @param actionEvent 触发事件
     */
    @FXML
    public void flush(ActionEvent actionEvent) {
        if(!GuiUtil.alert(Alert.AlertType.CONFIRMATION,Main.RESOURCE_BUNDLE.getString(Constant.ALERT_MESSAGE_DEL_FLUSH) )){
            return;
        }
        async(()->{
            exeRedis(RedisClient::flushDB);
            Platform.runLater(()-> treeView.getRoot().getChildren().clear());
        });
    }



    /**
     * 删除单个treeView对应的key,由子层调用
     * @param p 删除参数
     */
    public boolean delKey(ObjectProperty<PassParameter> p) {
        //如果treeView是的db和删除key的db相同,则需要对应删除treeView中的节点
        if(p.get().getDb()==this.currentDb){
            Platform.runLater(()->{
                TreeItem<KeyTreeNode> find=tryFindTreeItemByKey(treeView.getRoot(), p.get().getKey());
                if(find!=null){
                    deleteTreeItems( List.of(find));
                }
            });
        }
        return true;
    }

    /**
     * 尝试找key的节点
     * @param root 树根
     * @param key key
     * @return 找到的节点
     */
    private TreeItem<KeyTreeNode> tryFindTreeItemByKey(TreeItem<KeyTreeNode> root, String key) {
        TreeItem<KeyTreeNode> find;
        //先从以打开的列表中找，找不到在从根节点递归找
        find = findTreeItemByKeyInOpenTreeItems( key);
        if(find==null){
            find = findTreeItemByKey(root, key);
        }
        return find;
    }

    /**
     * 从打开的key列表中查找
     * @param key key
     * @return 找到的节点
     */
    private TreeItem<KeyTreeNode> findTreeItemByKeyInOpenTreeItems(String key) {
        for (WeakReference<TreeItem<KeyTreeNode>> openTreeItem : this.openTreeItems) {
            TreeItem<KeyTreeNode> keyTreeNodeTreeItem = openTreeItem.get();
            if((keyTreeNodeTreeItem!=null&&keyTreeNodeTreeItem.getValue()!=null)&&keyTreeNodeTreeItem.getValue().getKey().equals(key)){
                return openTreeItem.get();
            }
        }
        return null;
    }

    /**
     * 递归方法，根据key查找并TreeView中的节点
     * @param parent 父节点
     * @param key key
     */
    public  TreeItem<KeyTreeNode> findTreeItemByKey(TreeItem<KeyTreeNode> parent, String key) {
        for (TreeItem<KeyTreeNode> child : parent.getChildren()) {
            if (child.getValue().getKey()!=null&&child.getValue().getKey().equals(key)) {
                return child;
            }
            // 如果当前节点不匹配且不是叶子节点，则递归搜索其子节点
            if (!child.isLeaf()) {
                TreeItem<KeyTreeNode> result = findTreeItemByKey(child, key);
                // 只有在找到匹配节点时才返回，否则继续搜索其他兄弟节点
                if (result != null) {
                    return result;
                }
            }
        }
        return null;

    }

    /**
     * 新增key并选中
     * @param p 参数
     */
    public void addKeyAndSelect(ObjectProperty<PassParameter> p) {
        //如果treeView是的db和删除key的db相同,则需要对应删除treeView中的节点
        if(p.get().getDb()==this.currentDb){
            Platform.runLater(()->{
                TreeItem<KeyTreeNode> newTreeItem = treeNodePutDir(treeView.getRoot(), KeyTreeNode.newLeaf(p.get().getKey(),p.get().getKeyType()));
                selectAndScrollTo(newTreeItem);
                open(null);
            });
        }
    }



    /**
     * db单选框点击则刷新
     * 或则全部重新加进去,然后再选中上次的
     *  不能通过点击就去刷新db,改为refresh手动刷新了
     * @param mouseEvent 触发事件
     */
    @Deprecated
    @FXML
    public void onChoiceBoxMouseClicked(MouseEvent mouseEvent) {
    }

    /**
     * 刷新db
     * 同时会触发db的选择事件,触发search
     * @param actionEvent 触发事件
     */
    @FXML
    public void refresh(ActionEvent actionEvent) {
        resetDbSelects();
    }

    public void reset(ActionEvent actionEvent) {
        searchText.setText("");
    }

    /**
     * 清空搜索记录
     */
    public void clearHistory(ActionEvent actionEvent) {
        this.recentHistory.clear();
    }

    /**
     * 隐藏工具栏
     */
    @FXML
    public void hideToolbar(ActionEvent actionEvent) {
        toolBar.setVisible(false);
        toolBar.setManaged(false);
        showButton.setVisible(true);
    }

    /**
     * 显示工具栏
     */
    @FXML
    public void showToolbar(ActionEvent actionEvent) {
        toolBar.setVisible(true);
        toolBar.setManaged(true);
        showButton.setVisible(false);
    }

    /**
     * 加载更多
     */
    @FXML
    public void loadMore(ActionEvent actionEvent) {
        ThreadPool.getInstance().execute(() -> {
            if(scanner.getSum()>=this.choiceBox.getSelectionModel().getSelectedItem().getDbSize()){
                return;
            }
            List<String> keys = scanner.scan();
            loadIntoTreeView(keys);
            updateProgressBar();
        });

    }

    /**
     * 加载所有
     */
    @FXML
    public void loadAll(ActionEvent actionEvent) {
        ThreadPool.getInstance().execute(() -> {
            if(scanner.getSum()>=this.choiceBox.getSelectionModel().getSelectedItem().getDbSize()){
                return;
            }
            List<String> keys = scanner.setCount(SCAN_COUNT*200).scan();
            while (!keys.isEmpty()){
                loadIntoTreeView(keys);
                updateProgressBar();
                keys = scanner.scan();
            }
        });

    }

    /**
     * 跳转到打开key窗口/最后选中窗口
     */
    @FXML
    public void location(ActionEvent actionEvent) {
        if(this.selectTabKey!=null){
            TreeItem<KeyTreeNode> keyTreeNodeTreeItem = tryFindTreeItemByKey(treeView.getRoot(), this.selectTabKey);
            if(keyTreeNodeTreeItem==null){
                return;
            }
            treeView.scrollTo(treeView.getRow(keyTreeNodeTreeItem));
            this.lastSelectedNode=keyTreeNodeTreeItem;
            selectAndScrollTo(this.lastSelectedNode);
        }
    }

    /**
     * 树节点展开所有
     */
    @FXML
    public void expanded(ActionEvent actionEvent) {
        GuiUtil.expandAllNodes(treeView.getRoot(),true);
    }


    /**
     * 树节点折叠所有
     */
    @FXML
    public void collapse(ActionEvent actionEvent) {
        GuiUtil.expandAllNodes(treeView.getRoot(),false);
    }


    @FXML
    public void tooBarRightHide(MouseEvent mouseEvent) {
        toolBarRight.setVisible(false);
    }

    @FXML
    public void tooBarRightShow(MouseEvent mouseEvent) {
        toolBarRight.setVisible(true);
    }

    /**
     * 模糊匹配切换事件
     */
    @FXML
    public void isLikeChange(ActionEvent actionEvent) {
        search(null);
    }

    @Override
    public void close() {
        super.close();
        //key的服务tab都关闭了，那所有连接都要关闭
        this.redisContext.close();
    }

    @FXML
    public void checkBox(ActionEvent actionEvent) {
        boxToolBar.setVisible(checkBox.isSelected());
        boxToolBar.setManaged(checkBox.isSelected());
        treeView.refresh();
    }

    @FXML
    public void export(ActionEvent actionEvent)   {
        List<KeyTreeNode> list = new ArrayList<>();
        //默认是需要导出ttl
        boolean pttlEnable = true;
        // 获取选中的节点
        if(!checkBox.isSelected()){
            list.addAll(getSelectionLeafNodes().stream().map(TreeItem::getValue).toList());
            //选择多个key，要弹出列表确认
            if(list.size()>1){
                Tuple2<Boolean, Boolean> tuple2 = keyConfirm(list, MultipleKeyController.EXPORT);
                pttlEnable=tuple2.t2();
                if(!tuple2.t1()){
                    return;
                }
            }
        }else {
            list.addAll(getCheckLeafNodes().stream().map(TreeItem::getValue).toList());
            Tuple2<Boolean, Boolean> tuple2 = keyConfirm(list, MultipleKeyController.EXPORT);
            pttlEnable=tuple2.t2();
            if(!tuple2.t1()){
                return;
            }
        }
        if(list.isEmpty()){
            return;
        }
        File file = GuiUtil.saveFileChoose(this.root.getScene().getWindow(), lastFile==null?null:lastFile.get(), "dump_%s.csv".formatted(System.currentTimeMillis()));
        if(file==null){
            return;
        }
        lastFile=new SoftReference<>(file);
        boolean finalPttlEnable = pttlEnable;
        List<Object> pipelineResults = exeRedis(j -> j.executePipelined(commands -> {
            for (KeyTreeNode keyTreeNode : list) {
                commands.dump(keyTreeNode.getKey());
                if(finalPttlEnable){
                    commands.pttl(keyTreeNode.getKey());
                }
            }
        },this.currentDb));
        StringBuilder csvContent = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            String key = FileUtil.byte2HexString(list.get(i).getKey().getBytes());
            csvContent.append(key).append(",");
            int n=i;
            if(finalPttlEnable){
                n*=2;
            }
            String value = FileUtil.byte2HexString((byte[]) pipelineResults.get(n));
            csvContent.append(value);
            if(finalPttlEnable){
                long ttl = (long) pipelineResults.get(n+1);
                csvContent.append(",").append(ttl);
            }
            csvContent.append("\n");
        }
        FileUtil.byteWrite2file(csvContent.toString().getBytes(), file.getAbsolutePath());

    }

    /**
     * 递归收集所有选中的叶子节点
     *
     */
    private List<TreeItem<KeyTreeNode>> getCheckLeafNodes() {
        List<TreeItem<KeyTreeNode>> checkedLeafNodes = new ArrayList<>();
        for (TreeItem<KeyTreeNode> child : treeView.getRoot().getChildren()) {
            collectCheckedLeafNodes(child, checkedLeafNodes);
        }
        return checkedLeafNodes;
    }

    /**
     * 多个key操作确认
     * @param list 待操作的key列表
     * @param model 操作类型
     * @return 是否确认
     */
    private Tuple2<Boolean,Boolean> keyConfirm(List<KeyTreeNode> list, int model) {
        Tuple2<AnchorPane,MultipleKeyController> tuple2 = loadFxml("/fxml/MultipleKeyView.fxml");
        AnchorPane borderPane = tuple2.t1();
        Stage stage = GuiUtil.createSubStage("", borderPane, root.getScene().getWindow());
        // 创建 CompletableFuture 用于等待结果
        CompletableFuture<Tuple2<Boolean,Boolean>> future = new CompletableFuture<>();
        // 将 stage 和 future 传递给控制器
        tuple2.t2().setCurrentStage(stage);
        tuple2.t2().setResultFuture(future);
        tuple2.t2().setModel(model);
        tuple2.t2().setKeys(list);
        // 显示 Stage
        stage.showAndWait();
        // 等待结果
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("key confirm exception",e);
           return new Tuple2<>(false,false);
        }
    }

    /**
     * 递归收集所有被选中的叶子节点
     * @param node 当前节点
     * @param result 结果列表
     */
    private void collectCheckedLeafNodes(TreeItem<KeyTreeNode> node, List<TreeItem<KeyTreeNode>> result) {
        if (node == null){
            return;
        }
        if (node.isLeaf()) {
            // 叶子节点：只有当节点被选中时才添加
            if (isNodeChecked(node)) {
                result.add(node);
            }
        } else {
            // 父节点：如果被选中，则其下所有叶子节点都被视为选中 这也是不可靠的，如果子节点选中，父节点有可能未被选中，这就很坑，只能全部遍历子节点才可靠
            for (TreeItem<KeyTreeNode> child : node.getChildren()) {
                collectCheckedLeafNodes(child, result);
            }
        }
    }

    /**
     * 检查节点是否被选中
     * @param node 要检查的节点
     * @return 是否选中
     */
    private boolean isNodeChecked(TreeItem<KeyTreeNode> node) {
        if (node instanceof CheckBoxTreeItem<?> cbt) {
            return cbt.isSelected();
        }
        return false;
    }


    @FXML
    public void cancel(ActionEvent actionEvent) {
        checkBox.setSelected(false);
        checkBox(actionEvent);
    }

    @FXML
    public void selectAll(ActionEvent actionEvent) {
        for (TreeItem<KeyTreeNode> child : treeView.getRoot().getChildren()) {
            if (child instanceof CheckBoxTreeItem<?> cbt) {
                cbt.setSelected(boxSelectAll.isSelected());
            }
        }
    }
}
