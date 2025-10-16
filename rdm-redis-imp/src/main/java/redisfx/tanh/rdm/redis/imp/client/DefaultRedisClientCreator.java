package redisfx.tanh.rdm.redis.imp.client;

import com.jcraft.jsch.Session;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;
import redisfx.tanh.rdm.common.util.DataUtil;
import redisfx.tanh.rdm.redis.RedisConfig;
import redisfx.tanh.rdm.redis.client.RedisClient;
import redisfx.tanh.rdm.redis.imp.Constant;
import redisfx.tanh.rdm.redis.imp.util.Util;

import javax.net.ssl.SSLSocketFactory;
import java.util.HashSet;
import java.util.Set;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 12:47
 */
public class DefaultRedisClientCreator implements RedisClientCreator{

    /**
     * jedis
     */
    private JedisPool jedisPool;
    private JedisSentinelPool jedisSentinelPool;
    private JedisCluster jedisCluster;
    private Session tunnel;
    private final RedisConfig redisConfig;

    public DefaultRedisClientCreator(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;
    }

    /**
     * 根据RedisConfig 判断创建什么类型的redis客户端
     * @return redis客户端
     */
    @Override
    public RedisClient create() {
        SSLSocketFactory sslSocketFactory=null;
        if(redisConfig.isSsl()){
            sslSocketFactory = Util.getSocketFactory(redisConfig.getCaCrt(), redisConfig.getRedisCrt(), redisConfig.getRedisKey(), redisConfig.getRedisKeyPassword());
        }
        if(redisConfig.isSentinel()){
            Set<String> sentinels = new HashSet<>();
            sentinels.add(redisConfig.getHost()+":"+redisConfig.getPort());
            jedisSentinelPool = new JedisSentinelPool(redisConfig.getMasterName(), sentinels,defaultPoolConfig(),redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(),DataUtil.ifEmpty(redisConfig.getAuth(),null),0);
            return new JedisSentinelPoolClient(jedisSentinelPool);
        }
        if (redisConfig.isCluster()) {
            Set<HostAndPort> nodes = new HashSet<>();
            nodes.add(new HostAndPort(redisConfig.getHost(), redisConfig.getPort()));
            if (redisConfig.isSsl()) {
                JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                        .connectionTimeoutMillis(redisConfig.getConnectionTimeout())
                        .socketTimeoutMillis(redisConfig.getSoTimeout())
                        .password(DataUtil.ifEmpty(redisConfig.getAuth(), null))
                        .ssl(true)
                        .sslSocketFactory(sslSocketFactory)
                        .build();

                jedisCluster = new JedisCluster(nodes, clientConfig, defaultPoolConfig());
            }else {
                jedisCluster = new JedisCluster(nodes,redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(),3,DataUtil.ifEmpty(redisConfig.getAuth(),null),defaultPoolConfig());
            }
            return new JedisClusterClient(jedisCluster,redisConfig);
        }
        int port = redisConfig.getPort();
        String host = redisConfig.getHost();
        if(redisConfig.isSsh()){
            tunnel = Util.createTunnel(redisConfig.getSshUserName(), redisConfig.getSshHost(), redisConfig.getSshPort(), redisConfig.getSshPassword(), redisConfig.getSshPrivateKey(), redisConfig.getSshPassphrase());
            port = Util.portForwardingL(tunnel, redisConfig.getHost(), redisConfig.getPort());
            host="127.0.0.1";
        }
        if(redisConfig.isSsl()){
            this.jedisPool=new JedisPool(defaultPoolConfig(), host, port,redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(),DataUtil.ifEmpty(redisConfig.getAuth(),null),0,null,true,sslSocketFactory,null,null);
            return new JedisPoolClient(jedisPool);
        }
        this.jedisPool=new JedisPool(defaultPoolConfig(), host, port,redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(), DataUtil.ifEmpty(redisConfig.getAuth(),null),0,null);
        return new JedisPoolClient(jedisPool);
    }

    @Override
    public RedisClient newRedisClient() {
        if(redisConfig.isSentinel()){
            return new JedisSentinelPoolClient(jedisSentinelPool);
        }
        if (redisConfig.isCluster()) {
            return new JedisClusterClient(jedisCluster,redisConfig);
        }
        return new JedisPoolClient(jedisPool);
    }

    /**
     * 获取默认的连接池配置
     * @return 默认的连接池配置
     */
    @SuppressWarnings("unchecked")
    private static <T> GenericObjectPoolConfig<T> defaultPoolConfig() {
        return (GenericObjectPoolConfig<T>) Constant.POOL_CONFIG;
    }

    /**
     * 关闭redis客户端
     */
    @Override
    public void close()  {
        Util.close(jedisPool,jedisSentinelPool,jedisCluster);
        if(tunnel!=null){
            tunnel.disconnect();
        }
    }
}
