package xyz.hashdog.rdm.redis.imp;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 13:12
 */
public class Constant {

    /**
     * jedis通用连接池配置
     */
    public static final  GenericObjectPoolConfig<?> POOL_CONFIG ;
    static {
        // 创建Jedis连接池配置对象
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(2);
        poolConfig.setTestOnBorrow(true);
        POOL_CONFIG=poolConfig;
    }
}
