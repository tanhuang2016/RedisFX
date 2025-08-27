package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.common.ValueTypeEnum;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.common.Constant.ALERT_MESSAGE_SAVE_SUCCESS;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2025/7/13 12:08
 */
public class JsonTypeController extends BaseKeyContentController implements Initializable {


    public BorderPane borderPane;
    public Button save;
    private ByteArrayController byteArrayController;
    /**
     * 当前value的二进制
     */
    private byte[] currentValue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initListener();
        initButton();
    }

    private void initButton() {
        save.getStyleClass().add(Styles.ACCENT);
    }

    /**
     * 初始化监听
     */
    private void initListener() {
    }





    @Override
    protected void initInfo() {
        ThreadPool.getInstance().execute(() -> {
                    String bytes = this.exeRedis(j -> j.jsonGet(this.getParameter().getKey()));
                    this.currentValue = bytes.getBytes();
                    Platform.runLater(()->{
                        Tuple2<AnchorPane,ByteArrayController> tuple2 = loadFxml("/fxml/ByteArrayView.fxml");
                        AnchorPane anchorPane = tuple2.t1();
                        this.byteArrayController  = tuple2.t2();
                        this.byteArrayController.setParentController(this);
                        this.byteArrayController.setByteArray(this.currentValue, ValueTypeEnum.JSON);
                        borderPane.setCenter(anchorPane);
                    });
        });


    }


    /**
     * 保存值
     * @param actionEvent
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        byte[] byteArray = byteArrayController.getByteArray();
        async(() -> {
            exeRedis(j -> j.jsonSet(this.getParameter().getKey(), new String(byteArray)));
            Platform.runLater(() -> {
                byteArrayController.setByteArray(byteArray);
                GuiUtil.alert(Alert.AlertType.INFORMATION, language(ALERT_MESSAGE_SAVE_SUCCESS));
            });
        });
    }
    @Override
    public void reloadInfo() {
        initInfo();
    }
}
