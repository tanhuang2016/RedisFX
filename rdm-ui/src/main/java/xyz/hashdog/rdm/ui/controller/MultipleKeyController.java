package xyz.hashdog.rdm.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import xyz.hashdog.rdm.ui.controller.base.BaseController;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;
import xyz.hashdog.rdm.ui.entity.KeyTreeNode;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class MultipleKeyController extends BaseController<ServerTabController> implements Initializable {
    public static final int DELETE=1;
    public static final int EXPORT=2;
    public AnchorPane root;
    public TextArea textArea;
    private int model;
    private CompletableFuture<Boolean> resultFuture;
    private Stage currentStage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setResultFuture(CompletableFuture<Boolean> future) {
        this.resultFuture = future;
    }
    public void setModel(int model) {
        this.model = model;
    }

    /**
     * 设置key列表，不进行属性设置
     * @param list key列表
     */
    public void setKeys(List<KeyTreeNode> list) {
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
        });
    }

    @FXML
    public void onConfirmAction(ActionEvent event) {
        // 执行确认逻辑
        if (resultFuture != null) {
            resultFuture.complete(true);
        }
        currentStage.close();
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        if (resultFuture != null) {
            resultFuture.complete(false);
        }
        currentStage.close();
    }



}
