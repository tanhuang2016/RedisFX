package redisfx.tanh.rdm.redis.imp;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

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
        poolConfig.setMaxIdle(1);
        poolConfig.setTestOnBorrow(true);
        // 设置空闲连接检测周期和超时时间
        // 设置连接在池中最小空闲时间30秒
        poolConfig.setSoftMinEvictableIdleDuration(Duration.ofSeconds(30));
        // 检测连接空闲时间间隔10秒
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(10));
        POOL_CONFIG=poolConfig;
    }
}
