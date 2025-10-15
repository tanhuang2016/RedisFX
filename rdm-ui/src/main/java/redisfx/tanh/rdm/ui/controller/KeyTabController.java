package redisfx.tanh.rdm.ui.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Popover;
import atlantafx.base.theme.Styles;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import redisfx.tanh.rdm.common.pool.ThreadPool;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.ui.Main;
import redisfx.tanh.rdm.ui.common.Constant;
import redisfx.tanh.rdm.ui.common.RedisDataTypeEnum;
import redisfx.tanh.rdm.ui.controller.base.BaseClientController;
import redisfx.tanh.rdm.ui.controller.base.BaseKeyController;
import redisfx.tanh.rdm.ui.controller.popover.RefreshPopover;
import redisfx.tanh.rdm.ui.entity.PassParameter;
import redisfx.tanh.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static redisfx.tanh.rdm.ui.common.Constant.*;
import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 */
public class KeyTabController extends BaseClientController<ServerTabController> implements RefreshPopover.IRefreshPopover,Initializable {


    @FXML
    public CustomTextField key;
    @FXML
    public CustomTextField ttl;
    @FXML
    public Label keyType;
    @FXML
    public BorderPane borderPane;
    public Button keyRefresh;
    public Button keyDelete;
    public Label keyRename;
    public Label keyEditTtl;
    public Label refreshText;


    private long currentTtl;


    private Popover refreshPopover;


    /**
     * 子类型控制层
     */
    private BaseKeyController subTypeController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLanguage();
        initListener();
        initButton();
        initTextField();
        initLabel();

    }

    @Override
    protected void initLanguage() {
        keyRename.setTooltip(GuiUtil.textTooltip(language("key.rename")));
        keyEditTtl.setTooltip(GuiUtil.textTooltip(language("key.edit")));
        keyDelete.setTooltip(GuiUtil.textTooltip(language("key.delete")));
        keyRefresh.setTooltip(GuiUtil.textTooltip(language("key.refresh")));
    }

    private void initLabel() {
        keyRename.getStyleClass().addAll( Styles.BUTTON_ICON,Styles.SUCCESS,Styles.FLAT);
        keyRename.setGraphic(new FontIcon(Feather.CHECK));
        keyRename.setCursor(Cursor.HAND);
        keyEditTtl.getStyleClass().addAll( Styles.BUTTON_ICON,Styles.SUCCESS,Styles.FLAT);
        keyEditTtl.setGraphic(new FontIcon(Feather.CHECK));
        keyEditTtl.setCursor(Cursor.HAND);

    }

    private void initTextField() {
        key.setRight(keyRename);
        ttl.setRight(keyEditTtl);
    }


    private void initButton() {
        initButtonIcon();
        initButtonStyles();
    }

    private void initButtonStyles() {
        keyRefresh.getStyleClass().addAll( Styles.BUTTON_ICON,Styles.SUCCESS);
        keyDelete.getStyleClass().addAll( Styles.BUTTON_ICON,Styles.DANGER);
    }

    private void initButtonIcon() {
        FontIcon fontIcon = new FontIcon(Feather.REFRESH_CW);
        GuiUtil.setIcon(keyRefresh,fontIcon);
        GuiUtil.setIcon(keyDelete,new FontIcon(Feather.TRASH_2));



    }

    /**
     * 初始化监听
     */
    private void initListener() {
        filterIntegerInputListener(true,this.ttl);
    }





    @Override
    protected void paramInitEnd() {
        initInfo();
    }

    /**
     * 重新加载
     */
    private void reloadInfo() {
        async(() -> {
            loadData();
            this.subTypeController.reloadInfo();
        });

    }

    /**
     * 加载数据
     */
    private void loadData() {

        this.currentTtl= this.exeRedis(j -> j.ttl(this.getParameter().getKey()));
        Platform.runLater(() -> {
            this.key.setText(this.getParameter().getKey());
            this.ttl.setText(String.valueOf(currentTtl));
            this.keyType.setText(this.getParameter().getKeyType());
        });

    }

    /**
     * 初始化数据展示
     */
    private void initInfo()  {
        Future<Boolean> submit = ThreadPool.getInstance().submit(() -> {
            //加载通用数据
            loadData();
            refreshTextUpdate();
        }, true);


        try {
            if(submit.get()){
                RedisDataTypeEnum te = RedisDataTypeEnum.getByType(this.parameter.get().getKeyType());
                Tuple2<AnchorPane, BaseKeyController> tuple2 = loadFxml(te.fxml);
                AnchorPane anchorPane = tuple2.t1();
                this.subTypeController  = tuple2.t2();
                PassParameter passParameter = new PassParameter(te.tabType);
                passParameter.setDb(this.currentDb);
                passParameter.setKey(this.parameter.get().getKey());
                passParameter.setKeyType(this.parameter.get().getKeyType());
                passParameter.setRedisClient(redisClient);
                passParameter.setRedisContext(redisContext);
                this.subTypeController.setParameter(passParameter);
                borderPane.setCenter(anchorPane);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


    }

    public void rename(MouseEvent mouseEvent) {
        if (GuiUtil.requiredTextField(this.key)) {
            return;
        }
        async(() -> {
            this.exeRedis(j -> j.rename(this.getParameter().getKey(), this.key.getText()));
            this.getParameter().setKey(this.key.getText());
            Platform.runLater(() -> GuiUtil.alert(Alert.AlertType.INFORMATION, language(ALERT_MESSAGE_RENAME_SUCCESS)));
        });
    }




    /**
     * 设置有效期
     *
     * @param mouseEvent 鼠标事件
     */
    @FXML
    public void editTtl(MouseEvent mouseEvent) {
        if (GuiUtil.requiredTextField(this.ttl)) {
            return;
        }
        int ttl = Integer.parseInt(this.ttl.getText());
        if (ttl <= -1) {
            if (GuiUtil.alert(Alert.AlertType.CONFIRMATION, language(ALERT_MESSAGE_SET_TTL))) {
                async(()->{
                    this.exeRedis(j -> j.persist(this.getParameter().getKey()));
                    Platform.runLater(()-> GuiUtil.alert(Alert.AlertType.INFORMATION,language(ALERT_MESSAGE_SET_SUCCESS)));
                });
            }
            return;
        }

        async(()->{
            this.exeRedis(j -> j.expire(this.getParameter().getKey(),ttl));
            Platform.runLater(()-> GuiUtil.alert(Alert.AlertType.INFORMATION,language(ALERT_MESSAGE_SET_SUCCESS)));
        });
    }

    /**
     * 删除键
     * 切需要关闭当前tab
     * @param actionEvent 鼠标事件
     */
    @FXML
    public void delete(ActionEvent actionEvent) {
        if (GuiUtil.alert(Alert.AlertType.CONFIRMATION, Main.RESOURCE_BUNDLE.getString(Constant.ALERT_MESSAGE_DEL))) {
            exeRedis(j -> j.del(parameter.get().getKey()));
            if(super.parentController.delKey(parameter)){
                super.parentController.removeTabByKeys(Collections.singletonList(parameter.get().getKey()));
            }
        }
    }



    /**
     * 刷新数据
     * @param actionEvent 鼠标事件
     */
    @FXML
    public void refresh(ActionEvent actionEvent) {
        reloadInfo();
        refreshTextUpdate();
    }

    @Override
    public void refresh() {
        refresh( null);
    }

    private long refreshTime;
    private boolean autoRefreshState=false;
    private Timeline refreshTextTimeline;

    /**
     * 每5秒更新刷新描述
     */
    private void refreshTextUpdate() {
        refreshTime=System.currentTimeMillis();
        updateRefreshText();
        if(!autoRefreshState){
            // 如果时间线已经存在，先停止它
            if (refreshTextTimeline != null) {
                refreshTextTimeline.stop();
            }

            // 创建新的时间线，每5秒更新一次
            refreshTextTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(5), event -> updateRefreshText())
            );
            refreshTextTimeline.setCycleCount(Timeline.INDEFINITE);
            refreshTextTimeline.play();
        }

    }

    /**
     * 更新刷新描述
     */
    private void updateRefreshText() {
        //如果是自动刷新，不更新描述，默认是自动刷新xxx
        if(autoRefreshState){
            return;
        }
        long currentTime = System.currentTimeMillis();
        long diffSeconds = (currentTime - refreshTime) / 1000;
        String text;
        if (diffSeconds < 5) {
            text = "now";
        } else if (diffSeconds < 30) {
            text = ">5s";
        } else if (diffSeconds < 60) {
            text = ">30s";
        } else if (diffSeconds < 120) {
            text = ">1min";
        } else if (diffSeconds < 180) {
            text = ">2min";
        } else if (diffSeconds < 240) {
            text = ">3min";
        } else if (diffSeconds < 300) {
            text = ">4min";
        } else if (diffSeconds < 600) {
            text = ">5min";
        } else if (diffSeconds < 900) {
            text = ">10min";
        } else if (diffSeconds < 1200) {
            text = ">15min";
        } else if (diffSeconds < 1500) {
            text = ">20min";
        } else if (diffSeconds < 1800) {
            text = ">25min";
        } else if (diffSeconds < 3600) {
            text = ">30min";
        } else {
            // 超过1小时，按小时计算
            long hours = diffSeconds / 3600;
            text = ">" + hours + "h";
        }
        refreshText.setText(text);
    }


    /**
     * 鼠标移入1.5秒，显示自动刷新设置弹窗
     * @param mouseEvent 鼠标事件
     */
    @FXML
    public void openRefreshPopover(MouseEvent mouseEvent) {
        if(refreshPopover!=null&&refreshPopover.isShowing()){
            return;
        }
        PauseTransition showRefreshPopoverDelay = new PauseTransition(Duration.seconds(1.5));
        showRefreshPopoverDelay.setOnFinished(event -> {
            if(refreshPopover!=null){
                refreshPopover.show(keyRefresh);
            }else {
                Tuple2<AnchorPane, RefreshPopover> tuple2 = loadFxml("/fxml/popover/RefreshPopover.fxml");
                AnchorPane root = tuple2.t1();
                var pop = new Popover(root);
                pop.setHeaderAlwaysVisible(false);
                pop.setDetachable(false);
                pop.setArrowLocation(Popover.ArrowLocation.TOP_CENTER);
                pop.show(keyRefresh);
                refreshPopover= pop;
            }

        });
        showRefreshPopoverDelay.play();
    }


    @Override
    public void setUpdateRefreshState(boolean isAutoRefresh, int rateValue) {
        autoRefreshState=isAutoRefresh;
        if(isAutoRefresh){
            refreshText.setText("now");
        }else {
            refreshText.setText(language("server.refresh.auto")+": "+rateValue+"s");
        }
    }

    @Override
    public void close() {
        super.close();
        refreshTextTimeline.stop();
        refreshTextTimeline=null;
    }
}
