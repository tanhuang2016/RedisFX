package xyz.hashdog.rdm.ui.controller;

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
     * 参数初始化结束后调用
     */
    protected abstract void initInfo();
}
