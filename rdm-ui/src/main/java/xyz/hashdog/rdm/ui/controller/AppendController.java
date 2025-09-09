package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import xyz.hashdog.rdm.ui.controller.base.BaseClientController;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;

import java.net.URL;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/12 22:10
 */
public class AppendController extends BaseWindowController<BaseClientController> implements Initializable {
    @FXML
    public BorderPane borderPane;
    @FXML
    public Button ok;
    public Button cancel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initLanguage();
        ok.getStyleClass().add(Styles.ACCENT);

    }

    @Override
    protected void initLanguage() {
        ok.setText(language("common.ok"));
        cancel.setText(language("common.cancel"));
    }


    /**
     * 设置内容
     * @param t1 子容器
     */
    public void setSubContent(Pane t1) {
        borderPane.setCenter(t1);
    }
}
