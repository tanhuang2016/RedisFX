package xyz.hashdog.rdm.ui.controller.base;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.hashdog.rdm.common.pool.ThreadPool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.ui.sampler.event.DefaultEventBus;
import xyz.hashdog.rdm.ui.sampler.event.Event;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.SvgManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 *
 * 用于父子关系
 * 封装通用方法
 * @author th
 * @version 1.0.0
 * @since 2023/7/22 10:43
 */
public abstract class BaseController<T> implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(BaseController.class);
    /**
     * 父控制器
     */
    protected T parentController;
    /**
     * 子控制器
     */
    protected List<BaseController<?>> children = new CopyOnWriteArrayList<>();

    /**
     * 临时事件订阅者
     */
    protected final List<Consumer<? extends Event>> tmEventSubscribers=new ArrayList<>();



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
    @SuppressWarnings("unchecked")
    protected final  <T1,T2>Tuple2<T1,T2> loadFxml(String fxml) {
        //noinspection rawtypes
        Tuple2<T1, BaseController> tuple2 = GuiUtil.doLoadFxml(fxml);
        tuple2.t2().setParentController(this);
        this.addChild(tuple2.t2());
        return (Tuple2<T1, T2>) tuple2;
    }

    /**
     * 设置父控制器
     * @param parentController 父控制器
     */
   public void setParentController(T parentController) {
        this.parentController = parentController;
    }

    public T getParentController() {
        return parentController;
    }

    /**
     * 添加配置变更事件订阅者
     * @param eventType 订阅的事件类型
     * @param subscriber 订阅者
     * @param <E> 订阅的事件类型
     */
    public <E extends Event> void addTmEventSubscriber(Class<? extends E> eventType, Consumer<E> subscriber) {
        tmEventSubscribers.add( subscriber);
        DefaultEventBus.getInstance().subscribe(eventType, subscriber);
    }

    /**
     * 重置语言
     */
    public void resetLanguage(){
        this.children.forEach(BaseController::resetLanguage);
        initLanguage();
        log.info("resetLanguage:{}",this);
    }
    protected void initLanguage(){
       //方法存根
    }

    @Override
    public void close()  {
        //子窗口挨个关闭
        this.children.forEach(BaseController::close);
        //svn缓存清除
        SvgManager.clear(this);
        tmEventSubscribers.forEach(DefaultEventBus.getInstance()::unsubscribe);
        //父窗口需要清除已经关闭的子窗口的引用
        if(parentController instanceof BaseController<?> parent){
            parent.removeChild(this);
        }
        log.info("close:{}",this);
    }

   private void addChild(BaseController<?> t) {
        this.children.add(t);
    }
    private void removeChild(BaseController<?> t) {
        this.children.remove(t);
    }
}
