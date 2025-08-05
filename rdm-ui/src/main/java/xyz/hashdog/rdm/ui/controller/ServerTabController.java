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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.TUtil;
import xyz.hashdog.rdm.ui.Main;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.common.Constant;
import xyz.hashdog.rdm.ui.common.RedisDataTypeEnum;
import xyz.hashdog.rdm.ui.entity.DBNode;
import xyz.hashdog.rdm.ui.entity.PassParameter;
import xyz.hashdog.rdm.ui.entity.config.KeyTabPaneSetting;
import xyz.hashdog.rdm.ui.entity.config.ServerTabPaneSetting;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.RecentHistory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ServerTabController extends BaseKeyController<MainController> {



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
    public Button reset;
    public MenuButton history;
    @FXML
    private TreeView<String> treeView;
    @FXML
    private ChoiceBox<DBNode> choiceBox;
    /**
     * tab页容器
     */
    @FXML
    public TabPane dbTabPane;

    private RecentHistory<String> recentHistory ;



    /**
     * 最后一个选中节点
     */
    private TreeItem<String> lastSelectedNode;


    @FXML
    public void initialize() {
        initRecentHistory();
        initNewKey();
        initAutoWah();
        initListener();
        initButton();
        initTextField();
        initTabPane();
    }
    private void initTabPane() {
        KeyTabPaneSetting ksetting =Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAB_PANE.name);
        this.dbTabPane.setSide(Side.valueOf(ksetting.getSide()));
    }

    /**
     * 最近使用搜索记录初始化
     */
    private void initRecentHistory() {
        //搜索记录 未做持久化 todo
        List<String> strings = new ArrayList<>();
//        List<String> strings = List.of("1","2","3","4","5");
        recentHistory = new RecentHistory<String>(5,strings,new RecentHistory.Noticer<String>() {

            @Override
            public void notice(List<String> list) {
                doRecentHistory(list);
            }
        });
        doRecentHistory(recentHistory.get());
    }

    /**
     * 创建搜索记录的菜单项
     * @param str
     * @return
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
     * @param list
     */
    private void doRecentHistory(List<String> list) {
        ObservableList<MenuItem> items = history.getItems();
        //为了一致性，直接清空在重新赋值，虽然单个元素增加会减少消耗，但是复杂度增加，暂时不考虑
        items.remove(0,items.size() - 2);
        List<String> reversed = list.reversed();
        for (String s : reversed) {
            items.addFirst(createSearchHistoryMenuItem(s));

        }
    }

    private void initTextField() {
        searchText.setRight(searchHbox);
    }

    private void initButton() {
        initButtonIcon();
        initButtonStyles();

    }
    private void initButtonStyles() {
        search.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.SMALL);
        reset.getStyleClass().addAll(Styles.BUTTON_CIRCLE,Styles.FLAT);
        history.getStyleClass().addAll(Styles.SMALL,Styles.FLAT, Tweaks.NO_ARROW);
        search.setCursor(Cursor.HAND);
        reset.setCursor(Cursor.HAND);
        history.setCursor(Cursor.HAND);
        newKey.getStyleClass().addAll(Tweaks.NO_ARROW);
    }

    private void initButtonIcon() {
        search.setGraphic(new FontIcon(Feather.SEARCH));
        reset.setGraphic(new FontIcon(Material2AL.CLEAR));
        history.setGraphic(new FontIcon(Material2AL.ARROW_DROP_DOWN));

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
        initTreeViewRoot();
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
                Tuple2<AnchorPane,NewKeyController> tuple2 = loadFXML("/fxml/NewKeyView.fxml");
                AnchorPane anchorPane = tuple2.getT1();
                NewKeyController controller = tuple2.getT2();
                controller.setParentController(this);
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
        // 自动展开根节点
        treeView.setShowRoot(false); // 隐藏根节点
        //默认根节点为选中节点
        treeView.getSelectionModel().select(treeView.getRoot());
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
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                newValue = treeView.getRoot();
            }
            //叶子节点是连接
            boolean isLeafNode = newValue.isLeaf();
            //是否为根
            boolean isRoot = newValue.getParent() == null;
            // 右键菜单显示/隐藏
            ObservableList<MenuItem> items = contextMenu.getItems();
            items.forEach(menuItem -> {
                if (menuItem.getStyleClass().contains("isLeafNode")) {
                    menuItem.setVisible(isLeafNode);
                }
                if (menuItem.getStyleClass().contains("isNotRoot")) {
                    menuItem.setVisible(!isRoot);
                }
            });
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
                TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
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

    /**
     * 父层传送的数据监听
     * 监听到进行db选择框的初始化
     */
    private void userDataPropertyListener() {
        super.parameter.addListener((observable, oldValue, newValue) -> {
            initDBSelects();
        });
    }


    /**
     * 初始化db选择框
     */
    private void initDBSelects() {
        ObservableList<DBNode> items = choiceBox.getItems();
        ThreadPool.getInstance().execute(() -> {
            Map<Integer, String> map = this.redisClient.dbSize();
            Platform.runLater(() -> {
                for (Map.Entry<Integer, String> en : map.entrySet()) {
                    items.add(new DBNode(en.getValue(), en.getKey()));
                }
                //默认选中第一个
                choiceBox.setValue(choiceBox.getItems().get(0));
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
            Map<Integer, String> map = this.redisClient.dbSize();
            Platform.runLater(() -> {
                for (Map.Entry<Integer, String> en : map.entrySet()) {
                    DBNode dbNode = new DBNode(en.getValue(), en.getKey());
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
                    List<TreeItem<String>> list = new ArrayList<>();
                    for (TreeItem<String> selectedItem : treeView.getSelectionModel().getSelectedItems()) {
                        if (!selectedItem.isLeaf()) { // Check if the selected node is a parent node
                            list.add(selectedItem);

                        }
                    }
                    for (TreeItem<String> selectedItem : list) {
                        //设置选中
                        selectChildren((TreeItem<String>) selectedItem);
                    }
                }

            }
        });
    }

    /**
     * 数据已经有了,直接更新到视图
     */
    private void initTreeView(List<String> keys) {

        ObservableList<TreeItem<String>> children = treeView.getRoot().getChildren();
        children.clear();
        ThreadPool.getInstance().execute(() -> {
            if(this.redisContext.getRedisConfig().isTreeShow()){
                buildTreeView(children,keys);
            }else {
                buildListView(children,keys);
            }
        });






    }

    /**
     * key构建列表
     * @param children
     * @param keys
     */
    private void buildListView(ObservableList<TreeItem<String>> children, List<String> keys) {
        for (String key : keys) {
            String type = exeRedis(j -> j.type(key));
            Label keyTypeLabel = GuiUtil.getKeyTypeLabel(type);
            Platform.runLater(() -> {
                children.add(new TreeItem<>(key, keyTypeLabel));
//            children.add(new TreeItem<>(key, GuiUtil.creatKeyImageView()));
            });

        }
    }


    /**
     * KEY展示构建树形结构
     * @param root
     * @param keys
     */
    private void buildTreeView(TreeItem<String> root,List<String> keys) {
        for (String key : keys) {
            String type = exeRedis(j -> j.type(key));
            Label keyTypeLabel = GuiUtil.getKeyTypeLabel(type);
            String keySeparator = this.redisContext.getRedisConfig().getKeySeparator();
            String[] parts = key.split(keySeparator);
            TreeItem<String> current = root;
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                //叶子节点是key类型
                boolean isLeaf = (i == parts.length - 1);

                TreeItem<String> childNode = findChild(current, part);
                if (childNode == null|| isLeaf) {
                    childNode = new TreeItem<>(part,new FontIcon(Feather.FOLDER));
                    if (isLeaf) {
                        childNode = new TreeItem<>(key);
                        childNode.setGraphic(keyTypeLabel);
                    }
                    TreeItem<String> finalChildNode = childNode;
                    TreeItem<String> finalCurrent = current;
                    Platform.runLater(() -> {
                        finalCurrent.getChildren().add(finalChildNode);
                    });

                }

                current = childNode;
            }
        }
    }

    // 查找子节点是否存在
    private TreeItem<String> findChild(TreeItem<String> parent, String part) {
        for (TreeItem<String> child : parent.getChildren()) {
            if (part.equals(child.getValue())) {
                return child;
            }
        }
        return null;
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
    private void selectChildren(TreeItem<String> parent) {
        if (parent == null) {
            return;
        }
        if (!parent.isLeaf()) {
            parent.setExpanded(true); // Optional: Expand the parent to show all children
            for (TreeItem<String> child : parent.getChildren()) {
                treeView.getSelectionModel().select(child);
                selectChildren(child); // Recursively select children of the child node
            }
        }
    }

    /**
     * 新增key
     *
     * @param actionEvent
     */
    public void newKey(ActionEvent actionEvent) {

    }

    /**
     * 模糊搜索
     *
     * @param actionEvent
     */
    public void search(ActionEvent actionEvent) {
        ThreadPool.getInstance().execute(() -> {
            List<String> keys = exeRedis(j -> j.scanAll(searchText.getText()));
            Platform.runLater(() -> {
                //key已经查出来,只管展示
                initTreeView(keys);
            });
            //搜索不是空，就加入历史记录
            if(TUtil.isNotEmpty(searchText.getText())){
                recentHistory.add(searchText.getText());
            }
        });
    }

    /**
     * 打开key
     *
     * @param actionEvent
     */
    public void open(ActionEvent actionEvent)  {
        String key = this.lastSelectedNode.getValue();
        String type = RedisDataTypeEnum.getByType(exeRedis(j -> j.type(key))).type;
        Tuple2<AnchorPane,BaseKeyController> tuple2 = loadFXML("/fxml/KeyTabView.fxml");
        AnchorPane borderPane = tuple2.getT1();
        BaseKeyController controller = tuple2.getT2();
        controller.setParentController(this);
        PassParameter passParameter = new PassParameter(PassParameter.NONE);
        passParameter.setDb(this.currentDb);
        passParameter.setKey(key);
        passParameter.setKeyType(type);
        passParameter.setRedisClient(redisClient);
        passParameter.setRedisContext(redisContext);
        StringProperty keySend = passParameter.keyProperty();
        //操作的kye和子界面进行绑定,这样更新key就会更新树节点
        this.lastSelectedNode.valueProperty().bind(keySend);
        controller.setParameter(passParameter);
        Tab tab = new Tab(String.format("%s|%s|%s", this.currentDb,type, key));
        ContextMenu cm=GuiUtil.newTabContextMenu(tab);
        tab.setContent(borderPane);
        tab.setGraphic(GuiUtil.creatKeyImageView());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);

    }

    /**
     * 控制台
     * @param actionEvent
     */
    @FXML
    public void console(ActionEvent actionEvent) throws IOException {
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadFXML("/fxml/ConsoleView.fxml");
        AnchorPane anchorPane = tuple2.getT1();
        BaseKeyController controller = tuple2.getT2();
        controller.setParentController(this);
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
        tab.setGraphic(GuiUtil.creatConsoleImageView());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
    }
    @FXML
    public void monitor(ActionEvent actionEvent) throws IOException {
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadFXML("/fxml/MonitorView.fxml");
        AnchorPane anchorPane = tuple2.getT1();
        BaseKeyController controller = tuple2.getT2();
        controller.setParentController(this);
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
        tab.setGraphic(GuiUtil.creatMonitorImageView());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
    }

    @FXML
    public void report(ActionEvent actionEvent) throws IOException {
        Tuple2<ScrollPane,ConsoleController> tuple2 = loadFXML("/fxml/ReportView.fxml");
        ScrollPane anchorPane = tuple2.getT1();
        BaseKeyController controller = tuple2.getT2();
        controller.setParentController(this);
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
        tab.setGraphic(GuiUtil.creatMonitorImageView());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
    }

    @FXML
    public void pubsub(ActionEvent actionEvent) throws IOException {
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadFXML("/fxml/PubSubView.fxml");
        AnchorPane anchorPane = tuple2.getT1();
        BaseKeyController controller = tuple2.getT2();
        controller.setParentController(this);
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
        tab.setGraphic(GuiUtil.creatPubSubImageView());
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
        List<TreeItem<String>> delItems =new ArrayList<>();
        treeView.getSelectionModel().getSelectedItems().forEach(item -> {
            if (item != treeView.getRoot()) {
                //叶子节点是连接,需要删除redis上的key
                if(item.isLeaf()){
                    delKeys.add(item.getValue());
                }
                delItems.add(item);
            }
        });
        //删除tree节点,得从新用list装一次再遍历删除,否则会有安全问题
        for (TreeItem<String> delItem : delItems) {
            delItem.getParent().getChildren().remove(delItem); // 将选中的节点从父节点的子节点列表中移除
        }

        //删除服务器的key
        asynexec(()->{
            exeRedis(j -> j.del(delKeys.toArray(new String[delKeys.size()])));
        });

        //删除对应打开的tab
        removeTabByKeys(delKeys);



    }

    /**
     * 删除对应key的tab
     * @param delKeys
     */
    public void removeTabByKeys(List<String> delKeys) {
        List<Tab> delTabs = new ArrayList<>();
        for (Tab tab : dbTabPane.getTabs()) {
            BaseKeyController controller =(BaseKeyController) tab.getContent().getUserData();
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
        if(!GuiUtil.alert(Alert.AlertType.CONFIRMATION,Main.RESOURCE_BUNDLE.getString(Constant.ALERT_MESSAGE_DELCONNECTION) )){
            return;
        }
        asynexec(()->{
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
                GuiUtil.deleteTreeNodeByKey(treeView.getRoot(),p.get().getKey());
            });
        }
        return true;
    }

    /**
     * 新增key并选中
     * @param p
     * @return
     */
    public boolean addKeyAndSelect(ObjectProperty<PassParameter> p) {
        //如果treeView是的db和删除key的db相同,则需要对应删除treeView中的节点
        if(p.get().getDb()==this.currentDb){
            Platform.runLater(()->{
                TreeItem treeItem = new TreeItem(p.get().getKey(),GuiUtil.getKeyTypeLabel(p.get().getKeyType()));
                treeView.getRoot().getChildren().add(treeItem);
                treeView.getSelectionModel().select(treeItem);
                open(null);
            });
        }
        return true;
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
}
