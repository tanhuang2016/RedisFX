package redisfx.tanh.rdm.redis.imp.client;

import redis.clients.jedis.JedisSentinelPool;

/**
 * Sentinel模式客户端
 *
 * @author th
 * @version 1.0.2
 * @since 2025/6/15 12:59
 */
public class JedisSentinelPoolClient extends JedisPoolClient {
    public JedisSentinelPoolClient(JedisSentinelPool jedisSentinelPool) {
        super(jedisSentinelPool);
    }
}
