package xyz.hashdog.rdm.ui.controller.base;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import xyz.hashdog.rdm.ui.controller.KeyTabController;


/**
 * key内容控制器
 * 打开key的窗口才用这个基类
 * @author th
 */
public abstract class BaseKeyContentController extends  BaseKeyController<KeyTabController>  {
    protected static final String SIZE = "Size:%dB";
    protected static final String TOTAL = "Total:%d";
    protected static final int ROWS_PER_PAGE = 32;
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

    private void initCommon() {
        save.getStyleClass().add(Styles.ACCENT);
    }

    /**
     * 初始化数据
     */
    protected abstract void initInfo();
}
