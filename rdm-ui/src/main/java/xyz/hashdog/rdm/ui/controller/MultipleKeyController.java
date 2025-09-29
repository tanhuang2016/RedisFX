package xyz.hashdog.rdm.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import xyz.hashdog.rdm.ui.controller.base.BaseController;
import xyz.hashdog.rdm.ui.controller.base.BaseWindowController;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class MultipleKeyController extends BaseController<ServerTabController> implements Initializable {
    public static final int DELETE=1;
    public static final int EXPORT=2;
    public int model;
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
