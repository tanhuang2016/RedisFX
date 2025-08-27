package xyz.hashdog.rdm.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.SvgManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 用于父子关系
 * 封装通用方法
 * @author th
 * @version 1.0.0
 * @since 2023/7/22 10:43
 */
public abstract class BaseController<T> implements AutoCloseable{
    /**
     * 父控制器
     */
    public T parentController;
    /**
     * 子控制器
     */
    public List<BaseController<?>> children = new ArrayList<>();

    /**
     * port只能为正整数
     * @param keyEvent 键盘事件
     */
    @FXML
    public void filterIntegerInput(KeyEvent keyEvent) {
        // 获取用户输入的字符
        String inputChar = keyEvent.getCharacter();
        // 如果输入字符不是整数，则阻止其显示在TextField中
        if (!inputChar.matches("\\d")) {
            keyEvent.consume();
        }
    }

    /**
     * 线程池异步执行
     * @param runnable 任务
     */
    protected void async(Runnable runnable) {
        ThreadPool.getInstance().execute(runnable);
    }

    /**
     * 只让输入整数
     */
    protected void filterIntegerInputListener(boolean flg,TextField... port) {
        GuiUtil.filterIntegerInput(flg,port);
    }

    /**
     * 加载fxml
     * @param fxml fxml文件名
     * @param <T1> 容器
     * @param <T2> 控制器
     * @return 容器和控制器
     */
    public <T1,T2>Tuple2<T1,T2> loadFxml(String fxml) {
        return GuiUtil.doLoadFxml(fxml);
    }

    /**
     * 设置父控制器
     * @param parentController 父控制器
     */
    public void setParentController(T parentController) {
        this.parentController = parentController;
    }

    @Override
    public void close()  {
        //子窗口挨个关闭
        this.children.forEach(BaseController::close);
        //svn缓存清除
        SvgManager.clear(this);
    }

    protected void addChild(BaseController<?> t) {
        this.children.add(t);
    }
}
