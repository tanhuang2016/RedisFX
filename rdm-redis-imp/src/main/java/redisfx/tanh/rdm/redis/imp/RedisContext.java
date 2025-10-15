package redisfx.tanh.rdm.redis.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisException;
import redisfx.tanh.rdm.redis.RedisConfig;
import redisfx.tanh.rdm.redis.client.RedisClient;
import redisfx.tanh.rdm.redis.exceptions.RedisException;
import redisfx.tanh.rdm.redis.imp.client.DefaultRedisClientCreator;
import redisfx.tanh.rdm.redis.imp.client.RedisClientCreator;
import redisfx.tanh.rdm.redis.imp.util.Util;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 10:53
 */
public class RedisContext implements redisfx.tanh.rdm.redis.RedisContext {
    private static final Logger log = LoggerFactory.getLogger(RedisContext.class);
    /**
     * redis配置
     */
    private final RedisConfig redisConfig;
    /**
     * redis客户创建器
     */
    private RedisClientCreator redisClientCreator;
    private RedisClient useRedisClient;




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

    @Override
    public RedisClient useRedisClient() {
        if(useRedisClient==null){
            return useRedisClient=newRedisClient();
        }
        return useRedisClient;
    }

    /**
     * 创建redis客户端
     * @return redis客户端
     */
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
        Util.close(useRedisClient,redisClientCreator);
    }
}
