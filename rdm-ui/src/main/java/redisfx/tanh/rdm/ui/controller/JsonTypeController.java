package redisfx.tanh.rdm.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.ui.common.ValueTypeEnum;
import redisfx.tanh.rdm.ui.controller.base.BaseKeyController;
import redisfx.tanh.rdm.ui.util.GuiUtil;

import static redisfx.tanh.rdm.ui.common.Constant.ALERT_MESSAGE_SAVE_SUCCESS;
import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2025/7/13 12:08
 */
public class JsonTypeController extends BaseKeyController {


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
            String bytes = this.exeRedis(j -> j.jsonGet(this.getParameter().getKey()));
            this.currentValue = bytes.getBytes();
            Platform.runLater(() -> {
                Tuple2<AnchorPane, ByteArrayController> tuple2 = loadFxml("/fxml/ByteArrayView.fxml");
                AnchorPane anchorPane = tuple2.t1();
                this.byteArrayController = tuple2.t2();
                this.byteArrayController.setByteArray(this.currentValue, ValueTypeEnum.JSON);
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
            exeRedis(j -> j.jsonSet(this.getParameter().getKey(), new String(byteArray)));
            Platform.runLater(() -> {
                byteArrayController.setByteArray(byteArray);
                GuiUtil.messageSaveSuccess();
            });
        });
    }

    @Override
    public void reloadInfo() {
        initInfo();
    }
}
