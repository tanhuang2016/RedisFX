package redisfx.tanh.rdm.redis.imp.client;

import redisfx.tanh.rdm.redis.client.RedisClient;

import java.io.Closeable;

/**
 *
 * RedisClient创建器
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 12:45
 */
public interface RedisClientCreator extends Closeable {
    /**
     * 创建redis客户端
     * @return redis客户端
     */
    RedisClient create();

    @Override
    void close();

    /**
     * 获取一个新的redis客户端，不会创建多余的连接池
     * @return redis客户端
     */
    RedisClient newRedisClient();
}
