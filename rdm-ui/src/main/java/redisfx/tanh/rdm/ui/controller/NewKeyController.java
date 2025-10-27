package redisfx.tanh.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import redisfx.tanh.rdm.redis.Message;
import redisfx.tanh.rdm.ui.common.RedisDataTypeEnum;
import redisfx.tanh.rdm.ui.controller.base.BaseClientController;
import redisfx.tanh.rdm.ui.handler.NewKeyHandler;
import redisfx.tanh.rdm.ui.util.GuiUtil;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/11 22:56
 */
public class NewKeyController extends BaseClientController<ServerTabController> implements Initializable {

    /**
     * 当前Stage
     */
    public Stage currentStage;

    @FXML
    public TextField key;

    @FXML
    public TextField ttl;
    public Button ok;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ok.getStyleClass().add(Styles.ACCENT);
        filterIntegerInputListener(true,this.ttl);
        
    }

    @FXML
    public void ok(ActionEvent actionEvent) {
        if(GuiUtil.requiredTextField(key, ttl)){
            return;
        }
        String keyType = this.parameter.get().getKeyType();
        RedisDataTypeEnum byType = RedisDataTypeEnum.getByType(keyType);
        NewKeyHandler newKeyHandler = byType.newKeyHandler;
        Message message = newKeyHandler.newKey(this.redisClient, this.currentDb, key.getText(), Long.parseLong(ttl.getText()));
        if(message.isSuccess()){
            //将新增的key,添加到参数
            this.parameter.get().setKey(key.getText());
            //调父层,增加列表
            this.parentController.addKeyAndSelect(this.parameter);
            cancel(null);
        }


    }


    @FXML
    public void cancel(ActionEvent actionEvent) {
        currentStage.close();
    }



    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
        this.currentStage.getIcons().add(GuiUtil.ICON_REDIS);
    }
}
