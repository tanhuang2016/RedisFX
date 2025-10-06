/**
 * <p>Title:public class ThreadPool {.java</p>
 * <p>Description: 守护线程池</p>
 * <p>Copyright:研发部 Copyright(c)2022</p>
 * <p>Date:2022-07-02</p>
 *
 * @author th
 * @version 1.0.0
 * @version 1.0
 */
package redisfx.tanh.rdm.common.pool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 守护线程池
 * 不一定会用到,所以使用双重检查实现单例
 * @author th
 * @version 1.0.0
 */
public class ThreadPool {
    /**
     * 当前系统核心数
     */
    private static final int NUM = Runtime.getRuntime().availableProcessors();
    /**
     * 最多两个现城
     */
    private static final int DEFAULT_CORE_SIZE = NUM <= 2 ? 1 : 2;
    /**
     * 最大队列数
     */
    private static final int MAX_QUEUE_SIZE = DEFAULT_CORE_SIZE + 2;
    /**
     * 线程池
     */
    private volatile static ThreadPoolExecutor executor;


    private ThreadPool() {
    }

    ;

    /**
     * 获取线程池
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor getInstance() {
        if (executor == null) {
            synchronized (ThreadPoolExecutor.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(DEFAULT_CORE_SIZE,
                            MAX_QUEUE_SIZE,
                            60 * 1000,
                            TimeUnit.MILLISECONDS,
                            new LinkedBlockingDeque<Runnable>(Integer.MAX_VALUE),
                            new DaemonThreadFactory("task")
                    );
                }
            }
        }
        return executor;
    }


}
