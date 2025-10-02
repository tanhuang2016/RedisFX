package redisfx.tanh.rdm.ui.common;

import org.junit.Before;
import org.junit.Test;
import redisfx.tanh.rdm.redis.RedisFactory;
import redisfx.tanh.rdm.redis.RedisFactorySingleton;
import redisfx.tanh.rdm.redis.client.RedisClient;
import redisfx.tanh.rdm.redis.RedisConfig;
import redisfx.tanh.rdm.redis.client.RedisConsole;
import redisfx.tanh.rdm.redis.RedisContext;

import java.util.List;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/19 10:23
 */
public class RedisFactorySingletonTest {

    private RedisConsole redisConsole;

    @Before
    public void before(){
        RedisFactory redisFactory= RedisFactorySingleton.getInstance();
        RedisConfig redisConfig =new RedisConfig();
        redisConfig.setHost("localhost");
        redisConfig.setPort(6379);
        RedisContext redisContext = redisFactory.createRedisContext(redisConfig);
        RedisClient redisClient=redisContext.newRedisClient();
        this.redisConsole=redisClient.getRedisConsole();
    }

    @Test
    public void ping(){
        List<String> result=redisConsole.sendCommand("ping");
        result.forEach(e-> System.out.println(e));
    }

}
