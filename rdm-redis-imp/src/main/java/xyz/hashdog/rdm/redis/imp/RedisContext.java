package xyz.hashdog.rdm.redis.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisException;
import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.redis.exceptions.RedisException;
import xyz.hashdog.rdm.redis.imp.client.DefaultRedisClientCreator;
import xyz.hashdog.rdm.redis.imp.client.RedisClientCreator;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 10:53
 */
public class RedisContext implements xyz.hashdog.rdm.redis.RedisContext{
    private static final Logger log = LoggerFactory.getLogger(RedisContext.class);
    /**
     * redis配置
     */
    private final RedisConfig redisConfig;
    /**
     * redis客户创建器
     */
    private RedisClientCreator redisClientCreator;




    public RedisContext(RedisConfig redisConfig) {
        this.redisConfig=redisConfig;
    }

    /**
     * 委派给 RedisClientCreator 进行redis客户端的创建
     * @return redis客户端
     */
    @Override
    public RedisClient newRedisClient() {
        if(redisClientCreator==null){
            return createRedisClient();
        }
        return redisClientCreator.newRedisClient();
    }

    private RedisClient createRedisClient() {
        try {
            this.redisClientCreator=new DefaultRedisClientCreator(redisConfig);
            return redisClientCreator.create();
        }catch (JedisException e){
            log.error("create redis client exception",e);
            throw new RedisException(e.getMessage());
        }
    }

    @Override
    public RedisConfig getRedisConfig() {
        return redisConfig;
    }



    /**
     * 获取创建器,可以进行创建多个客户端实例
     * @return 创建器
     */
    public RedisClientCreator getRedisClientCreator() {
        return redisClientCreator;
    }

    /**
     * 设置创建器,可以自定义创建器
     * @param redisClientCreator 创建器
     */
    public void setRedisClientCreator(RedisClientCreator redisClientCreator) {
        this.redisClientCreator = redisClientCreator;
    }

    @Override
    public void close()  {
        redisClientCreator.close();
    }
}
