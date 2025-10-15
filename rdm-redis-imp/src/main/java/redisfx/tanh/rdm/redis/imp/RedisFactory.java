package redisfx.tanh.rdm.redis.imp;

import redisfx.tanh.rdm.redis.RedisConfig;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 10:32
 */
public class RedisFactory implements redisfx.tanh.rdm.redis.RedisFactory {

    @Override
    public redisfx.tanh.rdm.redis.RedisContext createRedisContext(RedisConfig redisConfig) {
        return new RedisContext(redisConfig);
    }
}
