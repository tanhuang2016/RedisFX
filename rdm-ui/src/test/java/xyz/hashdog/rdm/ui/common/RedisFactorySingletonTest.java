package xyz.hashdog.rdm.ui.common;

import org.junit.Before;
import org.junit.Test;
import xyz.hashdog.rdm.redis.RedisFactorySingleton;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.client.RedisConsole;
import xyz.hashdog.rdm.redis.RedisContext;

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
        xyz.hashdog.rdm.redis.RedisFactory redisFactory= RedisFactorySingleton.getInstance();
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
