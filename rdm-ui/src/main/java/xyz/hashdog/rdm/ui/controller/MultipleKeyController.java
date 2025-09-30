package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import xyz.hashdog.rdm.ui.common.Constant;
import xyz.hashdog.rdm.ui.controller.base.BaseController;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;
import xyz.hashdog.rdm.ui.entity.KeyTreeNode;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

public class MultipleKeyController extends BaseController<ServerTabController> implements Initializable {
    public static final int DELETE=1;
    public static final int EXPORT=2;
    public AnchorPane root;
    public TextArea textArea;
    public Label prompt;
    public Label total;
    public Button cancel;
    public Button ok;
    public CheckBox pttl;
    public Separator pttlSeparator;
    private CompletableFuture<Boolean> resultFuture;
    private Stage currentStage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textArea.setStyle("-fx-background-color: transparent; -fx-border-color: %s; -fx-border-radius: 5px;"
                .formatted(Constant.THEME_COLOR_BORDER_DEFAULT));
        ok.getStyleClass().add(Styles.ACCENT);
        prompt.getStyleClass().addAll(Styles.TEXT_BOLD,Styles.DANGER);
        total.getStyleClass().addAll(Styles.SUCCESS);
    }

    public void setResultFuture(CompletableFuture<Boolean> future) {
        this.resultFuture = future;
    }
    public void setModel(int model) {
        switch (model) {
            case DELETE:
                prompt.setText("请确认要删除的key");
                prompt.setGraphic(new FontIcon(Material2MZ.REPORT_PROBLEM));
                ok.setText("确认删除");
                break;
            case EXPORT:
                pttlSeparator.setVisible(true);
                pttl.setVisible(true);
                pttl.setText("导出时效");
                prompt.setText("请确认要导出的key");
                prompt.setGraphic(new FontIcon(Material2AL.GET_APP));
                ok.setText("确认导出");
                break;
        }
        cancel.setText(language("common.cancel"));
    }

    /**
     * 设置key列表，不进行属性设置
     * @param list key列表
     */
    public void setKeys(List<KeyTreeNode> list) {
        total.setText("数量 "+list.size());
        for (int i = 0; i < list.size(); i++) {
            textArea.appendText(i+1+": "+list.get(i).getKey()+"\n");
        }
    }
    public void setCurrentStage(Stage stage) {
        this.currentStage = stage;
        stage.setOnCloseRequest(event -> {
            if (resultFuture != null) {
                resultFuture.complete(false);
            }
            this.close();
        });
    }

    @FXML
    public void onConfirmAction(ActionEvent event) {
        // 执行确认逻辑
        if (resultFuture != null) {
            resultFuture.complete(true);
        }
        currentStage.close();
        this.close();
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        if (resultFuture != null) {
            resultFuture.complete(false);
        }
        currentStage.close();
        this.close();
    }



}
