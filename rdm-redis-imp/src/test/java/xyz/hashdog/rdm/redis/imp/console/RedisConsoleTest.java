package xyz.hashdog.rdm.redis.imp.console;

import org.junit.Before;
import org.junit.Test;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.client.RedisConsole;
import xyz.hashdog.rdm.redis.RedisContext;
import xyz.hashdog.rdm.redis.imp.RedisFactory;

import java.util.List;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/19 9:26
 */
public class RedisConsoleTest {
    private RedisConsole redisConsole;

    @Before
    public void before(){
        xyz.hashdog.rdm.redis.RedisFactory redisFactory=new RedisFactory();
        RedisConfig redisConfig =new RedisConfig();
        redisConfig.setHost("localhost");
        redisConfig.setPort(6379);
        RedisContext redisContext = redisFactory.createRedisContext(redisConfig);
        RedisClient redisClient=redisContext.newRedisClient();
        this.redisConsole=redisClient.getRedisConsole();
    }

    @Test
    public void scan(){
        List<String> result=redisConsole.sendCommand("SCAN 0 MATCH A* COUNT 10");
        result.forEach(e-> System.out.println(e));
    }
    @Test
    public void ping(){
        List<String> result=redisConsole.sendCommand("ping");
        result.forEach(e-> System.out.println(e));
    }
    @Test
    public void keys(){
        List<String> result=redisConsole.sendCommand("keys *");
        result.forEach(e-> System.out.println(e));
    }
    @Test
    public void lrange(){
        List<String> result=redisConsole.sendCommand("lrange list 0 99");
        result.forEach(e-> System.out.println(e));
    }

    @Test
    public void MONITOR(){
        List<String> result=redisConsole.sendCommand("MONITOR");
        result.forEach(e-> System.out.println(e));
    }

}
