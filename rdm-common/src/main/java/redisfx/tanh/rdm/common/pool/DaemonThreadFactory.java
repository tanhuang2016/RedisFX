/**
 * <p>Title:DaemonThreadFactory.java</p>
 * <p>Description: 守护线程池工厂</p>
 * <p>Copyright:研发部 Copyright(c)2022</p>
 * <p>Date:2022-07-02</p>
 *
 * @author th
 * @version 1.0.0
 * @version 1.0
 */
package redisfx.tanh.rdm.common.pool;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 守护线程池工厂
 * @author th
 */
public class DaemonThreadFactory implements ThreadFactory {

    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;

    DaemonThreadFactory(String name) {
        this(name, 1);
    }

    DaemonThreadFactory(String name, int firstThreadNum) {
        threadNumber.set(firstThreadNum);
        group = Thread.currentThread().getThreadGroup();
        namePrefix = name + "-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (!t.isDaemon()) {
            t.setDaemon(true);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

}