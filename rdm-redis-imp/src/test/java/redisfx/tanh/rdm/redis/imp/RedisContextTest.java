package redisfx.tanh.rdm.redis.imp;

import org.junit.Before;
import org.junit.Test;
import redisfx.tanh.rdm.redis.RedisConfig;
import redisfx.tanh.rdm.redis.RedisContext;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 10:21
 */
public class RedisContextTest {

    private RedisContext redisContext;

    @Before
    public void before() {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("localhost");
        redisConfig.setPort(6379);
        redisContext = new redisfx.tanh.rdm.redis.imp.RedisContext(redisConfig);
    }

    @Test
    public void testConnect() {
        System.out.println(redisContext.newRedisClient().testConnect());
    }
}
