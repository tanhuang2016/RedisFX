package xyz.hashdog.rdm.redis.imp.client;

import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.client.RedisClient;

import java.io.Closeable;

/**
 *
 * RedisClinent创建器
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 12:45
 */
public interface RedisClientCreator extends Closeable {
    /**
     * 创建redis客户端
     * @param redisConfig redis配置
     * @return redis客户端
     */
    RedisClient create(RedisConfig redisConfig);

    @Override
    void close();
}
