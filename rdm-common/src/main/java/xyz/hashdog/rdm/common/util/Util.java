package xyz.hashdog.rdm.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author th
 * @since 2025/8/17 14:05
 */
public class Util {
    protected static Logger log = LoggerFactory.getLogger(Util.class);
    /**
     * 关闭资源
     * @param closeable 需要关闭的资源
     */
    public static void close(AutoCloseable... closeable) {
        for (AutoCloseable close : closeable) {
            if (null != close) {
                try {
                    close.close();
                } catch (Exception e) {
                   log.error("close Exception", e);
                }
            }
        }
    }
}
