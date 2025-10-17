package redisfx.tanh.rdm.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author th
 * @since 2025/8/17 14:05
 */
public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);
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

    /**
     * 生成随机字符串
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
