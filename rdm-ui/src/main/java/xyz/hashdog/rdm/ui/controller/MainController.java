package xyz.hashdog.rdm.ui.controller;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.redis.Message;
import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.RedisContext;
import xyz.hashdog.rdm.redis.RedisFactorySingleton;
import xyz.hashdog.rdm.redis.exceptions.RedisException;
import xyz.hashdog.rdm.ui.Main;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.common.Constant;
import xyz.hashdog.rdm.ui.entity.PassParameter;
import xyz.hashdog.rdm.ui.entity.config.KeyTabPaneSetting;
import xyz.hashdog.rdm.ui.entity.config.ServerTabPaneSetting;
import xyz.hashdog.rdm.ui.entity.config.TabPaneSetting;
import xyz.hashdog.rdm.ui.sampler.event.DefaultEventBus;
import xyz.hashdog.rdm.ui.sampler.event.Event;
import xyz.hashdog.rdm.ui.sampler.event.TabPaneEvent;
import xyz.hashdog.rdm.ui.sampler.layout.ApplicationWindow;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.RecentHistory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javafx.scene.input.KeyCombination.*;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * 主控制层
 * @author th
 */
public class MainController extends BaseWindowController<ApplicationWindow> {
    @FXML
    public AnchorPane root;
    /**
     * tab页容器
     */
    @FXML
    public TabPane serverTabPane;
    public MenuItem fileOpen;
    public MenuItem fileConnect;
    public MenuItem fileSettings;
    public Menu history;
    public Menu servers;
    public MenuItem undo;
    public MenuItem redo;
    public MenuItem cut;
    public MenuItem copy;
    public MenuItem paste;
    public MenuItem del;
    public MenuItem selectAll;
    public MenuItem deselect;
    public Menu serverTabPaneMenu;
    public Menu keyTabPaneMenu;
    public MenuItem fullScreen;
    public MenuItem maximized;
    public MenuItem minimized;
    public RadioMenuItem serverTabTop;
    public RadioMenuItem serverTabBottom;
    public RadioMenuItem serverTabLeft;
    public RadioMenuItem serverTabRight;
    public RadioMenuItem keyTabTop;
    public RadioMenuItem keyTabBottom;
    public RadioMenuItem keyTabLeft;
    public RadioMenuItem keyTabRight;
    /**
     * 服务连接的Stage
     */
    private Stage serverConnectionsWindowStage;
    private ServerConnectionsController serverConnectionsController;
    /**
     * 设置的stage
     */
    private Stage settingsStage;
    private RecentHistory<RedisConfig> recentHistory ;

    @FXML
    public void initialize() {
        initListener();
        initMenuIconAndKey();
        initRecentHistory();
        initMenuGroup();
        initTabPane();
    }

    private void initTabPane() {
        ServerTabPaneSetting setting =Applications.getConfigSettings(ConfigSettingsEnum.SERVER_TAB_PANE.name);
        this.serverTabPane.setSide(Side.valueOf(setting.getSide()));
    }

    private void initMenuGroup() {
        setMenuGroup(serverTabPaneMenu,keyTabPaneMenu);
        ServerTabPaneSetting ssetting =Applications.getConfigSettings(ConfigSettingsEnum.SERVER_TAB_PANE.name);
        setRadioMenuItemSelected(ssetting,serverTabPaneMenu,false);
        KeyTabPaneSetting ksetting =Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAB_PANE.name);
        setRadioMenuItemSelected(ksetting,keyTabPaneMenu,false);


    }


    /**
     * RadioMenuItem 设置分组
     * @param menus 菜单
     */
    private void setMenuGroup(Menu... menus) {
        for (Menu menu : menus) {
            ToggleGroup toggleGroup = new ToggleGroup();
            for (MenuItem item : menu.getItems()) {
                if(item instanceof RadioMenuItem i){
                    i.setToggleGroup(toggleGroup);
                }
            }
        }

    }

    /**
     * 重新方法，增加了监听当前场景中哪个节点拥有输入焦点，弃用相关编辑菜单项
     * @param currentStage 当前场景，主场景
     */
    @Override
    public void setCurrentStage(Stage currentStage) {
        super.setCurrentStage(currentStage);
        currentStage.getScene().focusOwnerProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode instanceof TextInputControl tic) {
                cut.setOnAction(e -> tic.cut());
                copy.setOnAction(e -> tic.copy());
                paste.setOnAction(e -> tic.paste());
                selectAll.setOnAction(e -> tic.selectAll());
                undo.setOnAction(e -> tic.undo());
                redo.setOnAction(e -> tic.redo());
                deselect.setOnAction(e -> tic.deselect());
                del.setOnAction(e -> {
                    int start = tic.getSelection().getStart();
                    int end = tic.getSelection().getEnd();
                    if (start != end) {
                        tic.deleteText(start, end);
                    }
                });
            }
        });
    }

    private void initListener() {
        serverTabPaneListener();
        tabPaneChangeListener();
    }

    private void tabPaneChangeListener() {
        addTmEventSubscriber(TabPaneEvent.class, e -> {
            var eventType = e.getEventType();
            if (eventType == TabPaneEvent.EventType.SERVER_TAB_PANE_CHANGE ) {
                ServerTabPaneSetting setting =Applications.getConfigSettings(ConfigSettingsEnum.SERVER_TAB_PANE.name);
                setRadioMenuItemSelected(setting,serverTabPaneMenu,true);
            }
            if (eventType == TabPaneEvent.EventType.KEY_TAB_PANE_CHANGE ) {
                KeyTabPaneSetting setting =Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAB_PANE.name);
                setRadioMenuItemSelected(setting,keyTabPaneMenu,true);
            }

        });
    }

    /**
     * 选择并点击
     * @param setting 撇嘴
     * @param menu 菜单
     * @param fire 是否需要触发点击
     */
    private void setRadioMenuItemSelected(TabPaneSetting setting, Menu menu, boolean fire) {
        Side side = Side.valueOf(setting.getSide());
        RadioMenuItem menuItem = (RadioMenuItem) menu.getItems().get(side.ordinal());
        menuItem.setSelected(true);
        if(fire) {
            menuItem.fire();
        }
    }

    /**
     * tab页监听器，主要用于更新菜单栏的服务器列表和关闭tab页时更新菜单项
     */
    private void serverTabPaneListener() {
        serverTabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            ObservableList<MenuItem> items = servers.getItems();
            while (change.next()) {
                // 添加时，在菜单项前面插入一个新元素
                if (change.wasAdded()) {
                    Tab tab = change.getList().get(change.getTo() - 1);
                    MenuItem menuItem = new MenuItem(tab.getText());
                    menuItem.setUserData(tab);
                    menuItem.setOnAction(event -> {
                        Object userData = menuItem.getUserData();
                        GuiUtil.closeTab(serverTabPane, (Tab) userData);
                    });
                    items.addFirst(menuItem);
                    // 删除时，删除对应的菜单项
                } else if (change.wasRemoved()) {
                    for (int i = 0; i < items.size()-2; i++) {
                        MenuItem item= items.get(i);
                        if (item.getUserData() == change.getRemoved().get(0)) {
                            items.remove(item);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void initRecentHistory() {
        //搜索记录 未做持久化 todo
        List<RedisConfig> rc = new ArrayList<>();
        recentHistory = new RecentHistory<RedisConfig>(5,rc,new RecentHistory.Noticer<RedisConfig>() {

            @Override
            public void notice(List<RedisConfig> list) {
                doRecentHistory(list);
            }
        });
        doRecentHistory(recentHistory.get());
    }

    /**
     * 刷新历史记录
     * @param list
     */
    private void doRecentHistory(List<RedisConfig> list) {
        ObservableList<MenuItem> items = history.getItems();
        //为了一致性，直接清空在重新赋值，虽然单个元素增加会减少消耗，但是复杂度增加，暂时不考虑
        items.remove(0,items.size() - 2);
        List<RedisConfig> reversed = list.reversed();
        for (RedisConfig rc : reversed) {
            items.addFirst(createHistoryMenuItem(rc));

        }
    }

    /**
     * 创建历史菜单项
     * @param str
     * @return
     */
    private MenuItem createHistoryMenuItem(RedisConfig str) {
        MenuItem menuItem = new MenuItem(str.getName());
        menuItem.setUserData(str);
        menuItem.setOnAction(event -> {
            newRedisTab((RedisConfig)menuItem.getUserData());
        });
        return menuItem;
    }

    /**
     * 初始化菜单图标和快捷键
     */
    private void initMenuIconAndKey() {
        GuiUtil.setIconAndKey(fileOpen,new FontIcon(Feather.FOLDER),new KeyCodeCombination(KeyCode.O, CONTROL_DOWN));
        GuiUtil.setIconAndKey(fileConnect,new FontIcon(Feather.LINK),new KeyCodeCombination(KeyCode.C, ALT_DOWN));
        GuiUtil.setIconAndKey(fileSettings,new FontIcon(Feather.SETTINGS),new KeyCodeCombination(KeyCode.Q, ALT_DOWN));
        //编辑按钮些
        GuiUtil.setIconAndKey(undo,new FontIcon(Feather.CORNER_DOWN_LEFT),new KeyCodeCombination(KeyCode.Z, CONTROL_DOWN));
        GuiUtil.setIconAndKey(redo,new FontIcon(Feather.CORNER_DOWN_RIGHT),new KeyCodeCombination(KeyCode.Y, CONTROL_DOWN));
        GuiUtil.setIconAndKey(cut,new FontIcon(Feather.SCISSORS),new KeyCodeCombination(KeyCode.X, CONTROL_DOWN));
        GuiUtil.setIconAndKey(copy,new FontIcon(Feather.COPY),new KeyCodeCombination(KeyCode.C, CONTROL_DOWN));
        GuiUtil.setIconAndKey(paste,new FontIcon(Feather.CLIPBOARD),new KeyCodeCombination(KeyCode.V, CONTROL_DOWN));
        GuiUtil.setIconAndKey(del,new FontIcon(Feather.DELETE),new KeyCodeCombination(KeyCode.D, CONTROL_DOWN));
        GuiUtil.setIconAndKey(selectAll,new FontIcon(Feather.CHECK_SQUARE),new KeyCodeCombination(KeyCode.A, CONTROL_DOWN));
        GuiUtil.setIconAndKey(deselect,new FontIcon(Feather.SQUARE),new KeyCodeCombination(KeyCode.A, CONTROL_DOWN,SHIFT_DOWN));
        //视图菜单按钮
        fullScreen.setAccelerator(new KeyCodeCombination(KeyCode.F11));
        maximized.setAccelerator(new KeyCodeCombination(KeyCode.M,CONTROL_DOWN,SHIFT_DOWN));
        minimized.setAccelerator(new KeyCodeCombination(KeyCode.M,CONTROL_DOWN));
        serverTabTop.setGraphic(new FontIcon(Feather.ARROW_UP));
        serverTabBottom.setGraphic(new FontIcon(Feather.ARROW_DOWN));
        serverTabLeft.setGraphic(new FontIcon(Feather.ARROW_LEFT));
        serverTabRight.setGraphic(new FontIcon(Feather.ARROW_RIGHT));
        keyTabTop.setGraphic(new FontIcon(Feather.ARROW_UP));
        keyTabBottom.setGraphic(new FontIcon(Feather.ARROW_DOWN));
        keyTabLeft.setGraphic(new FontIcon(Feather.ARROW_LEFT));
        keyTabRight.setGraphic(new FontIcon(Feather.ARROW_RIGHT));

    }

    @FXML
    public void openServerLinkWindo(ActionEvent actionEvent)   {
        if(this.serverConnectionsWindowStage!=null){
            if(this.serverConnectionsWindowStage.isShowing()){
                //已经设置为WINDOW_MODAL模式,子窗口未关闭是不能操作的,所以子窗口显示在最上方的操作已经没有意义
//                serverConnectionsWindowStage.setAlwaysOnTop(true);
//                serverConnectionsWindowStage.setAlwaysOnTop(false);

            }else {
                serverConnectionsWindowStage.show();
            }
        }else{
            this.serverConnectionsWindowStage=new Stage();
            serverConnectionsWindowStage.initModality(Modality.WINDOW_MODAL);
            this.serverConnectionsWindowStage.setTitle(Main.RESOURCE_BUNDLE.getString(Constant.MAIN_FILE_CONNECT));

            Tuple2<AnchorPane,ServerConnectionsController> tuple2 = loadFXML("/fxml/ServerConnectionsView.fxml");
            AnchorPane borderPane =tuple2.t1();
            serverConnectionsController = tuple2.t2();
            Scene scene = new Scene(borderPane);
            this.serverConnectionsWindowStage.initOwner(root.getScene().getWindow());
            this.serverConnectionsWindowStage.setScene(scene);
            this.serverConnectionsWindowStage.show();
            serverConnectionsController.setParentController(this);
            serverConnectionsController.setCurrentStage(serverConnectionsWindowStage);


        }

    }

    /**
     * 创建新的tab页
     *
     * @param redisContext redis上下文
     * @param name 服务名称
     */
    public void newRedisTab(RedisContext redisContext, String name) throws IOException {
        Tuple2<AnchorPane,ServerTabController> tuple2 = loadFXML("/fxml/ServerTabView.fxml");
        AnchorPane borderPane = tuple2.t1();
        ServerTabController controller = tuple2.t2();
        controller.setParentController(this);
        PassParameter passParameter = new PassParameter(PassParameter.REDIS);
        passParameter.setRedisContext(redisContext);
        passParameter.setRedisClient(redisContext.newRedisClient());
        controller.setParameter(passParameter);
        Tab tab = new Tab(name);
        tab.setContent(borderPane);
        tab.setUserData(controller);
        this.serverTabPane.getTabs().add(tab);
        this.serverTabPane.getSelectionModel().select(tab);
        tab.setGraphic(GuiUtil.creatConnctionImageView());

        if(passParameter.getTabType()== PassParameter.REDIS){
            // 监听Tab被关闭事件,但是remove是无法监听的
            tab.setOnClosed(event2 -> {
                ThreadPool.getInstance().execute(()->controller.getRedisContext().close());
            });
        }
        ContextMenu cm= GuiUtil.newTabContextMenu(tab);
        //写入最近连接记录
        recentHistory.add(redisContext.getRedisConfig());
    }

    /**
     * 新建redis连接tab页
     * @param redisConfig
     * @throws IOException
     */
    public void newRedisTab(RedisConfig redisConfig)  {
        RedisContext redisContext = RedisFactorySingleton.getInstance().createRedisContext(redisConfig);
        Message message = redisContext.newRedisClient().testConnect();
        if (!message.isSuccess()) {
            GuiUtil.alert(Alert.AlertType.WARNING, message.getMessage());
            return;
        }
        try {
            this.newRedisTab(redisContext,redisConfig.getName());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RedisException(e.getMessage());
        }
    }

    /**
     * 打开设置窗口
     * @param actionEvent
     */
    public void openSettings(ActionEvent actionEvent) {
        if(this.settingsStage!=null){
            if(this.settingsStage.isShowing()){
            }else {
                settingsStage.show();
            }
        }else{
            this.settingsStage=new Stage();
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.getIcons().add(GuiUtil.ICON_REDIS);
            this.settingsStage.setTitle(language("main.file.setting"));

//            Tuple2<AnchorPane,SettingsController> tuple2 = loadFXML("/fxml/AdvancedPage.fxml");
//            AnchorPane borderPane =tuple2.getT1();
            ApplicationWindow applicationWindow = new ApplicationWindow();
//            SettingsController controller = tuple2.getT2();
            var antialiasing = Platform.isSupported(ConditionalFeature.SCENE3D)
                    ? SceneAntialiasing.BALANCED
                    : SceneAntialiasing.DISABLED;
            Scene scene = new Scene(applicationWindow,ApplicationWindow.MIN_WIDTH + 80, 768, false, antialiasing);
            Main.initTm(scene);
            this.settingsStage.initOwner(root.getScene().getWindow());
            this.settingsStage.setScene(scene);
            this.settingsStage.show();
//            controller.setParentController(this);
//            controller.setCurrentStage(settingsStage);


        }
    }

    /**
     * 清除所有最近连接记录
     * @param actionEvent
     */
    public void clearHistory(ActionEvent actionEvent) {
        this.recentHistory.clear();
    }

    /**
     * 关闭tabPan当前服务
     * @param actionEvent
     */
    public void closeCurrentServer(ActionEvent actionEvent) {
        Tab selectedItem = this.serverTabPane.getSelectionModel().getSelectedItem();
        if(selectedItem!=null){
            GuiUtil.closeTab(this.serverTabPane,selectedItem);
        }
    }

    /**
     * 关闭所有服务
     * @param actionEvent
     */
    public void closeServerAll(ActionEvent actionEvent) {
        ObservableList<Tab> tabs = this.serverTabPane.getTabs();
        GuiUtil.closeTab(this.serverTabPane,new ArrayList<>(tabs));
    }

    /**
     * 快速新建连接
     * @param actionEvent
     */
    public void newConnection(ActionEvent actionEvent) throws IOException {
        openServerLinkWindo(actionEvent);
        serverConnectionsController.newConnection(actionEvent);
    }

    /**
     * 快速新建分组
     * @param actionEvent
     * @throws IOException
     */
    @FXML
    public void newGroup(ActionEvent actionEvent) throws IOException {
        openServerLinkWindo(actionEvent);
        serverConnectionsController.newGroup(actionEvent);
    }

    /**
     * 快速连接
     * @param actionEvent
     */
    public void quickConnection(ActionEvent actionEvent) throws IOException {
        openServerLinkWindo(actionEvent);
        serverConnectionsController.quickConnection();
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void maximized(ActionEvent actionEvent) {
        currentStage.setMaximized(true);
    }

    public void minimized(ActionEvent actionEvent) {
        currentStage.setIconified(true);

    }

    public void fullScreen(ActionEvent actionEvent) {
        currentStage.setFullScreen(true);
    }

    public void resetWindow(ActionEvent actionEvent) {
        double contentWidth = root.getPrefWidth();
        double contentHeight = root.getPrefHeight();

        // 获取窗口装饰区域的宽度和高度
        double windowWidth = contentWidth + (currentStage.getWidth() - currentStage.getScene().getWidth());
        double windowHeight = contentHeight + (currentStage.getHeight() - currentStage.getScene().getHeight());

        currentStage.setWidth(windowWidth);
        currentStage.setHeight(windowHeight);
        currentStage.centerOnScreen();

    }

    public void serverTabTop(ActionEvent actionEvent) {
        GuiUtil.setTabPaneSide(serverTabPane,Side.TOP);
    }

    public void serverTabBottom(ActionEvent actionEvent) {
        GuiUtil.setTabPaneSide(serverTabPane,Side.BOTTOM);
    }

    public void serverTabLeft(ActionEvent actionEvent) {
        GuiUtil.setTabPaneSide(serverTabPane,Side.LEFT);
    }

    public void serverTabRight(ActionEvent actionEvent) {
        GuiUtil.setTabPaneSide(serverTabPane,Side.RIGHT);
    }

    public void keyTabTop(ActionEvent actionEvent) {
        setKeyTabSide(serverTabPane,Side.TOP);
    }

    private void setKeyTabSide(TabPane serverTabPane, Side side) {
        for (Tab tab : this.serverTabPane.getTabs()) {
            Object userData = tab.getUserData();
            if(userData instanceof ServerTabController stc){
                GuiUtil.setTabPaneSide( stc.dbTabPane,side);
            }
        }
    }

    public void keyTabBottom(ActionEvent actionEvent) {
        setKeyTabSide(serverTabPane,Side.BOTTOM);
    }

    public void keyTabLeft(ActionEvent actionEvent) {
        setKeyTabSide(serverTabPane,Side.LEFT);
    }

    public void keyTabRight(ActionEvent actionEvent) {
        setKeyTabSide(serverTabPane,Side.RIGHT);
    }
}
