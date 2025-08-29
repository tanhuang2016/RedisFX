package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.common.util.TUtil;
import xyz.hashdog.rdm.redis.client.RedisKeyScanner;
import xyz.hashdog.rdm.ui.Main;
import xyz.hashdog.rdm.ui.common.*;
import xyz.hashdog.rdm.ui.controller.base.BaseClientController;
import xyz.hashdog.rdm.ui.entity.DBNode;
import xyz.hashdog.rdm.ui.entity.KeyTreeNode;
import xyz.hashdog.rdm.ui.entity.PassParameter;
import xyz.hashdog.rdm.ui.entity.config.KeyTabPaneSetting;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.RecentHistory;
import xyz.hashdog.rdm.ui.util.SvgManager;
import xyz.hashdog.rdm.ui.util.Util;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;
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
    public Button searchOptionsButton;
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

    /**
     * 缓存的图标加载任务，用于批量处理类型tag，避免线程切换的开销
     */
    private final Queue<TreeItem<KeyTreeNode>> iconLoadQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isLoading = new AtomicBoolean(false);

    private final static int SCAN_COUNT = 500;
    private RedisKeyScanner scanner;


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

    @FXML
    public void initialize() {
        initRecentHistory();
        initNewKey();
        initAutoWah();
        initTreeViewRoot();
        initListener();
        initButton();
        initTextField();
        initTabPane();
        progressBar.getStyleClass().add(Styles.SMALL);
        initLanguage();



    }

    /**
     * 显示历史搜索记录
     */
    private void showHistoryPopup() {
        history.show(search,
                searchText.localToScreen(0, 0).getX(),
                searchText.localToScreen(0, 0).getY() + searchText.getHeight());
    }

    private void initLanguage() {
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
    }

    private void progressBarLanguage() {
        if(scanner==null){
            return;
        }
        progressBar.setTooltip(GuiUtil.textTooltip(String.format(language("server.toolBar.progress"),scanner.getSum())));
        progressText.setTooltip(GuiUtil.textTooltip(String.format(language("server.toolBar.progress"),scanner.getSum())));
    }

    private void initTabPane() {
        KeyTabPaneSetting ksetting =Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAB_PANE.name);
        this.dbTabPane.setSide(Side.valueOf(ksetting.getSide()));
    }

    /**
     * 最近使用搜索记录初始化
     */
    private void initRecentHistory() {
        history = new ContextMenu();
        history.getItems().add(new SeparatorMenuItem());
        MenuItem clearItem = new MenuItem(language("server.clear"));
        clearItem.setOnAction(this::clearHistory);
        history.getItems().add(clearItem);
        recentHistory = new RecentHistory<String>(5,new RecentHistory.Noticer<String>() {
            @Override
            public void notice(List<String> list) {
                doRecentHistory(list);
            }
        });
        doRecentHistory(recentHistory.get());
    }

    /**
     * 创建搜索记录的菜单项
     * @param str 历史记录名称
     * @return 菜单项
     */
    private MenuItem createSearchHistoryMenuItem(String str) {
        MenuItem menuItem = new MenuItem(str);
        menuItem.setOnAction(event -> {
            searchText.setText(menuItem.getText());
        });
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
        isLike.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT,UiStyles.MINI,UiStyles.SEMI_CIRCLE);
        reset.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT,UiStyles.MINI,Styles.ROUNDED);
        searchOptionsButton.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT,UiStyles.MINI,UiStyles.SEMI_CIRCLE);
        search.setCursor(Cursor.HAND);
        reset.setCursor(Cursor.HAND);
        isLike.setCursor(Cursor.HAND);
        searchOptionsButton.setCursor(Cursor.HAND);
        newKey.getStyleClass().addAll(Tweaks.NO_ARROW);
        initToolBarButtonStyles(locationButton,expandedButton,collapseButton,optionsButton,hideButton,showButton);
        loadMore.getStyleClass().addAll(Styles.SMALL, UiStyles.MINI);
        loadAll.getStyleClass().addAll(Styles.SMALL,Styles.DANGER, UiStyles.MINI);
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
            items.add(new MenuItem(value.type));
        }
    }

    /**
     * 初始化监听时间
     */
    private void initListener() {
        userDataPropertyListener();
        choiceBoxSelectedLinstener();
        treeViewListener();
        newKeyListener();
        searchTextListener();
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
            }
        });
        // 添加键盘事件监听,alt+down 显示历史搜索记录
        searchText.setOnKeyPressed(event -> {
            if (event.isAltDown() && event.getCode() == KeyCode.DOWN) {
                showHistoryPopup();
                // 阻止事件继续传播
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
        treeView.setCellFactory(tv -> new TreeCell<KeyTreeNode>() {

            @Override
            protected void updateItem(KeyTreeNode item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 设置基本文本
                    setText(item.toString());
                    // 只有叶子节点首次显示时才加载图标
                    if (item.getLeaf()&&!item.isInitialized()) {
                        loadNodeGraphicIfNeeded(this.getTreeItem());
                        item.setInitialized(true);
                    }
                    // 如果图标已经加载过，直接显示
                    if (getTreeItem().getGraphic() != null) {
                        setGraphic(getTreeItem().getGraphic());
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
                // 批量处理队列中的所有节点
                TreeItem<KeyTreeNode> treeItem;
                int n=0;
                // 取出队列中所有待加载的节点
                while ((treeItem = iconLoadQueue.poll()) != null) {
                    KeyTreeNode item = treeItem.getValue();
                    String type = item.getType()==null?exeRedis(j -> j.type(item.getKey())):item.getType();
                    Label keyTypeLabel = GuiUtil.getKeyTypeLabel(type);
                    treeItem.setGraphic(keyTypeLabel);
                    // 标记节点已初始化
                    treeItem.getValue().setInitialized(true);
                    n++;
                }
                // 只在需要时刷新
                if (n>0) {
                    // 在UI线程中更新所有图标
                    Platform.runLater(() -> {
                        treeView.refresh();
                    });
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
    private void choiceBoxSelectedLinstener() {

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
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
//            ThreadPool.getInstance().execute(() -> {
//                this.redisClient.select(db);
//                search(null);
//            });
        });
    }

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
            initDBSelects();
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
    private void initDBSelects() {
        ObservableList<DBNode> items = choiceBox.getItems();
        ThreadPool.getInstance().execute(() -> {
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
    private void resetDBSelects(){

        DBNode selectedItem = choiceBox.getSelectionModel().getSelectedItem();
        ObservableList<DBNode> items= FXCollections.observableArrayList();
        ThreadPool.getInstance().execute(() -> {
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
        //shift 或则ctrol+鼠标单机为选取操作,会触发选中,选择父节点会同步选中子节点
        treeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) { // Check for single click
                if (event.isShiftDown() || event.isControlDown()) {
                    List<TreeItem<KeyTreeNode>> list = new ArrayList<>();
                    for (TreeItem<KeyTreeNode> selectedItem : treeView.getSelectionModel().getSelectedItems()) {
                        if (!selectedItem.isLeaf()) { // Check if the selected node is a parent node
                            list.add(selectedItem);

                        }
                    }
                    for (TreeItem<KeyTreeNode> selectedItem : list) {
                        //设置选中
                        selectChildren((TreeItem<KeyTreeNode>) selectedItem);
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
     * @param keys
     */
    private void buildListView( List<String> keys) {
        ObservableList<TreeItem<KeyTreeNode>> children = treeView.getRoot().getChildren();
        List<TreeItem<KeyTreeNode>> list = new ArrayList<>();
        for (String key : keys) {
            list.add(new TreeItem<>(KeyTreeNode.leaf(key)));
        }
        children.addAll( list);
    }

    /**
     * key排序
     * @return
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
     * KEY展示构建树形结构
     * 递归构建，新能不好，已经过时
     * @param keys
     */
    @Deprecated
    private void buildTreeViewOld(List<String> keys) {
        TreeItem<KeyTreeNode> root = treeView.getRoot();
        for (String key : keys) {
            String keySeparator = this.redisContext.getRedisConfig().getKeySeparator();
            String[] parts = key.split(keySeparator);
            TreeItem<KeyTreeNode> current = root;
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                //叶子节点是key类型
                boolean isLeaf = (i == parts.length - 1);
                TreeItem<KeyTreeNode> childNode = findChild(current, part);
                if (childNode == null|| isLeaf) {
                    if (isLeaf) {
                        childNode = new TreeItem<>(KeyTreeNode.leaf(key));
                    }else {
                        //目录的话，直接设置图标
                        childNode = new TreeItem<>(KeyTreeNode.dir(part),new FontIcon(Feather.FOLDER));
                    }
                    TreeItem<KeyTreeNode> finalChildNode = childNode;
                    if (isLeaf) {
                        current.getChildren().add(finalChildNode);
                        if(current.getValue()!=null){
                            finalChildNode.getValue().setParent(current.getValue());
                            current.getValue().addChildKeyCount();
                        }
                    }else {
                        current.getChildren().addFirst(finalChildNode);
                        if(current.getValue()!=null){
                            //不是叶子节点，不用计数
                            finalChildNode.getValue().setParent(current.getValue());
                        }
                    }
//                    current.getChildren().sort(treeItemSortComparator());
                }

                current = childNode;
            }
        }
        sortTreeItems(root);
    }

    /**
     * 控件换时间，利用缓存优化了key的树形结构构造，速度提升了10倍不止
     * @param keys
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
                    childNode = new TreeItem<>(KeyTreeNode.leaf(key));
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
                    childNode = new TreeItem<>(KeyTreeNode.dir(part),new FontIcon(Feather.FOLDER));
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
     * 根节点下的所有子目录
     * @param root
     * @return
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
     * @param node
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
     *
     * @param parent
     */
    private void selectChildren(TreeItem<KeyTreeNode> parent) {
        if (parent == null) {
            return;
        }
        if (!parent.isLeaf()) {
            parent.setExpanded(true); // Optional: Expand the parent to show all children
            for (TreeItem<KeyTreeNode> child : parent.getChildren()) {
                treeView.getSelectionModel().select(child);
                selectChildren(child); // Recursively select children of the child node
            }
        }
    }




    /**
     * 模糊搜索
     *
     * @param actionEvent
     */
    public void search(ActionEvent actionEvent) {
        ThreadPool.getInstance().execute(() -> {
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
        scanner.init(searchText.getText(),SCAN_COUNT,null,this.isLike.isSelected());
        Platform.runLater(() -> {
            progressBar.setProgress(0);
            progressText.setText(String.format("%.1f%%", 0d));
        });

    }

    /**
     * 打开key
     *
     * @param actionEvent
     */
    public void open(ActionEvent actionEvent)  {
        if(!this.lastSelectedNode.isLeaf()){
            this.lastSelectedNode.setExpanded(true);
            return;
        }
        String key = this.lastSelectedNode.getValue().getKey();
        String type = RedisDataTypeEnum.getByType(exeRedis(j -> j.type(key))).type;
        Tuple2<AnchorPane, BaseClientController> tuple2 = loadFxml("/fxml/KeyTabView.fxml");
        AnchorPane borderPane = tuple2.t1();
        BaseClientController controller = tuple2.t2();
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
        tab.setOnClosed(event2 -> {
            ThreadPool.getInstance().execute(()->{
                controller.close();
            });
        });
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
              this.selectTabKey=controller.getParameter().getKey();
            }
        });
        ContextMenu cm=GuiUtil.newTabContextMenu(tab);
        tab.setContent(borderPane);
        tab.setGraphic(GuiUtil.creatKeyIcon());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
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
            Platform.runLater(() -> {
                treeView.scrollTo(treeView.getRow(this.lastSelectedNode));
            });
        }

    }

    /**
     * 给节点切换成新地址
     * @param newNode
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
            TreeItem<KeyTreeNode> keyTreeNodeTreeItem = new TreeItem<>(keyTreeNode);
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
                    // 获取key的类型，用于显示对应的图标
//                    String type = exeRedis(j -> j.type(key));
//                    childNode = new TreeItem<>(KeyTreeNode.leaf(key));
                    childNode = new TreeItem<>(keyTreeNode);
//                    Label keyTypeLabel = GuiUtil.getKeyTypeLabel(type);
//                    childNode.setGraphic(keyTypeLabel);
                } else {
                    childNode = new TreeItem<>(KeyTreeNode.dir(part), new FontIcon(Feather.FOLDER));
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
     * @param actionEvent
     */
    @FXML
    public void console(ActionEvent actionEvent) throws IOException {
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadFxml("/fxml/ConsoleView.fxml");
        AnchorPane anchorPane = tuple2.t1();
        BaseClientController controller = tuple2.t2();
        PassParameter passParameter = new PassParameter(PassParameter.CONSOLE);
        passParameter.setDb(this.currentDb);
        passParameter.setRedisClient(redisContext.newRedisClient());
        passParameter.setRedisContext(redisContext);
        controller.setParameter(passParameter);
        Tab tab = new Tab("Console");
        if(passParameter.getTabType()==PassParameter.CONSOLE){
            // 监听Tab被关闭事件,但是remove是无法监听的
            tab.setOnClosed(event2 -> {
                ThreadPool.getInstance().execute(()->controller.getRedisClient().close());
            });
        }
        ContextMenu cm=GuiUtil.newTabContextMenu(tab);
        tab.setContent(anchorPane);
        tab.setGraphic(GuiUtil.creatConsoleIcon());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
    }
    @FXML
    public void monitor(ActionEvent actionEvent) throws IOException {
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadFxml("/fxml/MonitorView.fxml");
        AnchorPane anchorPane = tuple2.t1();
        BaseClientController controller = tuple2.t2();
        PassParameter passParameter = new PassParameter(PassParameter.MONITOR);
        passParameter.setDb(this.currentDb);
        passParameter.setRedisClient(redisContext.newRedisClient());
        passParameter.setRedisContext(redisContext);
        controller.setParameter(passParameter);
        Tab tab = new Tab("Monitor");
        if(passParameter.getTabType()==PassParameter.MONITOR){
            // 监听Tab被关闭事件,但是remove是无法监听的
            tab.setOnClosed(event2 -> {
                ThreadPool.getInstance().execute(()->{
                    controller.getRedisClient().close();
                    controller.close();
                });
            });
        }
        ContextMenu cm=GuiUtil.newTabContextMenu(tab);
        tab.setContent(anchorPane);
        tab.setGraphic(GuiUtil.creatMonitorIcon());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
    }

    @FXML
    public void report(ActionEvent actionEvent) throws IOException {
        Tuple2<ScrollPane,ConsoleController> tuple2 = loadFxml("/fxml/ReportView.fxml");
        Region anchorPane = tuple2.t1();
        BaseClientController controller = tuple2.t2();
        PassParameter passParameter = new PassParameter(PassParameter.MONITOR);
        passParameter.setDb(this.currentDb);
        passParameter.setRedisClient(redisContext.newRedisClient());
        passParameter.setRedisContext(redisContext);
        controller.setParameter(passParameter);
        Tab tab = new Tab("Report");
        if(passParameter.getTabType()==PassParameter.MONITOR){
            // 监听Tab被关闭事件,但是remove是无法监听的
            tab.setOnClosed(event2 -> {
                ThreadPool.getInstance().execute(()->{
                    controller.getRedisClient().close();
                    controller.close();
                });
            });
        }
        ContextMenu cm=GuiUtil.newTabContextMenu(tab);
        tab.setContent(anchorPane);
        tab.setGraphic(GuiUtil.creatInfoIcon());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
    }

    @FXML
    public void pubsub(ActionEvent actionEvent) throws IOException {
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadFxml("/fxml/PubSubView.fxml");
        AnchorPane anchorPane = tuple2.t1();
        BaseClientController controller = tuple2.t2();
        PassParameter passParameter = new PassParameter(PassParameter.PUBSUB);
        passParameter.setDb(this.currentDb);
        passParameter.setRedisClient(redisContext.newRedisClient());
        passParameter.setRedisContext(redisContext);
        controller.setParameter(passParameter);
        Tab tab = new Tab("Pub/Sub");
        if(passParameter.getTabType()==PassParameter.PUBSUB){
            // 监听Tab被关闭事件,但是remove是无法监听的
            tab.setOnClosed(event2 -> {
                ThreadPool.getInstance().execute(()->{
                    controller.getRedisClient().close();
                    controller.close();
                });
            });
        }
        ContextMenu cm=GuiUtil.newTabContextMenu(tab);
        tab.setContent(anchorPane);
        tab.setGraphic(GuiUtil.creatPubSubIcon());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
    }

    /**
     * 删除key,包括多选的
     *
     * @param actionEvent
     */
    @FXML
    public void delete(ActionEvent actionEvent) {
        if(!GuiUtil.alert(Alert.AlertType.CONFIRMATION, Main.RESOURCE_BUNDLE.getString(Constant.ALERT_MESSAGE_DEL))){
            return;
        }
        List<String> delKeys=new ArrayList<>();
        // 获取选中的节点
        List<TreeItem<KeyTreeNode>> delItems =new ArrayList<>();
        treeView.getSelectionModel().getSelectedItems().forEach(item -> {
            if (item != treeView.getRoot()) {
                //叶子节点是连接,需要删除redis上的key
                if(item.isLeaf()){
                    delKeys.add(item.getValue().getKey());
                }
                delItems.add(item);
            }
        });
        deleteTreeItems(delItems);

        //删除服务器的key
        async(()->{
            exeRedis(j -> j.del(delKeys.toArray(new String[delKeys.size()])));
        });

        //删除对应打开的tab
        removeTabByKeys(delKeys);



    }

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
     * @param delKeys
     */
    public void removeTabByKeys(List<String> delKeys) {
        List<Tab> delTabs = new ArrayList<>();
        for (Tab tab : dbTabPane.getTabs()) {
            BaseClientController controller =(BaseClientController) tab.getContent().getUserData();
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
     * @param actionEvent
     */
    @FXML
    public void flush(ActionEvent actionEvent) {
        if(!GuiUtil.alert(Alert.AlertType.CONFIRMATION,Main.RESOURCE_BUNDLE.getString(Constant.ALERT_MESSAGE_DEL_CONNECTION) )){
            return;
        }
        async(()->{
            exeRedis(j -> j.flushDB());
            Platform.runLater(()->{
                treeView.getRoot().getChildren().clear();
            });
        });
    }



    /**
     * 删除单个treeView对应的key,由子层调用
     * @param p
     * @return
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
     * 尝试找key
     * @param root 树根
     * @param key key
     * @return
     */
    private TreeItem<KeyTreeNode> tryFindTreeItemByKey(TreeItem<KeyTreeNode> root, String key) {
        TreeItem<KeyTreeNode> find=null;
        //先从以打开的列表中找，找不到在从根节点递归找
        find = findTreeItemByKeyInOpenTreeItems( key);
        if(find==null){
            find = findTreeItemByKey(treeView.getRoot(), key);
        }
        return find;
    }

    /**
     * 从打开的key列表中查找
     * @param key key
     * @return
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
     * @param p
     * @return
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
     * @param mouseEvent
     */
    @Deprecated
    @FXML
    public void onChoiceBoxMouseClicked(MouseEvent mouseEvent) {
    }

    /**
     * 刷新db
     * 同时会触发db的选择事件,触发search
     * @param actionEvent
     */
    @FXML
    public void refresh(ActionEvent actionEvent) {
        resetDBSelects();
    }

    public void reset(ActionEvent actionEvent) {
        searchText.setText("");
    }

    /**
     * 清空搜索记录
     * @param actionEvent
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
}
