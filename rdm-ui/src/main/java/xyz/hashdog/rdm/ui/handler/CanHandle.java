package xyz.hashdog.rdm.ui.handler;

/**
 * 判断数据是否可以处理
 *
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public interface CanHandle {
    int DEFAULT_ORDER =Integer.MAX_VALUE/2;
    /**
     * 判断数据是否可以处理
     *
     * @param data 数据
     * @return true为可以 处理
     */
    boolean accept(byte[] data);


    /**
     * 顺序,越小优先级越高
     * @return 顺序
     */
    default int order() {
        return DEFAULT_ORDER;
    }
}
