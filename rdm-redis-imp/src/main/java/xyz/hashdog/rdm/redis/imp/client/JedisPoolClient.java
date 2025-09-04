package xyz.hashdog.rdm.redis.imp.client;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.util.Pool;
import xyz.hashdog.rdm.common.tuple.Tuple2;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.common.util.TUtil;
import xyz.hashdog.rdm.redis.Message;
import xyz.hashdog.rdm.redis.client.*;
import xyz.hashdog.rdm.redis.exceptions.RedisException;
import xyz.hashdog.rdm.redis.imp.Util;
import xyz.hashdog.rdm.redis.imp.console.RedisConsole;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 单机版jedis,用JedisPool包装实现
 *
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 12:59
 */
public class JedisPoolClient extends AbstractRedisClient implements RedisClient {
    private static final Logger log = LoggerFactory.getLogger(JedisPoolClient.class);

    private final Jedis jedis;
    private final Pool<Jedis> jedisPool;
    public JedisPoolClient(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
        this.jedis = jedisPool.getResource();
    }

    private int db;


    /**
     * 这里通过message进行传输异常
     * 可以优化为统一异常处理,这个方法暂时保留
     * @return 测试连接结果
     */
    @Override
    public Message testConnect()  {
        Message message=new Message();
        try {
            jedis.ping();
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
     * @param execCommand 执行的命令
     * @return 执行结果
     * @param <R> 结果泛型
     */
    private  <R> R execute(Function<Jedis, R> execCommand) {
        try {
            return TUtil.execute(this.jedis,execCommand,Jedis::close);

        }catch (JedisException e){
            log.info("redis api exception",e);
            throw new RedisException(e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return execute(Jedis::isConnected);
    }

    /**
     * info Keyspace返回
     * db0:keys=6,expires=0,avg_ttl=0
     * db1:keys=1,expires=0,avg_ttl=0
     * 拆分获取
     * @return 0:DB[size]
     */
    @Override
    public Map<Integer, Integer> dbSize() {
        return execute(jedis->{
            String info = jedis.info("Keyspace");
            Map<Integer,Integer> map = new LinkedHashMap<>();
            for (int i = 0; i < 15; i++) {
                map.put(i,0);
            }
            String[] line = info.split("\r\n");
            for (String row : line) {
                if(!row.startsWith("db")){
                    continue;
                }
                Tuple2<Integer,Integer> tu=Util.keyspaceParseDb(row);
                map.put(tu.t1(),tu.t2());
            }
            return map;
        });
    }

    @Override
    public String select(int db) {
        String execute = execute(jedis -> jedis.select(db));
        this.db=db;
        return execute;
    }

    @Override
    public long hlen(String key) {
        return execute(jedis->jedis.hlen(key));
    }


    @Override
    public Map<byte[],byte[]> hscanAll(byte[] key) {
        return execute(jedis -> super.hscanAll(key,( cursor, scanParams) -> jedis.hscan(key, cursor, scanParams)));
    }

    @Override
    public Map<String,String> hscanAll(String key) {
        return execute(jedis -> super.hscanAll(key,( cursor, scanParams) -> jedis.hscan(key, cursor, scanParams)));
    }

    @Override
    public List<String> scanAll(String pattern) {
        return execute(jedis -> super.scanAll(pattern, jedis::scan));
    }

    /**
     * 封装key模糊查询
     * 带cursor的scan
     * @param pattern 匹配模式
     * @param cursors 游标
     * @param count 游标数量
     * @param type 匹配类型
     * @param isLike 是否模糊匹配
     * @return 匹配结果
     */
    @Override
    public Tuple2<List<String>, List<String>> scan(String pattern, List<String>cursors, int count, String type, boolean isLike) {
        return execute(jedis -> {
            if("-1".equals(cursors.getFirst())){
                return new Tuple2<>(cursors, new ArrayList<>());
            }
            Tuple2<String, List<String>> scan = super.scan(pattern, count, isLike, (scanParams) -> {
                if (DataUtil.isBlank(type)) {
                    return jedis.scan(cursors.getFirst(), scanParams);
                } else {
                    return jedis.scan(cursors.getFirst(), scanParams, type);
                }
            });
            return new Tuple2<>(List.of(scan.t1()), scan.t2());
        });


    }

    @Override
    public RedisKeyScanner getRedisKeyScanner() {
        RedisClient redisClient = this;
        return new RedisKeyScanner() {
            @Override
            public List<String> doScan() {
                Tuple2<List<String>, List<String>> scan = redisClient.scan(pattern, List.of(cursor), count, type, isLike);
                this.cursor = scan.t1().getFirst();
                return scan.t2();
            }
        };
    }

    @Override
    public List<String> sscanAll(String key) {
        return execute(jedis -> super.sscanAll(key, ( cursor, scanParams) -> jedis.sscan(key, cursor, scanParams)));
    }

    @Override
    public List<byte[]> sscanAll(byte[] key) {
        return execute(jedis -> super.sscanAll(key,  ( cursor, scanParams) -> jedis.sscan(key, cursor, scanParams)));
    }


    @Override
    public Set<String> keys(String pattern) {
        return execute(jedis->jedis.keys(pattern));
    }

    @Override
    public String type(String key) {
        return execute(jedis->jedis.type(key));
    }

    @Override
    public long ttl(String key) {
        return execute(jedis->jedis.ttl(key));
    }

    @Override
    public String ping() {
        return execute(Jedis::ping);

    }
    @Override
    public String info() {
        return execute(Jedis::info);

    }
    @Override
    public String rename(String oldKey, String newKey) {
        return execute(jedis->jedis.rename(oldKey,newKey));

    }
    @Override
    public long expire(String key, long seconds) {
        return execute(jedis->jedis.expire(key,seconds));

    }
    @Override
    public boolean exists(String key) {
        return execute(jedis->jedis.exists(key));
    }
    @Override
    public long del(String... key) {
        return execute(jedis->jedis.del(key));

    }
    @Override
    public long persist(String key) {
        return execute(jedis->jedis.persist(key));

    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue) {
        return execute(jedis->jedis.restore(key,ttl,serializedValue));
    }
    @Override
    public byte[] dump(String key) {
        return execute(jedis->jedis.dump(key));
    }
    @Override
    public String flushDB() {
        return execute(Jedis::flushDB);
    }


    @Override
    public String get(String key) {
        return execute(jedis->jedis.get(key));
    }

    @Override
    public String jsonGet(String key) {
        return execute(jedis -> {
            Connection connection = jedis.getConnection();
            CommandObjects commandObjects = new CommandObjects();
            JSONArray o = (JSONArray) connection.executeCommand(commandObjects.jsonGet(key, Path2.ROOT_PATH));
            return o.getJSONObject(0)
                    .toString();
        });
    }


    @Override
    public byte[] get(byte[] key) {
        return execute(jedis->jedis.get(key));
    }
    @Override
    public String set(String key,String value) {
        return execute(jedis->jedis.set(key,value));
    }
    @Override
    public String jsonSet(String key, String defaultJsonValue) {
       return execute(jedis->{
            Connection connection = jedis.getConnection();
            CommandObjects commandObjects = new CommandObjects();
           return connection.executeCommand(commandObjects.jsonSet(key, Path2.ROOT_PATH, defaultJsonValue));
        });

    }

    @Override
    public long xlen(String key) {
        return execute(jedis -> jedis.xlen(key));
    }

    @Override
    public long xdel(String key, String id) {
        return execute(jedis -> jedis.xdel(key,new StreamEntryID(id)));
    }

    @Override
    public String xadd(String key, String id, String jsonValue) {
        return execute(jedis->super.xadd(jedis,key,id,jsonValue));
    }

    @Override
    public Map<String, String> xrevrange(String key, String start, String end, int total) {
        return execute(jedis->super.xrevrange(jedis,key,start,end,total));
    }

    @Override
    public String set(byte[] key,byte[] value) {
        return execute(jedis->jedis.set(key,value));
    }

    @Override
    public long llen(String list) {
        return execute(jedis->jedis.llen(list));
    }

    @Override
    public long strlen(String key) {
        return execute(jedis->jedis.strlen(key));
    }
    @Override
    public Class<?> jsonType(String key) {
        return execute(jedis -> {
            Connection connection = jedis.getConnection();
            CommandObjects commandObjects = new CommandObjects();
            return connection.executeCommand(commandObjects.jsonType(key, Path2.ROOT_PATH)).getFirst();
        });
    }

    @Override
    public long jsonObjLen(String key) {
        return execute(jedis -> {
            Connection connection = jedis.getConnection();
            CommandObjects commandObjects = new CommandObjects();
            return connection.executeCommand(commandObjects.jsonObjLen(key, Path2.ROOT_PATH)).getFirst();
        });
    }

    @Override
    public long jsonStrLen(String key) {
        return execute(jedis -> {
            Connection connection = jedis.getConnection();
            CommandObjects commandObjects = new CommandObjects();
            return connection.executeCommand(commandObjects.jsonStrLen(key, Path2.ROOT_PATH)).getFirst();
        });
    }

    @Override
    public long jsonArrLen(String key) {
        return execute(jedis -> {
            Connection connection = jedis.getConnection();
            CommandObjects commandObjects = new CommandObjects();
            return connection.executeCommand(commandObjects.jsonArrLen(key, Path2.ROOT_PATH)).getFirst();
        });
    }

    @Override
    public List<String> lrange(String list, int start, int stop) {
        return execute(jedis->jedis.lrange(list,start,stop));
    }

    @Override
    public List<byte[]> lrange(byte[] list, int start, int stop) {
        return execute(jedis->jedis.lrange(list,start,stop));
    }



    @Override
    public String lset(byte[] list, int i, byte[] value) {
        return execute(jedis->jedis.lset(list,i,value));
    }

    @Override
    public String lset(String list, int i, String value) {
        return execute(jedis->jedis.lset(list,i,value));
    }

    @Override
    public long lrem(byte[] list, int i, byte[] value) {
        return execute(jedis->jedis.lrem(list,i,value));
    }

    @Override
    public long lrem(String list, int i, String value) {
        return execute(jedis->jedis.lrem(list,i,value));
    }

    @Override
    public String lpop(String list) {
        return execute(jedis->jedis.lpop(list));
    }

    @Override
    public String rpop(String list) {
        return execute(jedis->jedis.rpop(list));
    }

    @Override
    public long lpush(String list, String value) {
        return execute(jedis->jedis.lpush(list,value));
    }

    @Override
    public long lpush(byte[] list, byte[] value) {
        return execute(jedis->jedis.lpush(list,value));
    }

    @Override
    public long rpush(String list, String value) {
        return execute(jedis->jedis.rpush(list,value));
    }

    @Override
    public long rpush(byte[] list, byte[] value) {
        return execute(jedis->jedis.rpush(list,value));
    }

    @Override
    public long hset(String key, String field, String value) {
        return execute(jedis->jedis.hset(key,field,value));
    }
    @Override
    public long hset(byte[] key, byte[] field, byte[] value) {
        return execute(jedis->jedis.hset(key,field,value));
    }

    @Override
    public long hsetnx(String key, String field, String value) {
        return execute(jedis->jedis.hsetnx(key,field,value));
    }

    @Override
    public long hsetnx(byte[] key, byte[] field, byte[] value) {
        return execute(jedis->jedis.hsetnx(key,field,value));
    }

    @Override
    public long hdel(byte[] key, byte[] field) {
        return execute(jedis->jedis.hdel(key,field));
    }
    @Override
    public long hdel(String key, String field) {
        return execute(jedis->jedis.hdel(key,field));
    }

    @Override
    public long scard(String key) {
        return execute(jedis->jedis.scard(key));
    }

    @Override
    public long srem(String key,String value) {
        return execute(jedis->jedis.srem(key,value));
    }
    @Override
    public long srem(byte[] key,byte[] value) {
        return execute(jedis->jedis.srem(key,value));
    }

    @Override
    public long sadd(String key,String value) {
        return execute(jedis->jedis.sadd(key,value));
    }
    @Override
    public long sadd(byte[] key,byte[] value) {
        return execute(jedis->jedis.sadd(key,value));
    }
    @Override
    public long zadd(byte[] key, double score, byte[] value) {
        return execute(jedis->jedis.zadd(key, score,value));
    }

    @Override
    public long zadd(String key, double score, String value) {
        return execute(jedis->jedis.zadd(key, score,value));
    }
    @Override
    public long zrem(String key,String value) {
        return execute(jedis->jedis.zrem(key,value));
    }
    @Override
    public long zrem(byte[] key,byte[] value) {
        return execute(jedis->jedis.zrem(key,value));
    }

    @Override
    public long zcard(byte[] key) {
        return execute(jedis->jedis.zcard(key));
    }
    @Override
    public long zcard(String key) {
        return execute(jedis->jedis.zcard(key));
    }

    @Override
    public void monitor(RedisMonitor redisMonitor) {
        Jedis newJedis = jedisPool.getResource();
        //连接关掉，并且回收到资源池
        redisMonitor.addJedis(newJedis.getConnection());
        redisMonitor.addJedis(newJedis);
        doMonitor(newJedis,redisMonitor);
    }



    @Override
    public void psubscribe(RedisPubSub redisPubSub, String text) {
        //订阅模式有命令限制，得单独拿一个连接来操作
        jedisPool.getResource().psubscribe(new JedisPubSub() {
            @Override
            public void onPMessage(String pattern, String channel, String message) {
                redisPubSub.onMessage(channel,message);
            }
        },text);
    }

    @Override
    public RedisSubscriber subscriber(){
        return subscriber(jedisPool::getResource, text->jedis.sendCommand(Protocol.Command.PUNSUBSCRIBE, text));
    }



    @Override
    public long publish(String channel, String message) {
        return execute(jedis->jedis.publish(channel,message));
    }

    @Override
    public long memoryUsage(String key, int samples) {
        Pipeline pipeline = jedis.pipelined();
        return execute(jedis->jedis.memoryUsage(key,samples));
    }

    @Override
    public Map<Double,String> zrangeWithScores(String key,long start, long stop) {
        return execute(jedis->super.zrangeWithScores(jedis,key,start,stop));
    }

    @Override
    public Map<Double,byte[]> zrangeWithScores(byte[] key,long start, long stop) {
        return execute(jedis->super.zrangeWithScores(jedis,key,start,stop));
    }

    @Override
    public List<Object> executePipelined(Consumer<PipelineAdapter> pipelineExecutor) {
        return execute(jedis -> {
            Pipeline pipeline = jedis.pipelined();
            PipeLineAdapterImpl pipeLineAdapter = new PipeLineAdapterImpl(pipeline);
            pipelineExecutor.accept(pipeLineAdapter);
            return pipeLineAdapter.syncAndReturnAll();
        });
    }





    /**
     * 传了一个SocketAcquirer匿名内部类实现
     * SocketAcquirer 每次都是从pool获取最新的socket
     * 但是使用socket后没关流,如果有必要可以用warp包装socket多传1个回调函数,
     * 进行cmd调用完之后关流
     * @return 控制台对象
     */
    @Override
    public RedisConsole getRedisConsole() {
        return new RedisConsole(() -> TUtil.getField(jedis.getConnection(), "socket"));
    }

    @Override
    public void close()  {
        //jedisPool在上下文中关闭
        Util.close(jedis);
    }
}
