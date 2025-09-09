package xyz.hashdog.rdm.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.controller.base.BaseKeyController;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.nio.charset.StandardCharsets;

import static xyz.hashdog.rdm.ui.common.Constant.ALERT_MESSAGE_SAVE_SUCCESS;
import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/31 12:08
 */
public class StringTypeController extends BaseKeyController {


    public BorderPane borderPane;
    private ByteArrayController byteArrayController;
    /**
     * 当前value的二进制
     */
    private byte[] currentValue;

    @FXML
    public void initialize() {
        initLanguage();
    }
    @Override
    protected void initInfo() {
        async(() -> {
            this.currentValue = this.exeRedis(j -> j.get(this.getParameter().getKey().getBytes(StandardCharsets.UTF_8)));
            Platform.runLater(() -> {
                Tuple2<AnchorPane, ByteArrayController> tuple2 = loadFxml("/fxml/ByteArrayView.fxml");
                AnchorPane anchorPane = tuple2.t1();
                this.byteArrayController = tuple2.t2();
                this.byteArrayController.setByteArray(this.currentValue);
                borderPane.setCenter(anchorPane);
            });
        });
    }


    /**
     * 保存值
     *
     * @param actionEvent 事件
     */
    @FXML
    public void save(ActionEvent actionEvent) {
        byte[] byteArray = byteArrayController.getByteArray();
        async(() -> {
            exeRedis(j -> j.set(this.getParameter().getKey().getBytes(), byteArray));
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
