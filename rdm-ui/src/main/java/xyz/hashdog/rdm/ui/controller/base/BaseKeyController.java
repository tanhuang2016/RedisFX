package xyz.hashdog.rdm.ui.controller.base;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import xyz.hashdog.rdm.ui.controller.KeyTabController;


/**
 * key内容控制器
 * 打开key的窗口才用这个基类
 * @author th
 */
public abstract class BaseKeyController extends BaseClientController<KeyTabController> {

    @FXML
    public Button save;

    /**
     * 重新加载数据
     */
    abstract public void reloadInfo() ;

    @Override
    public void paramInitEnd() {
        initCommon();
        initInfo();
    }

     void initCommon() {
        if(save!=null){
            save.getStyleClass().add(Styles.ACCENT);
        }
    }

    /**
     * 初始化数据
     */
    protected abstract void initInfo();


    /**
     * 创建一个label 通用方法，需要自定义单个输入框
     * @param str label的文本
     * @return HBox
     */
    protected HBox createLabelHbox(String str) {
        Label label = new Label(str);
        label.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(label);
        HBox.setHgrow(label, Priority.ALWAYS);
        hBox.setPrefHeight(40);
        hBox.setMaxHeight(hBox.getPrefHeight());
        hBox.setMinHeight(hBox.getPrefHeight());
        hBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(hBox,Priority.ALWAYS);
        return hBox;
    }
}
