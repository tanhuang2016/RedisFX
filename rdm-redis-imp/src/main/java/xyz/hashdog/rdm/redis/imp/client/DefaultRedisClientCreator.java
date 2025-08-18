package xyz.hashdog.rdm.redis.imp.client;

import com.jcraft.jsch.Session;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.redis.imp.Constant;
import xyz.hashdog.rdm.redis.imp.Util;

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
    /**
     * 根据RedisConfig 判断创建什么类型的redis客户端
     * @param redisConfig
     * @return
     */
    @Override
    public RedisClient create(RedisConfig redisConfig) {
        if(redisConfig.isSentinel()){
            Set<String> sentinels = new HashSet<>();
            sentinels.add(redisConfig.getHost()+":"+redisConfig.getPort());
            jedisSentinelPool = new JedisSentinelPool(redisConfig.getMasterName(), sentinels,Constant.POOL_CONFIG,redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(),DataUtil.ifEmpty(redisConfig.getAuth(),null),0);
            return new JedisSentinelPoolClient(jedisSentinelPool);
        }
        if (redisConfig.isCluster()) {
            Set<HostAndPort> nodes = new HashSet<>();
            nodes.add(new HostAndPort(redisConfig.getHost(), redisConfig.getPort()));
            jedisCluster = new JedisCluster(nodes,redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(),3,DataUtil.ifEmpty(redisConfig.getAuth(),null),Constant.POOL_CONFIG);
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
            SSLSocketFactory SSLSocketFactory = Util.getSocketFactory(redisConfig.getCaCrt(), redisConfig.getRedisCrt(), redisConfig.getRedisKey(), redisConfig.getRedisKeyPassword());
            this.jedisPool=new JedisPool(Constant.POOL_CONFIG, host, port,redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(),DataUtil.ifEmpty(redisConfig.getAuth(),null),0,null,true,SSLSocketFactory,null,null);
            return new JedisPoolClient(jedisPool,tunnel);
        }
        this.jedisPool=new JedisPool(Constant.POOL_CONFIG, host, port,redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(), DataUtil.ifEmpty(redisConfig.getAuth(),null),0,null);
        return new JedisPoolClient(jedisPool,tunnel);
    }

    @Override
    public void close()  {
        Util.close(jedisPool,jedisSentinelPool,jedisCluster);
        if(tunnel!=null){
            tunnel.disconnect();
        }
    }
}
