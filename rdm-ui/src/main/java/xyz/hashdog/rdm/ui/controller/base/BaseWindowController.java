package xyz.hashdog.rdm.ui.controller.base;

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



    /**
     * 取消
     * @param actionEvent  事件
     */
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
     */
    protected final <T2 extends BaseWindowController>T2 loadSubWindow(String title, String fxml, Window parent, int model)  {
        Stage newConnctionWindowStage = new Stage();
        newConnctionWindowStage.initModality(Modality.WINDOW_MODAL);
        //去掉最小化和最大化
        newConnctionWindowStage.initStyle(StageStyle.DECORATED);
        //禁用掉最大最小化
        newConnctionWindowStage.setMaximized(false);
        newConnctionWindowStage.setTitle(title);
        Tuple2<AnchorPane,T2> tuple2 = loadFxml(fxml);
        AnchorPane borderPane = tuple2.t1();
        T2 controller = tuple2.t2();
        controller.setCurrentStage(newConnctionWindowStage);
        Scene scene = new Scene(borderPane);
        newConnctionWindowStage.initOwner(parent);
        newConnctionWindowStage.setScene(scene);
        newConnctionWindowStage.show();
        controller.model=model;
        return controller;
    }

    /**
     * 设置当前Stage
     * @param currentStage 当前Stage
     */
    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
        this.currentStage.getIcons().add(GuiUtil.ICON_REDIS);
    }

    @Override
    public void close() {
        super.close();
    }
}
