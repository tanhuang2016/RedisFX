package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.redis.Message;
import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.RedisContext;
import xyz.hashdog.rdm.redis.RedisFactorySingleton;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;
import xyz.hashdog.rdm.ui.entity.config.AdvancedSetting;
import xyz.hashdog.rdm.ui.entity.config.ConnectionServerNode;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.common.Constant.ALERT_MESSAGE_CONNECT_SUCCESS;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * 新建连接的控制层
 * @author th
 * @version 1.0.0
 * @since 2023/7/19 21:45
 */
public class NewConnectionController extends BaseWindowController<ServerConnectionsController> implements Initializable {
    /**
     * 当前根节点
     */
    public AnchorPane root;

    /**
     * 测试连接按钮
     */
    @FXML
    public Button testConnectButton;
    /**
     * 连接名称
     */
    @FXML
    public TextField name;
    /**
     * 地址/域
     */
    @FXML
    public TextField host;
    /**
     * 端口
     */
    @FXML
    public Spinner<Integer> port;
    /**
     * 密码
     */
    @FXML
    public PasswordField auth;
    /**
     * 连接id,保存的时候会有
     */
    @FXML
    public TextField dataId;
    /**
     * 是否集群模式
     */
    @FXML
    public CheckBox cluster;
    /**
     * 是否哨兵模式
     */
    @FXML
    public CheckBox sentinel;
    /**
     * 哨兵模式下的master名称
     */
    @FXML
    public TextField masterName;
    /**
     * 哨兵模式下的节点列表
     */
    @FXML
    public VBox sentinelVBox;
    /**
     * 是否ssl
     */
    public CheckBox ssl;
    /**
     * ca证书
     */
    public TextField caCrt;
    /**
     * redis证书
     */
    public TextField redisCrt;
    /**
     * redis秘钥
     */
    public TextField redisKey;
    /**
     * 秘钥密码
     */
    public PasswordField redisKeyPassword;
    /**
     * ssh
     */
    public CheckBox ssh;
    /**
     * ssh地址
     */
    public TextField sshHost;
    /**
     * ssh端口
     */
    public Spinner<Integer> sshPort;
    /**
     * ssh用户名
     */
    public TextField sshUserName;
    /**
     * ssh密码
     */
    public PasswordField sshPassword;
    /**
     * ssh私钥
     */
    public TextField sshPrivateKey;
    /**
     * ssh密钥密码
     */
    public PasswordField sshPassphrase;
    /**
     * 连接超时
     */
    public Spinner<Integer> connectionTimeout;
    /**
     * 读取超时
     */
    public Spinner<Integer> soTimeout;
    /**
     * key分隔符
     */
    public TextField keySeparator;
    public Button caCrtButton;
    public Button redisCrtButton;
    public Button redisKeyButton;
    public Button sshPrivateKeyButton;
    public ToggleButton treeShow;
    public ToggleButton listShow;
    public String id=DataUtil.uuid();

    /**
     * 选中的最后的文件的父级目录
     */
    private File lastFile;


    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initSpinner();
        initButton();
        initListener();
        initToggleButton();
        initDefaultData();



    }

    private void initDefaultData() {
        initAdvancedSettingData();
    }
    private void initAdvancedSettingData() {
        AdvancedSetting setting = Applications.getConfigSettings(ConfigSettingsEnum.ADVANCED.name);
        setData(setting);
    }

    private void setData(AdvancedSetting setting) {
        connectionTimeout.getEditor().setText(String.valueOf(setting.getConnectionTimeout()));
        soTimeout.getEditor().setText(String.valueOf(setting.getConnectionTimeout()));
        treeShow.setSelected(setting.isTreeShow());
        listShow.setSelected(!setting.isTreeShow());
        keySeparator.setText(setting.getKeySeparator());
    }

    private void initSpinner() {
        port.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, 6379));
        sshPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, 22));
        connectionTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60_000, 6000));
        soTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60_000, 6000));
    }

    private void initToggleButton() {
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(treeShow,listShow);
        // 添加监听，确保至少一个按钮被选中
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (toggleGroup.getSelectedToggle() == null) {
                // 如果没有选中任何按钮，恢复上一个或默认选中
                if (oldToggle != null) {
                    oldToggle.setSelected(true);
                } else {
                    treeShow.setSelected(true);
                }
            }
        });
        GuiUtil.setIcon(treeShow,new FontIcon(Material2AL.ACCOUNT_TREE));
        GuiUtil.setIcon(listShow,new FontIcon(Material2MZ.VIEW_LIST));
        treeShow.getStyleClass().addAll(Styles.LEFT_PILL);
        listShow.getStyleClass().addAll(Styles.RIGHT_PILL);
    }

    private void initButton() {
        initButtonIcon();
        initButtonStyles();
    }

    private void initButtonStyles() {
        addButtonStyles(caCrtButton,redisCrtButton,redisKeyButton,sshPrivateKeyButton);
    }
    private void addButtonStyles(Button... button) {
        for (Button bu : button) {
            bu.getStyleClass().addAll( Styles.BUTTON_ICON);
        }

    }

    private void initButtonIcon() {
        GuiUtil.setIcon(caCrtButton,new FontIcon(Material2MZ.MORE_HORIZ));
        GuiUtil.setIcon(redisCrtButton,new FontIcon(Material2MZ.MORE_HORIZ));
        GuiUtil.setIcon(redisKeyButton,new FontIcon(Material2MZ.MORE_HORIZ));
        GuiUtil.setIcon(sshPrivateKeyButton,new FontIcon(Material2MZ.MORE_HORIZ));
    }


    /**
     * 初始化监听
     */
    private void initListener() {
        filterIntegerInputListener(false,this.port.getEditor(),this.sshPort.getEditor());
        initCheckBoxListener();
    }

    /**
     * 初始化checkbox监听,只能单选
     */
    private void initCheckBoxListener() {
        // 添加事件监听器来处理 CheckBox 的选中状态变化
        cluster.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                sentinel.setSelected(false);
            }
        });

        sentinel.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                cluster.setSelected(false);
                showSentinelVBox();
            }else {
                hideSentinelVBox();
            }
        });
    }

    private void showSentinelVBox() {
        sentinelVBox.setVisible(true);
        sentinelVBox.setManaged(true);
    }

    private void hideSentinelVBox() {
        sentinelVBox.setVisible(false);
        sentinelVBox.setManaged(false);
    }


    @FXML
    public void testConnect(ActionEvent actionEvent) {
        if(GuiUtil.requiredTextField(host, port.getEditor())){
            return;
        }
        String hostStr = host.getText();
        String portStr = port.getEditor().getText();
        String authStr = auth.getText();
        boolean clusterSelected = cluster.isSelected();
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost(hostStr);
        redisConfig.setPort(Integer.parseInt(portStr));
        redisConfig.setAuth(authStr);
        redisConfig.setCluster(clusterSelected);
        redisConfig.setSentinel(sentinel.isSelected());
        redisConfig.setMasterName(masterName.getText());
        redisConfig.setSsl(ssl.isSelected());
        redisConfig.setCaCrt(caCrt.getText());
        redisConfig.setRedisCrt(redisCrt.getText());
        redisConfig.setRedisKey(redisKey.getText());
        redisConfig.setRedisKeyPassword(redisKeyPassword.getText());
        redisConfig.setSsh(ssh.isSelected());
        redisConfig.setSshHost(sshHost.getText());
        redisConfig.setSshPort(DataUtil.isNotEmpty(sshPort.getEditor().getText()) ? Integer.parseInt(sshPort.getEditor().getText()) : 22);
        redisConfig.setSshUserName(sshUserName.getText());
        redisConfig.setSshPassword(sshPassword.getText());
        redisConfig.setSshPrivateKey(sshPrivateKey.getText());
        redisConfig.setSshPassphrase(sshPassphrase.getText());
        redisConfig.setConnectionTimeout(connectionTimeout.getValue());
        redisConfig.setSoTimeout(soTimeout.getValue());
        redisConfig.setKeySeparator(keySeparator.getText());
        redisConfig.setTreeShow(treeShow.isSelected());
        RedisContext redisContext = RedisFactorySingleton.getInstance().createRedisContext(redisConfig);
        try {
            Message message = redisContext.newRedisClient().testConnect();
            if (message.isSuccess()) {
                testConnectButton.getStyleClass().add(Styles.SUCCESS);
                GuiUtil.alert(Alert.AlertType.INFORMATION, language(ALERT_MESSAGE_CONNECT_SUCCESS));
            } else {
                testConnectButton.getStyleClass().add(Styles.DANGER);
                GuiUtil.alert(Alert.AlertType.WARNING, message.getMessage());
            }
        }catch (Exception e){
            testConnectButton.getStyleClass().add(Styles.DANGER);
            throw e;
        }

    }

    /**
     * 确定之后将新增的节点持久化
     * 再对父窗口视图进行更新
     * @param actionEvent
     */
    @FXML
    public void ok(ActionEvent actionEvent) {
        if(GuiUtil.requiredTextField(name,host, port.getEditor())){
            return;
        }
        ConnectionServerNode connectionServerNode =new ConnectionServerNode(ConnectionServerNode.SERVER);
        connectionServerNode.setName(name.getText());
        connectionServerNode.setHost(host.getText());
        connectionServerNode.setPort(Integer.parseInt(port.getEditor().getText()));
        connectionServerNode.setAuth(auth.getText());
        connectionServerNode.setCluster(cluster.isSelected());
        connectionServerNode.setSentine(sentinel.isSelected());
        connectionServerNode.setMasterName(masterName.getText());
        connectionServerNode.setSsl(ssl.isSelected());
        connectionServerNode.setCaCrt(caCrt.getText());
        connectionServerNode.setRedisCrt(redisCrt.getText());
        connectionServerNode.setRedisKey(redisKey.getText());
        connectionServerNode.setRedisKeyPassword(redisKeyPassword.getText());
        connectionServerNode.setSsh(ssh.isSelected());
        connectionServerNode.setSshHost(sshHost.getText());
        connectionServerNode.setSshPort(DataUtil.isNotEmpty(sshPort.getEditor().getText())?Integer.parseInt(sshPort.getEditor().getText()):0);
        connectionServerNode.setSshUserName(sshUserName.getText());
        connectionServerNode.setSshPassword(sshPassword.getText());
        connectionServerNode.setSshPrivateKey(sshPrivateKey.getText());
        connectionServerNode.setSshPassphrase(sshPassphrase.getText());
        connectionServerNode.setConnectionTimeout(connectionTimeout.getValue());
        connectionServerNode.setSoTimeout(soTimeout.getValue());
        connectionServerNode.setKeySeparator(keySeparator.getText());
        connectionServerNode.setTreeShow(treeShow.isSelected());
        connectionServerNode.setId(this.id);
        Message message=null;
        switch (this.model){
            case QUICK:
            case ADD:
                //父窗口树节点新增,切选中新增节点
                connectionServerNode.setParentDataId(super.parentController.getSelectedDataId());
                //更新或修改保存
                message=Applications.addOrUpdateConnectionOrGroup(connectionServerNode);
                //父窗口树节点新增,切选中新增节点
                parentController.AddConnectionOrGroupNodeAndSelect(connectionServerNode);
                break;
            case UPDATE:
                connectionServerNode.setDataId(dataId.getText());
                //更新或修改保存
                message=Applications.addOrUpdateConnectionOrGroup(connectionServerNode);
                //更新
                parentController.updateNodeInfo(connectionServerNode);
                break;

            default:
                break;

        }
        if(message.isSuccess()){
            currentStage.close();
        }
        //如果是快速连接的话，确认之后，直接打开连接
        if(this.model==QUICK){
            parentController.connect(actionEvent);
        }

    }




    /**
     * 填充编辑数据
     * @param selectedNode
     */
    public void editInfo(ConnectionServerNode selectedNode) {
        name.setText(selectedNode.getName());
        host.setText(selectedNode.getHost());
        port.getEditor().setText(String.valueOf(selectedNode.getPort()));
        auth.setText(selectedNode.getAuth());
        dataId.setText(selectedNode.getDataId());
        cluster.setSelected(selectedNode.isCluster());
        sentinel.setSelected(selectedNode.isSentine());
        masterName.setText(selectedNode.getMasterName());
        ssl.setSelected(selectedNode.isSsl());
        caCrt.setText(selectedNode.getCaCrt());
        redisCrt.setText(selectedNode.getRedisCrt());
        redisKey.setText(selectedNode.getRedisKey());
        redisKeyPassword.setText(selectedNode.getRedisKeyPassword());
        ssh.setSelected(selectedNode.isSsh());
        sshHost.setText(selectedNode.getSshHost());
        sshPort.getEditor().setText(String.valueOf(selectedNode.getSshPort()));
        sshUserName.setText(selectedNode.getSshUserName());
        sshPassword.setText(selectedNode.getSshPassword());
        sshPrivateKey.setText(selectedNode.getSshPrivateKey());
        sshPassphrase.setText(selectedNode.getSshPassphrase());
        connectionTimeout.getEditor().setText(String.valueOf(selectedNode.getConnectionTimeout()));
        soTimeout.getEditor().setText(String.valueOf(selectedNode.getSoTimeout()));
        keySeparator.setText(selectedNode.getKeySeparator());
        treeShow.setSelected(selectedNode.isTreeShow());
        listShow.setSelected(!selectedNode.isTreeShow());
        this.id=selectedNode.getId();

    }

    public void caCrtFile(ActionEvent actionEvent) {
        File file = GuiUtil.fileChoose(this.root.getScene().getWindow(), lastFile);
        lastFile=file.getParentFile();
        this.caCrt.setText(file.getPath());
    }

    public void redisCrtFile(ActionEvent actionEvent) {
        File file = GuiUtil.fileChoose(this.root.getScene().getWindow(), lastFile);
        lastFile=file.getParentFile();
        this.redisCrt.setText(file.getPath());
    }

    public void redisKeyFile(ActionEvent actionEvent) {
        File file = GuiUtil.fileChoose(this.root.getScene().getWindow(), lastFile);
        lastFile=file.getParentFile();
        this.redisKey.setText(file.getPath());
    }

    public void sshPrivateKeyFile(ActionEvent actionEvent) {
        File file = GuiUtil.fileChoose(this.root.getScene().getWindow(), lastFile);
        lastFile=file.getParentFile();
        this.sshPrivateKey.setText(file.getPath());
    }
}
