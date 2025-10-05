package redisfx.tanh.rdm.redis.client;

import redisfx.tanh.rdm.common.tuple.Tuple2;
import redisfx.tanh.rdm.redis.Message;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
     * @return 连接消息
     */
    Message testConnect();
    /**
     * 判断是否连接
     *
     * @return 是否连接
     */
    boolean isConnected();

    /**
     * keys 模糊查新的命令
     *
     * @param pattern 规则
     * @return key集合
     */
    Set<String> keys(String pattern);

    /**
     * 获取key的类型
     *
     * @param key  key
     * @return key类型
     */
    String type(String key);

    /**
     * key的存活时间是多少秒
     *
     */
    long ttl(String key);

    /**
     *
     * @return  ping
     */
    String ping();

    /**
     * redis信息
     */
    String info();

    /**
     * key 重命名
     *
     */
    String rename(String oldKey, String newKey);

    /**
     * 控制台交互器
     *
     */
    RedisConsole getRedisConsole();

    /**
     * 设置key的时长
     *
     */
    long expire(String key, long seconds);

    /**
     * key是否存在
     */
    boolean exists(String key);

    /**
     * 删除key
     *
     */
    long del(String... key);

    /**
     * 设置key永不过期
     *
     */
    long persist(String key);

    /**
     * 序列化
     *
     */
    byte[] dump(String key);

    /**
     * 反序列化
     *
     */
    String restore(String key, long ttl, byte[] serializedValue);

    /**
     * 清空当前库
     *
     */
    String flushDB();

    /**
     * String类型的获取
     *
     */
    String get(String key);

   String jsonGet(String bytes);


    /**
     * String类型的获取
     *
     */
    byte[] get(byte[] key);

    /**
     * String类型的增加
     */
    String set(String key, String value);
    /**
     * json类型的增加
     */
    String jsonSet(String key, String defaultJsonValue);


    /**
     * String类型的增加
     */
    String set(byte[] key, byte[] value);


    /**
     * 返回各库的数量
     * value是库名,key是库号
     */
    Map<Integer, Integer> dbSize();

    /**
     * 切换库
     *
     * @param db 库号
     */
    String select(int db);

    /**
     * scan 模糊查所有key
     */
    @Deprecated
    List<String> scanAll(String pattern);
    /**
     * scan 模糊查所有key
     */
    Tuple2<List<String>,List<String>> scan(String pattern,List<String>cursors,int count,String type,boolean isLike);

    RedisKeyScanner getRedisKeyScanner();

    /**
     * 返回当前db
     */
    int getDb();

    @Override
    void close() ;

    /**
     * 查询list类型元素长度
     */
    long llen(String key);

    /**
     * list范围查询
     */
    List<String> lrange(String key, int start, int stop);

    /**
     * list范围查询
     */
    List<byte[]> lrange(byte[] key, int start, int stop);

    /**
     * 给list指定下标元素设置值
     */
    String lset(byte[] list, int i, byte[] value);

    /**
     * 给list指定下标元素设置值
     */
    String lset(String key, int i, String value);

    /**
     * 删除list元素
     * @param i 删除个数
     * @param value 被删除的值为这个
     */
    long lrem(byte[] key, int i, byte[] value);

    /**
     * 删除list元素
     * @param i 删除个数
     * @param value 被删除的值为这个
     */
    long lrem(String key, int i, String value);

    /**
     * 删除list头元素
     */
    String lpop(String key);

    /**
     * 删除list尾元素
     */
    String rpop(String key);

    /**
     * list头元素添加
     */
    long lpush(String key, String value);

    /**
     * list头元素添加
     */
    long lpush(byte[] key, byte[] value);




    /**
     * list尾元素添加
     */
    long rpush(String key, String value);

    /**
     * list尾元素添加
     */
    long rpush(byte[] key, byte[] value);

    /**
     * 查hash的元素数量
     */
    long hlen(String key);


    /**
     * 查hash所有元素
     */
    Map<byte[],byte[]> hscanAll(byte[] key);

    /**
     * 查hash所有元素
     */
    Map<String,String> hscanAll(String key);

    /**
     * 给hash中设置数据,如果存在将覆盖原来的k
     */
    long hset(String key, String field, String value);
    /**
     * 给hash中设置数据,如果存在将覆盖原来的k
     */
    long hset(byte[] key, byte[] field, byte[] value);
    /**
     * 给hash中设置数据,不存在才能设置
     */
    long hsetnx(String key, String field, String value);
    /**
     * 给hash中设置数据,不存在才能设置
     */
    long hsetnx(byte[] key, byte[] field, byte[] value);

    /**
     * 删除哈数中的元素
     */
    long hdel(byte[] key, byte[] field);

    /**
     * 删除hash中的元素
     */
    long hdel(String key, String field);

    /**
     * 查询set的元素数量
     */
    long scard(String key);

    /**
     * 删除set的元素
     */
    long srem(String key,String value);
    /**
     * 删除set的元素
     */
    long srem(byte[] key,byte[] value);

    /**
     * set添加元素
     */
    long sadd(String key,String value);

    /**
     * set添加元素
     */
    long sadd(byte[] key,byte[] value);


    /**
     * 查询set所有元素
     */
    List<byte[]> sscanAll(byte[] key);

    /**
     * 查询set所有元素
     */
    List<String> sscanAll(String key);

    /**
     * 对zset进行新增
     */
    long zadd(byte[] key,double score,byte[] value);
    /**
     * 对zset进行新增
     */
    long zadd(String key,double score,String value);

    /**
     * 对zset进行删除
     */
    long zrem(String key,String value);

    /**
     * 对zset进行删除
     */
    long zrem(byte[] key,byte[] value);

    /**
     * 查询zset元素个数
     */
    long zcard(byte[] key);
    /**
     * 查询zset元素个数
     */
    long zcard(String key);

    /**
     * 查询zset元素,并返回分数
     */
    Map<Double,String> zrangeWithScores(String key,long start, long stop);
    /**
     * 查询zset元素,并返回分数
     */
    Map<Double,byte[]> zrangeWithScores(byte[] key,long start, long stop);

    /**
     * 添加stream元素
     */
    String xadd(String key, String id, String jsonValue);

    /**
     * 查询stream元素数量
     */
    long xlen(String key);

    /**
     * 获取stream元素
     */
    Map<String, String> xrevrange(String key, String start, String end, int total);

    /**
     * 删除stream元素
     */
    long xdel(String key, String id);

    /**
     * redis命令监控
     * @param redisMonitor 监控对象
     */
     void monitor(RedisMonitor redisMonitor);

     /**
     * 订阅redis命令
     * 该订阅，要取消只能靠中断线程，现在改用subscriber
     * @param redisPubSub 订阅对象
     * @param text 订阅命令
     */
    void psubscribe(RedisPubSub redisPubSub, String text);

    /**
     * 获取订阅者
     * @return 订阅者
     */
    RedisSubscriber subscriber();

    /**
     * 发布redis命令
     * @param channel 发布频道
     * @param message 发布内容
     * @return 发布数量
     */
    long publish(String channel, String message);

    /**
     * 获取内存使用情况
     * @param key key
     * @param samples 采样数
     * @return 内存使用情况
     */
    long memoryUsage(String key, int samples);


    /**
     * 获取字符串长度
     * @param key key
     * @return 字符串长度
     */
    long strlen(String key);

    /**
     * 获取json类型
     * @param key key
     * @return json类型
     */
    Class<?> jsonType(String key);

    /**
     * 获取json对象长度
     * @param key key
     * @return json对象长度
     */
    long jsonObjLen(String key);

    /**
     * 获取json字符串长度
     * @param key key
     * @return json字符串长度
     */
    long jsonStrLen(String key);

    /**
     * 获取json数组长度
     * @param key key
     * @return json数组长度
     */
    long jsonArrLen(String key);


    /**
     * 执行Pipeline命令
     * @param pipelineExecutor Pipeline命令执行器
     * @return 执行结果列表
     */
    List<Object> executePipelined(Consumer<PipelineAdapter> pipelineExecutor,int db);

}
