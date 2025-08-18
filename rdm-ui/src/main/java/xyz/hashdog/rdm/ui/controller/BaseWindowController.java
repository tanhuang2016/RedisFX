package xyz.hashdog.rdm.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.util.GuiUtil;

import java.io.IOException;

/**
 * 用于新开窗口的父子关系
 * 只用于对非redis操作的窗口
 * 如果需要对redis操作,需要用BaseKeyController
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 17:35
 */
public abstract class BaseWindowController<T> extends BaseController<T> {

    /**
     * 模式,默认是NONE
     */
    protected int model;
    protected static final int NONE = 1;
    protected static final int ADD = 2;
    protected static final int UPDATE = 3;
    protected static final int RENAME = 4;
    protected static final int QUICK = 5;
    /**
     * 当前Stage
     */
    public Stage currentStage;


    @FXML
    public void cancel(ActionEvent actionEvent) {
        currentStage.close();
    }

    /**
     * 子窗口模态框
     * 每次都是打开最新的
     *
     * @param title  窗口标题
     * @param fxml   fxml路径
     * @param parent 父窗口
     * @param model 模式
     * @throws IOException
     */
    protected <T extends BaseWindowController>T loadSubWindow(String title, String fxml, Window parent, int model) throws IOException {
        Stage newConnctionWindowStage = new Stage();
        newConnctionWindowStage.initModality(Modality.WINDOW_MODAL);
        //去掉最小化和最大化
        newConnctionWindowStage.initStyle(StageStyle.DECORATED);
        //禁用掉最大最小化
        newConnctionWindowStage.setMaximized(false);
        newConnctionWindowStage.setTitle(title);
        Tuple2<AnchorPane,T> tuple2 = loadFXML(fxml);
        AnchorPane borderPane = tuple2.t1();
        T controller = tuple2.t2();
        controller.setParentController(this);
        controller.setCurrentStage(newConnctionWindowStage);
        Scene scene = new Scene(borderPane);
        newConnctionWindowStage.initOwner(parent);
        newConnctionWindowStage.setScene(scene);
        newConnctionWindowStage.show();
        controller.setModel(model);
        return controller;
    }



    /**
     * 子窗口模态框
     * 每次都是打开最新的
     *
     * @param title  窗口标题
     * @param fxml   fxml路径
     * @param parent 父窗口
     * @throws IOException
     */
    public <T extends BaseWindowController>T loadSubWindow(String title, String fxml, Window parent) throws IOException {
        return loadSubWindow(title, fxml, parent,NONE);
    }




    public void setModel(int model) {
        this.model = model;
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
        this.currentStage.getIcons().add(GuiUtil.ICON_REDIS);
    }



}
