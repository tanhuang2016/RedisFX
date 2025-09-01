package xyz.hashdog.rdm.redis;

import xyz.hashdog.rdm.redis.client.RedisClient;

import java.io.Closeable;

/**
 * redis上下文,提供了对redis操作及相关信息所有的包装
 * 多实例,可以由RedisFactory创建
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 10:48
 */
public interface RedisContext extends Closeable {
    /**
     * 获取一个新的redis客户端获，但连接池始终是一个
     */
    RedisClient newRedisClient();
    /**
     * 获取当前使用的redis客户端
     */
    RedisClient useRedisClient();

    /**
     * 获取redis的配置
     */
    RedisConfig getRedisConfig();


    @Override
    void close();
}
