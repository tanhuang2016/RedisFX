package redisfx.tanh.rdm.ui.controller;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.redis.Message;
import redisfx.tanh.rdm.redis.RedisConfig;
import redisfx.tanh.rdm.redis.RedisContext;
import redisfx.tanh.rdm.redis.RedisFactorySingleton;
import redisfx.tanh.rdm.redis.exceptions.RedisException;
import redisfx.tanh.rdm.ui.Main;
import redisfx.tanh.rdm.ui.common.Applications;
import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.common.Constant;
import redisfx.tanh.rdm.ui.controller.base.BaseWindowController;
import redisfx.tanh.rdm.ui.entity.PassParameter;
import redisfx.tanh.rdm.ui.entity.config.KeyTabPaneSetting;
import redisfx.tanh.rdm.ui.entity.config.ServerTabPaneSetting;
import redisfx.tanh.rdm.ui.entity.config.TabPaneSetting;
import redisfx.tanh.rdm.ui.sampler.event.TabPaneEvent;
import redisfx.tanh.rdm.ui.sampler.layout.ApplicationWindow;
import redisfx.tanh.rdm.ui.sampler.layout.MainModel;
import redisfx.tanh.rdm.ui.sampler.page.Page;
import redisfx.tanh.rdm.ui.util.GuiUtil;
import redisfx.tanh.rdm.ui.util.RecentHistory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static javafx.scene.input.KeyCombination.*;
import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * 主控制层
 * @author th
 */
public class MainController extends BaseWindowController<Main> {
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
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
    public MenuItem fileFastConnect;
    public Menu file;
    public Menu fileNew;
    public MenuItem newGroup;
    public MenuItem newConnect;
    public MenuItem historyClear;
    public MenuItem currentClose;
    public MenuItem serversCloseAll;
    public MenuItem exit;
    public MenuItem find;
    public MenuItem replace;
    public Menu edit;
    public Menu view;
    public MenuItem resetWindow;
    public Menu window;
    public MenuItem welcome;
    public Menu help;
    public MenuItem guide;
    public MenuItem suggest;
    public MenuItem update;
    public MenuItem about;
    public MenuItem restartWindow;
    public AnchorPane center;
    /**
     * 服务连接的Stage
     */
    private Stage serverConnectionsWindowStage;
    /**
     * 服务连接的Controller
     */
    private ServerConnectionsController serverConnectionsController;
    /**
     * 设置的stage
     */
    private Stage settingsStage;
    /**
     * 最近使用的服务，缓存的配置
     */
    private RecentHistory<RedisConfig> recentHistory ;

    @FXML
    public void initialize() {
        initLanguage();
        initListener();
        initMenuIconAndKey();
        initRecentHistory();
        initMenuGroup();
        initTabPane();
    }

    @Override
    protected void initLanguage() {
        file.setText(language("main.file"));
        fileConnect.setText(language("main.file.connect"));
        fileFastConnect.setText(language("main.file.fast"));
        fileNew.setText(language("main.file.new"));
        newGroup.setText(language("main.file.new.group"));
        newConnect.setText(language("main.file.new.connect"));
        fileOpen.setText(language("main.file.open"));
        history.setText(language("main.file.history"));
        historyClear.setText(language("main.file.clear"));
        currentClose.setText(language("main.file.close.current"));
        servers.setText(language("main.file.close"));
        serversCloseAll.setText(language("main.file.close.all"));
        fileSettings.setText(language("main.file.setting"));
        exit.setText(language("main.file.exit"));

        edit.setText(language("main.edit"));
        undo.setText(language("main.edit.undo"));
        redo.setText(language("main.edit.redo"));
        cut.setText(language("main.edit.cut"));
        copy.setText(language("main.edit.copy"));
        paste.setText(language("main.edit.paste"));
        del.setText(language("main.edit.del"));
        selectAll.setText(language("main.edit.selectall"));
        deselect.setText(language("main.edit.deselect"));
        find.setText(language("main.edit.find"));
        replace.setText(language("main.edit.replace"));

        view.setText(language("main.view"));
        fullScreen.setText(language("main.view.full"));
        resetWindow.setText(language("main.view.reset"));
        maximized.setText(language("main.view.maximized"));
        minimized.setText(language("main.view.minimized"));

        window.setText(language("main.window"));
        welcome.setText(language("main.window.welcome"));
        restartWindow.setText(language("main.window.restart"));
        serverTabPaneMenu.setText(language("main.window.server"));
        serverTabTop.setText(language("main.window.top"));
        serverTabBottom.setText(language("main.window.bottom"));
        serverTabLeft.setText(language("main.window.left"));
        serverTabRight.setText(language("main.window.right"));
        keyTabPaneMenu.setText(language("main.window.key"));
        keyTabTop.setText(language("main.window.top"));
        keyTabBottom.setText(language("main.window.bottom"));
        keyTabLeft.setText(language("main.window.left"));
        keyTabRight.setText(language("main.window.right"));

        help.setText(language("main.help"));
        guide.setText(language("main.help.guide"));
        suggest.setText(language("main.help.suggest"));
        update.setText(language("main.help.update"));
        about.setText(language("main.help.about"));
        if(this.settingsStage!=null){
            this.settingsStage.setTitle(language("main.file.setting"));

        }
        if(this.serverConnectionsWindowStage!=null){
            this.serverConnectionsWindowStage.setTitle(Main.RESOURCE_BUNDLE.getString(Constant.MAIN_FILE_CONNECT));
        }


    }

    /**
     * 初始化tabPane
     */
    private void initTabPane() {
        ServerTabPaneSetting setting =Applications.getConfigSettings(ConfigSettingsEnum.SERVER_TAB_PANE.name);
        this.serverTabPane.setSide(Side.valueOf(setting.getSide()));
        this.serverTabPane.setTabMaxWidth(200);
    }

    /**
     * 初始化菜单组
     */
    private void initMenuGroup() {
        setMenuGroup(serverTabPaneMenu,keyTabPaneMenu);
        ServerTabPaneSetting serverSetting =Applications.getConfigSettings(ConfigSettingsEnum.SERVER_TAB_PANE.name);
        setRadioMenuItemSelected(serverSetting,serverTabPaneMenu,false);
        KeyTabPaneSetting keySetting =Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAB_PANE.name);
        setRadioMenuItemSelected(keySetting,keyTabPaneMenu,false);
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
     * 重写方法，增加了监听当前场景中哪个节点拥有输入焦点，弃用相关编辑菜单项
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

    /**
     * 初始化监听器
     */
    private void initListener() {
        serverTabPaneListener();
        tabPaneChangeListener();
    }

    /**
     * 监听设置中TabPane切换事件，配置修改及时生效
     */
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
                        if (item.getUserData() == change.getRemoved().getFirst()) {
                            items.remove(item);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * 初始化最近使用
     */
    private void initRecentHistory() {
        recentHistory = new RecentHistory<>(5, this::doRecentHistory);
        doRecentHistory(recentHistory.get());
    }

    /**
     * 刷新历史记录
     * @param list 历史配置
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
     * @param redisConfig 配置
     * @return 菜单项
     */
    private MenuItem createHistoryMenuItem(RedisConfig redisConfig) {
        MenuItem menuItem = new MenuItem(redisConfig.getName());
        menuItem.setUserData(redisConfig);
        menuItem.setOnAction(event -> newRedisTab((RedisConfig)menuItem.getUserData()));
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

    /**
     * 打开服务器连接窗口
     * @param actionEvent 事件
     */
    @FXML
    public void openServerLinkWindow(ActionEvent actionEvent)   {
        if(this.serverConnectionsWindowStage!=null){
            if(!this.serverConnectionsWindowStage.isShowing()){
                serverConnectionsWindowStage.show();
            }
        }else{
            this.serverConnectionsWindowStage=new Stage();
            serverConnectionsWindowStage.initModality(Modality.WINDOW_MODAL);
            this.serverConnectionsWindowStage.setTitle(Main.RESOURCE_BUNDLE.getString(Constant.MAIN_FILE_CONNECT));
            Tuple2<AnchorPane,ServerConnectionsController> tuple2 = loadFxml("/fxml/ServerConnectionsView.fxml",BaseWindowController.NONE);
            AnchorPane borderPane =tuple2.t1();
            serverConnectionsController = tuple2.t2();
            Scene scene = new Scene(borderPane);
            this.serverConnectionsWindowStage.initOwner(root.getScene().getWindow());
            this.serverConnectionsWindowStage.setScene(scene);
            this.serverConnectionsWindowStage.show();
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
        Tuple2<AnchorPane,ServerTabController> tuple2 = loadFxml("/fxml/ServerTabView.fxml");
        AnchorPane borderPane = tuple2.t1();
        ServerTabController controller = tuple2.t2();
        PassParameter passParameter = new PassParameter(PassParameter.REDIS);
        passParameter.setRedisContext(redisContext);
        passParameter.setRedisClient(redisContext.useRedisClient());
        controller.setParameter(passParameter);
        Tab tab = new Tab(name);
        tab.setUserData(controller);
        tab.setGraphic(GuiUtil.creatConnectionIcon());
        GuiUtil.setTab(tab,this.serverTabPane,tuple2);
        //写入最近连接记录
        recentHistory.add(redisContext.getRedisConfig());
    }

    /**
     * 新建redis连接tab页
     * @param redisConfig redis连接配置
     */
    public void newRedisTab(RedisConfig redisConfig)  {
        RedisContext redisContext = RedisFactorySingleton.getInstance().createRedisContext(redisConfig);
        Message message = redisContext.useRedisClient().testConnect();
        if (!message.isSuccess()) {
            GuiUtil.alert(Alert.AlertType.WARNING, message.getMessage());
            return;
        }
        try {
            this.newRedisTab(redisContext,redisConfig.getName());
        } catch (IOException e) {
            log.error("new redis tab exception",e);
            throw new RedisException(e.getMessage());
        }
    }

    /**
     * 打开设置窗口
     * @param actionEvent 事件
     * @param clazz 页面类
     */
    public void openSettings(ActionEvent actionEvent,Class<? extends Page> clazz) {
        MainModel.DEFAULT_PAGE = clazz;
        openSettings(actionEvent);
    }
    /**
     * 打开设置窗口
     * @param actionEvent  事件
     */
    @FXML
    public void openSettings(ActionEvent actionEvent) {
        if(this.settingsStage!=null){
            if(!this.settingsStage.isShowing()){
                settingsStage.show();
            }
        }else{
            this.settingsStage=new Stage();
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.getIcons().add(GuiUtil.ICON_REDIS);
            this.settingsStage.setTitle(language("main.file.setting"));
            ApplicationWindow applicationWindow = new ApplicationWindow();
            var antialiasing = Platform.isSupported(ConditionalFeature.SCENE3D)
                    ? SceneAntialiasing.BALANCED
                    : SceneAntialiasing.DISABLED;
            Scene scene = new Scene(applicationWindow,ApplicationWindow.MIN_WIDTH + 80, 768, false, antialiasing);
            Main.initTm(scene);
            this.settingsStage.initOwner(root.getScene().getWindow());
            this.settingsStage.setScene(scene);
            this.settingsStage.show();
        }
    }

    /**
     * 清除所有最近连接记录
     * @param actionEvent  事件
     */
    @FXML
    public void clearHistory(ActionEvent actionEvent) {
        this.recentHistory.clear();
    }

    /**
     * 关闭tabPan当前服务
     * @param actionEvent  事件
     */
    @FXML
    public void closeCurrentServer(ActionEvent actionEvent) {
        Tab selectedItem = this.serverTabPane.getSelectionModel().getSelectedItem();
        if(selectedItem!=null){
            GuiUtil.closeTab(this.serverTabPane,selectedItem);
        }
    }

    /**
     * 关闭所有服务
     * @param actionEvent  事件
     */
    @FXML
    public void closeServerAll(ActionEvent actionEvent) {
        ObservableList<Tab> tabs = this.serverTabPane.getTabs();
        GuiUtil.closeTab(this.serverTabPane,new ArrayList<>(tabs));
    }

    /**
     * 快速新建连接
     * @param actionEvent  事件
     */
    @FXML
    public void newConnection(ActionEvent actionEvent) {
        openServerLinkWindow(actionEvent);
        serverConnectionsController.newConnection(actionEvent);
    }

    /**
     * 快速新建分组
     * @param actionEvent  事件
     */
    @FXML
    public void newGroup(ActionEvent actionEvent) {
        openServerLinkWindow(actionEvent);
        serverConnectionsController.newGroup(actionEvent);
    }

    /**
     * 快速连接
     * @param actionEvent  事件
     */
    public void quickConnection(ActionEvent actionEvent) {
        openServerLinkWindow(actionEvent);
        serverConnectionsController.quickConnection();
    }

    /**
     * 退出
     * @param actionEvent  事件
     */
    @FXML
    public void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    /**
     * 最大化
     * @param actionEvent  事件
     */
    @FXML
    public void maximized(ActionEvent actionEvent) {
        currentStage.setMaximized(true);
    }

    /**
     * 最小化
     * @param actionEvent  事件
     */
    @FXML
    public void minimized(ActionEvent actionEvent) {
        currentStage.setIconified(true);

    }

    /**
     * 全屏
     * @param actionEvent  事件
     */
    @FXML
    public void fullScreen(ActionEvent actionEvent) {
        currentStage.setFullScreen(true);
    }

    /**
     * 重置窗口大小
     * @param actionEvent  事件
     */
    @FXML
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

    /**
     * 服务器tab页顶部
     * @param actionEvent  事件
     */
    @FXML
    public void serverTabTop(ActionEvent actionEvent) {
        GuiUtil.setTabPaneSide(serverTabPane,Side.TOP);
    }

    /**
     * 服务器tab页底部
     * @param actionEvent  事件
     */
    @FXML
    public void serverTabBottom(ActionEvent actionEvent) {
        GuiUtil.setTabPaneSide(serverTabPane,Side.BOTTOM);
    }

    /**
     * 服务器tab页左侧
     * @param actionEvent  事件
     */
    @FXML
    public void serverTabLeft(ActionEvent actionEvent) {
        GuiUtil.setTabPaneSide(serverTabPane,Side.LEFT);
    }

    /**
     * 服务器tab页右侧
     * @param actionEvent  事件
     */
    @FXML
    public void serverTabRight(ActionEvent actionEvent) {
        GuiUtil.setTabPaneSide(serverTabPane,Side.RIGHT);
    }

    /**
     * keytab页顶部
     * @param actionEvent  事件
     */
    @FXML
    public void keyTabTop(ActionEvent actionEvent) {
        setKeyTabSide(Side.TOP);
    }

    /**
     * 设置key tab页放置位置
     * @param side  side
     */
    private void setKeyTabSide( Side side) {
        for (Tab tab : this.serverTabPane.getTabs()) {
            Object userData = tab.getUserData();
            if(userData instanceof ServerTabController stc){
                GuiUtil.setTabPaneSide( stc.dbTabPane,side);
            }
        }
    }

    /**
     * keytab页底部
     * @param actionEvent  事件
     */
    @FXML
    public void keyTabBottom(ActionEvent actionEvent) {
        setKeyTabSide(Side.BOTTOM);
    }

    /**
     * keytab页左侧
     * @param actionEvent  事件
     */
    @FXML
    public void keyTabLeft(ActionEvent actionEvent) {
        setKeyTabSide(Side.LEFT);
    }

    /**
     * keytab页右侧
     * @param actionEvent  事件
     */
    @FXML
    public void keyTabRight(ActionEvent actionEvent) {
        setKeyTabSide(Side.RIGHT);
    }

    /**
     * 改进建议和问题反馈，直接条issues页面
     */
    @FXML
    public void suggest(ActionEvent actionEvent) {
        String issuesUrl = "";
        try {
            issuesUrl =System.getProperty(Constant.APP_HOME_PAGE)+ "/issues";
            Desktop.getDesktop().browse(new URI(issuesUrl));
        } catch (IOException | URISyntaxException e) {
            log.error("unable to open the browser", e);
            GuiUtil.alert(Alert.AlertType.ERROR, String.format(language("alert.message.help.suggest")+": %s", issuesUrl));
        }
    }
    @FXML
    public void guide(ActionEvent actionEvent) {
        String guideUrl = "";
        try {
            guideUrl =System.getProperty(Constant.DOC_HOME_PAGE)+ "/user-manual";
            Desktop.getDesktop().browse(new URI(guideUrl));
        } catch (IOException | URISyntaxException e) {
            log.error("unable to open the browser", e);
            GuiUtil.alert(Alert.AlertType.ERROR, String.format(language("alert.message.help.suggest")+": %s", guideUrl));
        }
    }

    /**
     * 打开欢迎页
     * @param actionEvent 事件
     */
    @FXML
    public void welcome(ActionEvent actionEvent) {
        Tuple2<Node,WelcomeController> tuple2 = loadFxml("/fxml/WelcomeView.fxml",BaseWindowController.NONE);
        Node borderPane = tuple2.t1();
        WelcomeController controller = tuple2.t2();
        Tab tab = new Tab(Constant.WELCOME_TAB_NAME);
        tab.setUserData(controller);
        tab.setGraphic(new FontIcon(Feather.HOME));
        tab.setContent(borderPane);
        GuiUtil.setTab(tab,this.serverTabPane,tuple2);
    }

    /**
     * 重启窗口
     * @param actionEvent 事件
     */
    @FXML
    public void restartWindow(ActionEvent actionEvent) {
        Main.instance.restart();
    }
    /**
     * 关于
     * @param actionEvent 事件
     */
    @FXML
    public void about(ActionEvent actionEvent) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(language("main.help.about"));
        alert.setHeaderText(null);
        alert.setGraphic(new ImageView(GuiUtil.svgImage("/svg/fx_icon.svg",64)));
        alert.setContentText(("%s\n\n%s\n\n"+language("main.help.about.copyright")).formatted(Applications.NODE_APP_NAME,"v" + System.getProperty(Constant.APP_VERSION), Year.now().getValue()));
        alert.initOwner(this.currentStage);
        // 应用尺寸调整
        // 获取内容标签并计算实际所需宽度
        DialogPane dialogPane = alert.getDialogPane();
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setWrapText(true);
            // 计算文本所需宽度
            double textWidth = GuiUtil.computeTextWidth(contentLabel.getFont(), alert.getContentText(), 300);
            // 设置合适的宽度 (图标64px + 间距 + 文本宽度 + 边距)
            double desiredWidth = Math.max(250, Math.min(400, 64 + 5 + textWidth + 5));
            dialogPane.setPrefWidth(desiredWidth);
        }
        alert.getDialogPane().getScene().getWindow().sizeToScene();
        alert.show();

    }

    /**
     * 检查更新
     * @param actionEvent 事件
     */
    @FXML
    public void update(ActionEvent actionEvent) {
        final var msg = new Notification(
                language("main.help.update.loading"),
                new ProgressIndicator()
        );
        ProgressIndicator progressIndicator = (ProgressIndicator) msg.getGraphic();
        progressIndicator.setPrefSize(22, 22);
        progressIndicator.setMaxSize(22, 22);
        msg.getStyleClass().addAll(
                Styles.ELEVATED_1
        );
        AnchorPane.setRightAnchor(msg,30d);
        AnchorPane.setTopAnchor(msg,30d);
        msg.setOnClose(e -> {
            var out = Animations.slideOutRight(msg, Duration.millis(250));
            out.setOnFinished(f -> center.getChildren().remove(msg));
            out.playFromStart();
        });
        var in = Animations.slideInRight(msg, Duration.millis(250));
        if (!center.getChildren().contains(msg)) {
            center.getChildren().add(msg);
        }
        in.playFromStart();
        checkForUpdatesAsync(msg);
    }


    /**
     * 异步调接口检查更新
     */
    private  void checkForUpdatesAsync(Notification msg) {
        async(()->{
            String apiUrl = System.getProperty(Constant.APP_PROPERTIES);
            Properties properties =getReleaseProperties(apiUrl);
            if (properties == null) {
                apiUrl = System.getProperty(Constant.APP_PROPERTIES2);
                properties =getReleaseProperties(apiUrl);
            }
            Properties finalProperties = properties;
            Platform.runLater(()->{
                checkToMsg(msg, finalProperties);
            });
        });

    }

    /**
     * 检查更新结果
     * @param properties 配置
     */
    private void checkToMsg(Notification msg, Properties properties) {
        if (properties == null) {
            msg.setMessage(language("main.help.update.fail"));
            msg.getStyleClass().addAll(
                    Styles.DANGER
            );
            msg.setGraphic(new FontIcon(Material2OutlinedAL.ERROR_OUTLINE));
            return;
        }
        msg.getStyleClass().addAll(
                Styles.SUCCESS
        );
        msg.setGraphic(new FontIcon(Material2OutlinedAL.CHECK_CIRCLE_OUTLINE));
        String releaseVersion = properties.getProperty(Constant.APP_VERSION);
        String currentVersion = System.getProperty(Constant.APP_VERSION);
        //已经是最新了
        if(currentVersion.equals(releaseVersion)){
            msg.setMessage(language("main.help.update.new"));
            CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> {
                Platform.runLater(() -> msg.getOnClose().handle(null));
            });
            return;
        }
        msg.setMessage(language("main.help.update.latest")+" v"+releaseVersion);
        var btn = getDownloadButton(msg);
        msg.setPrimaryActions(btn);
    }

    /**
     * 获取下载按钮
     * @param msg 消息
     * @return 按钮
     */
    private static @NotNull Button getDownloadButton(Notification msg) {
        var btn = new Button(language("main.help.update.download"));
        btn.setOnAction(e -> {
           String downloadUrl =System.getProperty(Constant.APP_HOME_PAGE)+ "/releases/latest";
            try {
                Desktop.getDesktop().browse(new URI(downloadUrl));
            } catch (IOException | URISyntaxException ex) {
                log.error("unable to open the browser:{}",downloadUrl, ex);
                GuiUtil.alert(Alert.AlertType.ERROR, String.format(language("alert.message.help.suggest")+": %s", downloadUrl));
            }finally {
                msg.getOnClose().handle(null);
            }

        });
        return btn;
    }

    /**
     * 从接口获取发布信息
     * @param apiUrl 接口地址
     * @return 发布信息
     */
    private Properties getReleaseProperties(String apiUrl) {
        try (HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(3))
                .build()){
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("getReleaseProperties url:{}",apiUrl);
            if (response.statusCode() == 200) {
                // 读取响应
                Properties properties = new Properties();
                properties.load(new StringReader(response.body()));
                return properties;
            }
        } catch (IOException|InterruptedException e) {
           log.error("getReleaseProperties exception:{} ",apiUrl , e);
        }
        return null;

    }

    public void test(ActionEvent actionEvent) {
        super.loadSubWindow("自定义编解码器", "/fxml/setting/NewCustomConverterView.fxml", root.getScene().getWindow(), ADD);
    }



}
