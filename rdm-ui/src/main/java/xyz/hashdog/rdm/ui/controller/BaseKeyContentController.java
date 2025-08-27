package xyz.hashdog.rdm.ui.controller;

/**
 * key内容控制器
 * 打开key的窗口才用这个基类
 * @author th
 */
public abstract class BaseKeyContentController extends  BaseKeyController<KeyTabController> {

    /**
     * 重新加载数据
     */
    abstract public void reloadInfo() ;

    @Override
    public void paramInitEnd() {
        initInfo();
    }
    /**
     * 初始化数据
     */
    protected abstract void initInfo();
}
