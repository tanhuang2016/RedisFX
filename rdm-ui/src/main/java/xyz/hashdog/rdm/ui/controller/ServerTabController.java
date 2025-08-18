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
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.common.util.TUtil;
import xyz.hashdog.rdm.ui.Main;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.common.Constant;
import xyz.hashdog.rdm.ui.common.RedisDataTypeEnum;
import xyz.hashdog.rdm.ui.entity.DBNode;
import xyz.hashdog.rdm.ui.entity.KeyTreeNode;
import xyz.hashdog.rdm.ui.entity.PassParameter;
import xyz.hashdog.rdm.ui.entity.config.KeyTabPaneSetting;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.RecentHistory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
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
    public MenuItem delete;
    public MenuItem open;
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
     * 最后一个选中节点，可能是目录哦
     */
    private TreeItem<KeyTreeNode> lastSelectedNode;
    /**
     * 缓存已打开的key节点，用户删除的时候提高性能，避免从根节点递归
     * 删除缓存的策略没有想好，目前考虑只存放10个，一般不会打开超过10个窗口
     * 存储的时候先进先出，保留最新的10个
     * 用
     */
    private final LinkedHashSet<WeakReference<TreeItem<KeyTreeNode>>> openTreeItems = new LinkedHashSet<>(10);


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
                AnchorPane anchorPane = tuple2.t1();
                NewKeyController controller = tuple2.t2();
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
        if(this.redisContext.getRedisConfig().isTreeShow()){
            Platform.runLater(() -> {
                buildTreeView(treeView.getRoot(),keys);
            });
        }else {
            buildListView(children,keys);
        }






    }

    /**
     * key构建列表
     * @param children
     * @param keys
     */
    private void buildListView(ObservableList<TreeItem<KeyTreeNode>> children, List<String> keys) {
        for (String key : keys) {
            String type = exeRedis(j -> j.type(key));
            Label keyTypeLabel = GuiUtil.getKeyTypeLabel(type);
            Platform.runLater(() -> {
                children.add(new TreeItem<>(KeyTreeNode.leaf(key), keyTypeLabel));
            });

        }
    }

    /**
     * key排序
     * @return
     */
    private Comparator<TreeItem<KeyTreeNode>> treeItemSortComparator(){
        return (o1, o2) -> {
            if (o1.getValue().getLeaf() && o2.getValue().getLeaf()) {
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
     * @param root
     * @param keys
     */
    private void buildTreeView(TreeItem<KeyTreeNode> root,List<String> keys) {
        for (String key : keys) {
            String type = exeRedis(j -> j.type(key));
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
                        Label keyTypeLabel = GuiUtil.getKeyTypeLabel(type);
                        childNode.setGraphic(keyTypeLabel);
                    }else {
                        childNode = new TreeItem<>(KeyTreeNode.dir(part),new FontIcon(Feather.FOLDER));
                    }
                    TreeItem<KeyTreeNode> finalChildNode = childNode;
                    TreeItem<KeyTreeNode> finalCurrent = current;
//                    Platform.runLater(() -> {

//                    });
                    if (isLeaf) {
                        finalCurrent.getChildren().add(finalChildNode);
                        if(finalCurrent.getValue()!=null){
                            finalChildNode.getValue().setParent(finalCurrent.getValue());
                            finalCurrent.getValue().addChildKeyCount();
                        }
                    }else {
                        finalCurrent.getChildren().addFirst(finalChildNode);
                        if(finalCurrent.getValue()!=null){
                            //不是叶子节点，不用计数
                            finalChildNode.getValue().setParent(finalCurrent.getValue());
                        }
                    }
                    finalCurrent.getChildren().sort(treeItemSortComparator());

                }

                current = childNode;
            }
        }
    }

    // 查找子节点是否存在
    private TreeItem<KeyTreeNode> findChild(TreeItem<KeyTreeNode> parent, String part) {
        for (TreeItem<KeyTreeNode> child : parent.getChildren()) {
            if (part.equals(child.getValue().getName())) {
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
            List<String> keys = exeRedis(j -> j.scanAll(searchText.getText()));
//            Platform.runLater(() -> {
//
//            });
            //key已经查出来,只管展示
            initTreeView(keys);
            //得刷新一下，不然会出现目录和叶子节点未对齐的显示问题
            treeView.refresh();
            //搜索不是空，就加入历史记录
            if(DataUtil.isNotEmpty(searchText.getText())){
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
        if(!this.lastSelectedNode.isLeaf()){
            this.lastSelectedNode.setExpanded(true);
            return;
        }
        String key = this.lastSelectedNode.getValue().getKey();
        String type = RedisDataTypeEnum.getByType(exeRedis(j -> j.type(key))).type;
        Tuple2<AnchorPane,BaseKeyController> tuple2 = loadFXML("/fxml/KeyTabView.fxml");
        AnchorPane borderPane = tuple2.t1();
        BaseKeyController controller = tuple2.t2();
        controller.setParentController(this);
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
        ContextMenu cm=GuiUtil.newTabContextMenu(tab);
        tab.setContent(borderPane);
        tab.setGraphic(GuiUtil.creatKeyImageView());
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
        Platform.runLater(() -> {
            treeView.scrollTo(treeView.getRow(this.lastSelectedNode));
        });
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
                    String type = exeRedis(j -> j.type(key));
                    childNode = new TreeItem<>(KeyTreeNode.leaf(key));
                    Label keyTypeLabel = GuiUtil.getKeyTypeLabel(type);
                    childNode.setGraphic(keyTypeLabel);
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
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadFXML("/fxml/ConsoleView.fxml");
        AnchorPane anchorPane = tuple2.t1();
        BaseKeyController controller = tuple2.t2();
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
        AnchorPane anchorPane = tuple2.t1();
        BaseKeyController controller = tuple2.t2();
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
        Region anchorPane = tuple2.t1();
        BaseKeyController controller = tuple2.t2();
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
        tab.setGraphic(GuiUtil.creatInfoImageView());
        this.dbTabPane.getTabs().add(tab);
        this.dbTabPane.getSelectionModel().select(tab);
    }

    @FXML
    public void pubsub(ActionEvent actionEvent) throws IOException {
        Tuple2<AnchorPane,ConsoleController> tuple2 = loadFXML("/fxml/PubSubView.fxml");
        AnchorPane anchorPane = tuple2.t1();
        BaseKeyController controller = tuple2.t2();
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
        asynexec(()->{
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
        if(!GuiUtil.alert(Alert.AlertType.CONFIRMATION,Main.RESOURCE_BUNDLE.getString(Constant.ALERT_MESSAGE_DEL_CONNECTION) )){
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
                TreeItem<KeyTreeNode> find=null;
                //先从以打开的列表中找，找不到在从根节点递归找
                find = findTreeItemByKeyInOpenTreeItems( p.get().getKey());
                if(find==null){
                    find = findTreeItemByKey(treeView.getRoot(), p.get().getKey());
                }
                if(find!=null){
                    deleteTreeItems( List.of(find));
                }
            });
        }
        return true;
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
                TreeItem<KeyTreeNode> newTreeItem = treeNodePutDir(treeView.getRoot(), KeyTreeNode.leaf(p.get().getKey()));
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
}
