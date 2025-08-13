package xyz.hashdog.rdm.redis.imp.client;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.Tuple;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.common.util.TUtil;
import xyz.hashdog.rdm.redis.Message;
import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.redis.client.RedisMonitor;
import xyz.hashdog.rdm.redis.client.RedisPubSub;
import xyz.hashdog.rdm.redis.exceptions.RedisException;
import xyz.hashdog.rdm.redis.imp.Util;
import xyz.hashdog.rdm.redis.imp.console.RedisConsole;

import java.util.*;
import java.util.function.Function;

/**
 * jidis集群客户端实现
 *
 * @author th
 * @version 1.0.1
 * @since 2025/6/08 12:59
 */
public class JedisClusterClient implements RedisClient {
    protected static Logger log = LoggerFactory.getLogger(JedisClusterClient.class);


    private final JedisCluster jedis;
    private RedisConfig redisConfig;

    private final List<String> masters;



    private int db;

    public JedisClusterClient(JedisCluster jedisCluster, RedisConfig redisConfig) {
        this.jedis = jedisCluster;
        this.redisConfig = redisConfig;
        byte[] nodes = (byte[])jedisCluster.sendCommand(Protocol.Command.CLUSTER, Protocol.ClusterKeyword.NODES.toString());
        this.masters = parseMasterNodes(new String(nodes));
    }

    private static List<String> parseMasterNodes(String clusterNodesOutput) {
        List<String> masters = new ArrayList<>();
        String[] lines = clusterNodesOutput.split("\n");

        for (String line : lines) {
            String[] parts = line.split("\\s+");
            if (parts.length >= 3 && parts[2].contains("master")) {
                String[] addrParts = parts[1].split(":");
                String ip = addrParts[0];
                String port = addrParts[1].split("@")[0];
                masters.add(ip + ":" + port);
            }
        }
        return masters;
    }

    /**
     * 这里通过message进行传输异常
     * 可以优化为统一异常处理,这个方法暂时保留 FIXME
     * @return
     */
    @Override
    public Message testConnect()  {
        Message message=new Message();
        try {
            this.ping();
            message.setSuccess(true);
        }catch (JedisConnectionException e) {
            message.setSuccess(false);
            message.setMessage(e.getMessage());
        }
        return message;
    }

    @Override
    public int getDb() {
        return db;
    }

    /**
     * 执行命令的封装
     * 统一命令的异常转换
     * @param execCommand
     * @return
     * @param <R>
     */
    private  <R> R execut( Function<JedisCluster, R> execCommand) {
        try {
            return TUtil.execut(this.jedis,execCommand,JedisCluster::close);

        }catch (JedisException e){
            log.info("redis api exception",e);
            throw new RedisException(e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return jedis.getClusterNodes().values().stream().findFirst().get().getResource().isConnected();
    }

    /**
     * info Keyspace返回
     * db0:keys=6,expires=0,avg_ttl=0
     * db1:keys=1,expires=0,avg_ttl=0
     * 拆分获取
     * @return
     */
    @Override
    public Map<Integer, String> dbSize() {
        return execut(jedis->{
            Map<Integer,String> map = new LinkedHashMap<>();
            long dbsize=0;
            for (String master : masters) {
                Connection connection = this.jedis.getClusterNodes().get(master).getResource();
                dbsize += (long)connection.executeCommand(Protocol.Command.DBSIZE);
            }
            map.put(0,"DB0"+String.format("[%d]",dbsize));
            return map;
        });
    }

    @Override
    public String select(int db) {
        String execut =jedis.getClusterNodes().values().stream().findFirst().get().getResource().select(db);
        this.db=db;
        return execut;
    }

    @Override
    public long hlen(String key) {
        return execut(jedis->jedis.hlen(key));
    }


    @Override
    public Map<byte[],byte[]> hscanAll(byte[] key) {
        return execut(jedis -> {
            Map<byte[],byte[]> map = new LinkedHashMap<>();
            // 定义SCAN命令参数，匹配所有键
            ScanParams scanParams = new ScanParams();
            scanParams.count(5000);
            // 开始SCAN迭代
            String cursor = "0";
            do {
                ScanResult<Map.Entry<byte[],byte[]>> scanResult = jedis.hscan(key, cursor.getBytes(), scanParams);
                for (Map.Entry<byte[],byte[]> entry : scanResult.getResult()) {
                    map.put(entry.getKey(),entry.getValue());
                }
                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));
            return map;
        });
    }

    @Override
    public Map<String,String> hscanAll(String key) {
        return execut(jedis -> {
            Map<String,String> map = new LinkedHashMap<>();
            // 定义SCAN命令参数，匹配所有键
            ScanParams scanParams = new ScanParams();
            scanParams.count(5000);
            // 开始SCAN迭代
            String cursor = "0";
            do {
                ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(key, cursor, scanParams);
                for (Map.Entry<String, String> entry : scanResult.getResult()) {
                    map.put(entry.getKey(),entry.getValue());
                }
                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));
            return map;
        });
    }

    @Override
    public List<String> scanAll(String pattern) {
        List<String> all = new ArrayList<>();
        CommandObjects commandObjects = new CommandObjects();
        for (String master : masters) {
            Connection connection = jedis.getClusterNodes().get(master).getResource();
            List<String> execut = execut(jedis -> {
                List<String> keys = new ArrayList<>();
                // 定义SCAN命令参数，匹配所有键
                ScanParams scanParams = new ScanParams();
                scanParams.count(5000);
                if (DataUtil.isNotBlank(pattern)) {
                    scanParams.match(pattern);
                }
                // 开始SCAN迭代
                String cursor = "0";
                do {
                    ScanResult<String> scanResult = connection.executeCommand(commandObjects.scan(cursor, scanParams));
//                    ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                    for (String key : scanResult.getResult()) {
                        keys.add(key);
                    }
                    cursor = scanResult.getCursor();
                } while (!"0".equals(cursor));
                return keys;
            });
            all.addAll(execut);
        }
        return all;
    }

    @Override
    public List<String> sscanAll(String key) {
        return execut(jedis -> {
            List<String> ress = new ArrayList<>();
            // 定义SSCAN命令参数，匹配所有键
            ScanParams scanParams = new ScanParams();
            scanParams.count(5000);
            // 开始SCAN迭代
            String cursor = "0";
            do {
                ScanResult<String> scanResult = jedis.sscan(key,cursor, scanParams);
                for (String res : scanResult.getResult()) {
                    ress.add(res);
                }
                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));
            return ress;
        });
    }

    @Override
    public List<byte[]> sscanAll(byte[] key) {
        return execut(jedis -> {
            List<byte[]> ress = new ArrayList<>();
            // 定义SSCAN命令参数，匹配所有键
            ScanParams scanParams = new ScanParams();
            scanParams.count(5000);
            // 开始SCAN迭代
            String cursor = "0";
            do {
                ScanResult<byte[]> scanResult = jedis.sscan(key,cursor.getBytes(), scanParams);
                for (byte[] res : scanResult.getResult()) {
                    ress.add(res);
                }
                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));
            return ress;
        });
    }


    @Override
    public Set<String> keys(String pattern) {
        return execut(jedis->jedis.keys(pattern));
    }

    @Override
    public String type(String key) {
        return execut(jedis->jedis.type(key));
    }

    @Override
    public long ttl(String key) {
        return execut(jedis->jedis.ttl(key));
    }

    @Override
    public String ping() {
//        return execut(UnifiedJedis::ping);
        boolean ping = jedis.getClusterNodes().values().stream().findFirst().get().getResource().ping();
        return ping?"PONG":"PONG FAIL";

    }
    @Override
    public String info() {
        return execut(jedis->jedis.info());

    }
    @Override
    public String rename(String oldkey, String newkey) {
        return execut(jedis->jedis.rename(oldkey,newkey));

    }
    @Override
    public long expire(String key, long seconds) {
        return execut(jedis->jedis.expire(key,seconds));

    }
    @Override
    public boolean exists(String key) {
        return execut(jedis->jedis.exists(key));
    }
    @Override
    public long del(String... key) {
        return execut(jedis->jedis.del(key));

    }
    @Override
    public long persist(String key) {
        return execut(jedis->jedis.persist(key));

    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue) {
        return execut(jedis->jedis.restore(key,ttl,serializedValue));
    }
    @Override
    public byte[] dump(String key) {
        return execut(jedis->jedis.dump(key));
    }
    @Override
    public String flushDB() {
        return execut(jedis->jedis.flushDB());
    }


    @Override
    public String get(String key) {
        return execut(jedis->jedis.get(key));
    }
    @Override
    public byte[] get(byte[] key) {
        return execut(jedis->jedis.get(key));
    }
    @Override
    public String set(String key,String value) {
        return execut(jedis->jedis.set(key,value));
    }
    @Override
    public String set(byte[] key,byte[] value) {
        return execut(jedis->jedis.set(key,value));
    }

    @Override
    public String jsonGet(String key) {
        return execut(jedis -> {
            JSONArray o = (JSONArray) jedis.jsonGet(key, Path2.ROOT_PATH);
            return o.getJSONObject(0)
                    .toString();
        });
    }

    @Override
    public String jsonSet(String key, String defualtJsonValue) {
        return execut(jedis->{
            return jedis.jsonSet(key, Path2.ROOT_PATH, defualtJsonValue);
        });
    }

    @Override
    public String xadd(String key, String id, String jsonValue) {
        return execut(jedis->{
            Map<String, String> map = Util.json2MapString(jsonValue);
            StreamEntryID streamEntryID;
            if(StreamEntryID.NEW_ENTRY.toString().equals(id)){
                streamEntryID = StreamEntryID.NEW_ENTRY;
            }else {
                streamEntryID = new StreamEntryID(id);
            }
            return   jedis.xadd(key, streamEntryID , map).toString();
        });
    }

    @Override
    public long xlen(String key) {
        return execut(jedis -> jedis.xlen(key));
    }

    @Override
    public Map<String, String> xrevrange(String key, String start, String end, int total) {
        return execut(jedis->{
            Map<String,String> map = new LinkedHashMap<>();
            for (StreamEntry streamEntry : jedis.xrevrange(key, start, end, total)) {
                Map<String, String> fields = streamEntry.getFields();
                String jsonValue =Util.obj2Json(fields);
                map.put(streamEntry.getID().toString(),jsonValue);
            }
            return map;
        });
    }

    @Override
    public long xdel(String key, String id) {
        return execut(jedis -> jedis.xdel(key,new StreamEntryID(id)));
    }

    @Override
    public long llen(String list) {
        return execut(jedis->jedis.llen(list));
    }
    @Override
    public long strlen(String key) {
        return execut(jedis->jedis.strlen(key));
    }

    @Override
    public Class<?> jsonType(String key) {
        return execut(jedis->jedis.jsonType(key,Path2.ROOT_PATH).getFirst());
    }

    @Override
    public long jsonObjLen(String key) {
        return execut(jedis->jedis.jsonObjLen(key,Path2.ROOT_PATH).getFirst());
    }

    @Override
    public long jsonStrLen(String key) {
        return execut(jedis->jedis.jsonStrLen(key,Path2.ROOT_PATH).getFirst());
    }

    @Override
    public long jsonArrLen(String key) {
        return execut(jedis->jedis.jsonArrLen(key,Path2.ROOT_PATH).getFirst());
    }

    @Override
    public List<String> lrange(String list, int start, int stop) {
        return execut(jedis->jedis.lrange(list,start,stop));
    }

    @Override
    public List<byte[]> lrange(byte[] list, int start, int stop) {
        return execut(jedis->jedis.lrange(list,start,stop));
    }



    @Override
    public String lset(byte[] list, int i, byte[] value) {
        return execut(jedis->jedis.lset(list,i,value));
    }

    @Override
    public String lset(String list, int i, String value) {
        return execut(jedis->jedis.lset(list,i,value));
    }

    @Override
    public long lrem(byte[] list, int i, byte[] value) {
        return execut(jedis->jedis.lrem(list,i,value));
    }

    @Override
    public long lrem(String list, int i, String value) {
        return execut(jedis->jedis.lrem(list,i,value));
    }

    @Override
    public String lpop(String list) {
        return execut(jedis->jedis.lpop(list));
    }

    @Override
    public String rpop(String list) {
        return execut(jedis->jedis.rpop(list));
    }

    @Override
    public long lpush(String list, String value) {
        return execut(jedis->jedis.lpush(list,value));
    }

    @Override
    public long lpush(byte[] list, byte[] value) {
        return execut(jedis->jedis.lpush(list,value));
    }

    @Override
    public long rpush(String list, String value) {
        return execut(jedis->jedis.rpush(list,value));
    }

    @Override
    public long rpush(byte[] list, byte[] value) {
        return execut(jedis->jedis.rpush(list,value));
    }

    @Override
    public long hset(String key, String field, String value) {
        return execut(jedis->jedis.hset(key,field,value));
    }
    @Override
    public long hset(byte[] key, byte[] field, byte[] value) {
        return execut(jedis->jedis.hset(key,field,value));
    }

    @Override
    public long hsetnx(String key, String field, String value) {
        return execut(jedis->jedis.hsetnx(key,field,value));
    }

    @Override
    public long hsetnx(byte[] key, byte[] field, byte[] value) {
        return execut(jedis->jedis.hsetnx(key,field,value));
    }

    @Override
    public long hdel(byte[] key, byte[] field) {
        return execut(jedis->jedis.hdel(key,field));
    }
    @Override
    public long hdel(String key, String field) {
        return execut(jedis->jedis.hdel(key,field));
    }

    @Override
    public long scard(String key) {
        return execut(jedis->jedis.scard(key));
    }

    @Override
    public long srem(String key,String value) {
        return execut(jedis->jedis.srem(key,value));
    }
    @Override
    public long srem(byte[] key,byte[] value) {
        return execut(jedis->jedis.srem(key,value));
    }

    @Override
    public long sadd(String key,String value) {
        return execut(jedis->jedis.sadd(key,value));
    }
    @Override
    public long sadd(byte[] key,byte[] value) {
        return execut(jedis->jedis.sadd(key,value));
    }
    @Override
    public long zadd(byte[] key,double scorem,byte[] value) {
        return execut(jedis->jedis.zadd(key,scorem,value));
    }

    @Override
    public long zadd(String key,double scorem,String value) {
        return execut(jedis->jedis.zadd(key,scorem,value));
    }
    @Override
    public long zrem(String key,String value) {
        return execut(jedis->jedis.zrem(key,value));
    }
    @Override
    public long zrem(byte[] key,byte[] value) {
        return execut(jedis->jedis.zrem(key,value));
    }

    @Override
    public long zcard(byte[] key) {
        return execut(jedis->jedis.zcard(key));
    }
    @Override
    public long zcard(String key) {
        return execut(jedis->jedis.zcard(key));
    }

    @Override
    public void monitor(RedisMonitor redisMonitor) {
        jedis.getClusterNodes().forEach((nodeStr,pool)->{
            try {
                String[] addr = nodeStr.split(":");
                Jedis jedis = new Jedis(addr[0], Integer.parseInt(addr[1]));
//                boolean connected = jedis.isConnected();
                jedis.auth(redisConfig.getAuth());
                Thread thread = new Thread(() -> {
                    jedis.monitor(new JedisMonitor() {
                        @Override
                        public void onCommand(String s) {
//                            String log = String.format("[Node %s %s] %s",addr[0],addr[1],s);
//                            System.out.println(log);
                            redisMonitor.onCommand(s);
                        }
                    });
                });
                thread.setDaemon(true);
                thread.start();
                System.out.println(nodeStr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }

    @Override
    public void psubscribe(RedisPubSub redisPubSub, String text) {
        jedis.psubscribe(new JedisPubSub() {
            @Override
            public void onPMessage(String pattern, String channel, String message) {
                redisPubSub.onMessage(channel, message);
            }
        },text);
    }
    @Override
    public long publish(String channel, String message) {
        return execut(jedis->jedis.publish(channel,message));
    }
    @Override
    public long memoryUsage(String key, int samples) {
        return execut(jedis->jedis.memoryUsage(key,samples));
    }
    @Override
    public Map<Double,String> zrangeWithScores(String key,long start, long stop) {
        return execut(jedis->{
            List<Tuple> tuples = jedis.zrangeWithScores(key, start, stop);
            Map<Double,String> map = new LinkedHashMap<>();
            tuples.forEach(e->map.put(e.getScore(),e.getElement()));
            return map;
        });
    }

    @Override
    public Map<Double,byte[]> zrangeWithScores(byte[] key,long start, long stop) {
        return execut(jedis->{
            List<Tuple> tuples = jedis.zrangeWithScores(key, start, stop);
            Map<Double,byte[]> map = new LinkedHashMap<>();
            tuples.forEach(e->map.put(e.getScore(),e.getBinaryElement()));
            return map;
        });
    }

    /**
     * 传了一个SocketAcquirer匿名内部类实现
     * SocketAcquirer 每次都是从pool获取最新的socket
     * 但是使用socket后没关流,如果有必要可以用warp包装socket多传1个回调函数,
     * 进行cmd调用完之后关流
     * @return
     */
    @Override
    public RedisConsole getRedisConsole() {
        return new RedisConsole(() -> {
            return TUtil.getField(jedis.getClusterNodes().values().stream().findFirst().get().getResource(), "socket");
        });
    }

    @Override
    public void close()  {
        if(this.jedis!=null){
            this.jedis.close();
        }
    }
}
