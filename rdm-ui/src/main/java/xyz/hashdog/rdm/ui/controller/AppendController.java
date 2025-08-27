package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/12 22:10
 */
public class AppendController extends BaseWindowController<BaseKeyController> implements Initializable {
    @FXML
    public BorderPane borderPane;
    @FXML
    public Button ok;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ok.getStyleClass().add(Styles.ACCENT);

    }


    /**
     * 设置内容
     * @param t1 子容器
     */
    public void setSubContent(Pane t1) {
        borderPane.setCenter(t1);
    }
}
