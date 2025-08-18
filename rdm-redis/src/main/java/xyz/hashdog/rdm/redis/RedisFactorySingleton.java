package xyz.hashdog.rdm.redis;

import java.util.ServiceLoader;

/**
 * 静态内部类实现单例
 * RedisFactory只用获取一次,使用spi机制
 * @author th
 * @version 1.0.0
 * @since 2023/7/19 9:59
 */
    public class RedisFactorySingleton {
    private RedisFactorySingleton() {
    }

    /**
     * 获取RedisFactory单例
     */
    public static RedisFactory getInstance(){
        return SingletonHolder.INSTANCE;
    }
    private static class SingletonHolder{
        private static final RedisFactory INSTANCE;
        static {
            INSTANCE= spi(RedisFactory.class);
        }
    }

    /**
     * spi获取此类服务
     *
     */
    public static <T> T spi(Class<T> clazz) {
        ServiceLoader<T> load = ServiceLoader.load(clazz);
        //循环获取所需的对象
        for (T next : load) {
            if (next != null) {
                return next;
            }
        }
        throw new RuntimeException("no such spi:" + clazz.getName());
    }

}
