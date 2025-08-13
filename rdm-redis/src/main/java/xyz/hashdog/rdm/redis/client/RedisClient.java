package xyz.hashdog.rdm.redis.client;

import xyz.hashdog.rdm.redis.Message;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis客户端操作
 * 相关redis操作命令都在这儿
 *
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 11:03
 */
public interface RedisClient extends Closeable {


    /**
     * 测试连接
     * @return
     */
    Message testConnect();
    /**
     * 判断是否连接
     *
     * @return
     */
    boolean isConnected();

    /**
     * keys 模糊查新的命令
     *
     * @param pattern
     * @return
     */
    Set<String> keys(String pattern);

    /**
     * 获取key的类型
     *
     * @param key
     * @return
     */
    String type(String key);

    /**
     * key的存活时间是多少秒
     *
     * @param key
     * @return
     */
    long ttl(String key);

    /**
     * ping
     *
     * @return
     */
    String ping();

    /**
     * redis信息
     *
     * @return
     */
    String info();

    /**
     * key 重命名
     *
     * @param oldkey
     * @param newkey
     * @return
     */
    String rename(String oldkey, String newkey);

    /**
     * 控制台交互器
     *
     * @return
     */
    RedisConsole getRedisConsole();

    /**
     * 设置key的时长
     *
     * @param key
     * @param seconds
     * @return
     */
    long expire(String key, long seconds);

    /**
     * key是否存在
     *
     * @param key
     * @return
     */
    boolean exists(String key);

    /**
     * 删除key
     *
     * @param key
     * @return
     */
    long del(String... key);

    /**
     * 设置key永不过期
     *
     * @param key
     * @return
     */
    long persist(String key);

    /**
     * 序列化
     *
     * @param key
     * @return
     */
    byte[] dump(String key);

    /**
     * 反序列化
     *
     * @param key
     * @param ttl
     * @param serializedValue
     * @return
     */
    String restore(String key, long ttl, byte[] serializedValue);

    /**
     * 清空当前库
     *
     * @return
     */
    String flushDB();

    /**
     * String类型的获取
     *
     * @param key
     * @return
     */
    String get(String key);

   String jsonGet(String bytes);


    /**
     * String类型的获取
     *
     * @param key
     * @return
     */
    byte[] get(byte[] key);

    /**
     * String类型的增加
     *
     * @param key
     * @param value
     * @return
     */
    String set(String key, String value);
    /**
     * json类型的增加
     * @param key
     * @param defualtJsonValue
     * @return
     */
    String jsonSet(String key, String defualtJsonValue);


    /**
     * String类型的增加
     *
     * @param key
     * @param value
     * @return
     */
    String set(byte[] key, byte[] value);


    /**
     * 返回各库的数量
     * value是库名,key是库号
     *
     * @return
     */
    Map<Integer, String> dbSize();

    /**
     * 切换库
     *
     * @param db 库号
     * @return
     */
    String select(int db);

    /**
     * scan 模糊查所有key
     * @param pattern 规则
     * @return
     */
    List<String> scanAll(String pattern);

    /**
     * 返回当前db
     * @return
     */
    int getDb();

    @Override
    void close() ;

    /**
     * 查询list类型元素长度
     * @param list
     * @return
     */
    long llen(String list);

    /**
     * list范围查询
     * @param list list的key
     * @param start
     * @param stop
     * @return
     */
    List<String> lrange(String list, int start, int stop);

    /**
     * list范围查询
     * @param list list的key
     * @param start
     * @param stop
     * @return
     */
    List<byte[]> lrange(byte[] list, int start, int stop);

    /**
     * 给list指定下标元素设置值
     * @param list
     * @param i
     * @param value
     * @return
     */
    String lset(byte[] list, int i, byte[] value);

    /**
     * 给list指定下标元素设置值
     * @param list
     * @param i
     * @param value
     * @return
     */
    String lset(String list, int i, String value);

    /**
     * 删除list元素
     * @param list
     * @param i 删除个数
     * @param value 被删除的值为这个
     * @return
     */
    long lrem(byte[] list, int i, byte[] value);

    /**
     * 删除list元素
     * @param list
     * @param i 删除个数
     * @param value 被删除的值为这个
     * @return
     */
    long lrem(String list, int i, String value);

    /**
     * 删除list头元素
     * @param list
     * @return
     */
    String lpop(String list);

    /**
     * 删除list尾元素
     * @param list
     * @return
     */
    String rpop(String list);

    /**
     * list头元素添加
     * @param list
     * @param value
     * @return
     */
    long lpush(String list, String value);

    /**
     * list头元素添加
     * @param list
     * @param value
     * @return
     */
    long lpush(byte[] list, byte[] value);




    /**
     * list尾元素添加
     * @param list
     * @param value
     * @return
     */
    long rpush(String list, String value);

    /**
     * list尾元素添加
     * @param list
     * @param value
     * @return
     */
    long rpush(byte[] list, byte[] value);

    /**
     * 查hash的元素数量
     * @param key
     * @return
     */
    long hlen(String key);


    /**
     * 查hash所有元素
     * @param key
     * @return
     */
    Map<byte[],byte[]> hscanAll(byte[] key);

    /**
     * 查hash所有元素
     * @param key
     * @return
     */
    Map<String,String> hscanAll(String key);

    /**
     * 给hash中设置数据,如果存在将覆盖原来的k
     * @param key
     * @param field
     * @param value
     * @return
     */
    long hset(String key, String field, String value);
    /**
     * 给hash中设置数据,如果存在将覆盖原来的k
     * @param key
     * @param field
     * @param value
     * @return
     */
    long hset(byte[] key, byte[] field, byte[] value);
    /**
     * 给hash中设置数据,不存在才能设置
     * @param key
     * @param field
     * @param value
     * @return
     */
    long hsetnx(String key, String field, String value);
    /**
     * 给hash中设置数据,不存在才能设置
     * @param key
     * @param field
     * @param value
     * @return
     */
    long hsetnx(byte[] key, byte[] field, byte[] value);

    /**
     * 删除哈数中的元素
     * @param key
     * @param field
     * @return
     */
    long hdel(byte[] key, byte[] field);

    /**
     * 删除hash中的元素
     * @param key
     * @param field
     * @return
     */
    long hdel(String key, String field);

    /**
     * 查询set的元素数量
     * @param key
     * @return
     */
    long scard(String key);

    /**
     * 删除set的元素
     * @param key
     * @param value
     * @return
     */
    long srem(String key,String value);
    /**
     * 删除set的元素
     * @param key
     * @param value
     * @return
     */
    long srem(byte[] key,byte[] value);

    /**
     * set添加元素
     * @param key
     * @param value
     * @return
     */
    long sadd(String key,String value);

    /**
     * set添加元素
     * @param key
     * @param value
     * @return
     */
    long sadd(byte[] key,byte[] value);


    /**
     * 查询set所有元素
     * @param key
     * @return
     */
    List<byte[]> sscanAll(byte[] key);

    /**
     * 查询set所有元素
     * @param key
     * @return
     */
    List<String> sscanAll(String key);

    /**
     * 对zset进行新增
     * @param key
     * @param scorem
     * @param value
     * @return
     */
    long zadd(byte[] key,double scorem,byte[] value);
    /**
     * 对zset进行新增
     * @param key
     * @param scorem
     * @param value
     * @return
     */
    long zadd(String key,double scorem,String value);

    /**
     * 对zset进行删除
     * @param key
     * @param value
     * @return
     */
    long zrem(String key,String value);

    /**
     * 对zset进行删除
     * @param key
     * @param value
     * @return
     */
    long zrem(byte[] key,byte[] value);

    /**
     * 查询zset元素个数
     * @param key
     * @return
     */
    long zcard(byte[] key);
    /**
     * 查询zset元素个数
     * @param key
     * @return
     */
    long zcard(String key);

    /**
     * 查询zset元素,并返回分数
     * @param key
     * @param start
     * @param stop
     * @return
     */
    Map<Double,String> zrangeWithScores(String key,long start, long stop);
    /**
     * 查询zset元素,并返回分数
     * @param key
     * @param start
     * @param stop
     * @return
     */
    Map<Double,byte[]> zrangeWithScores(byte[] key,long start, long stop);

    /**
     * 添加stream元素
     * @param key
     * @param id
     * @param jsonValue
     * @return
     */
    String xadd(String key, String id, String jsonValue);

    /**
     * 查询stream元素数量
     * @param key
     * @return
     */
    long xlen(String key);

    /**
     * 获取stream元素
     * @param key
     * @param start
     * @param end
     * @param total
     * @return
     */
    Map<String, String> xrevrange(String key, String start, String end, int total);

    long xdel(String key, String id);

    /**
     * redis命令监控
     * @param redisMonitor
     */
     void monitor(RedisMonitor redisMonitor);

    void psubscribe(RedisPubSub redisPubSub, String text);

    long publish(String channel, String message);

    long memoryUsage(String key, int samples);


    long strlen(String key);

    Class<?> jsonType(String key);

    long jsonObjLen(String key);

    long jsonStrLen(String key);

    long jsonArrLen(String key);
}
