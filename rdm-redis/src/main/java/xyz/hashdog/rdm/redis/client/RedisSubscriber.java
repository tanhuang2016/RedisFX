package xyz.hashdog.rdm.redis.client;

import xyz.hashdog.rdm.common.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 订阅者
 *
 * @author th
 */
public abstract class RedisSubscriber {
    /**
     * 订阅的频道
     */
    protected String text;
    /**
     * 订阅的回调
     */
    protected RedisPubSub redisPubSub;

    /**
     * 缓存需要关闭的资源
     */
    private List<AutoCloseable> jedisList;

    /**
     * 是否订阅中
     */
    protected final AtomicBoolean closed = new AtomicBoolean(false);

    public RedisSubscriber() {

    }


    public RedisSubscriber text(String text) {
        this.text = text;
        return this;
    }

    public RedisSubscriber redisPubSub(RedisPubSub redisPubSub) {
        this.redisPubSub = redisPubSub;
        return this;
    }

    /**
     * 订阅
     */
    public void subscribe() {
       Thread subscribeThread = new Thread(() -> {
           closed.set(false);
            doSubscribe();
        });
        subscribeThread.setDaemon(true);
        subscribeThread.start();
    }

    /**
     * 订阅逻辑
     */
    public abstract void doSubscribe();


    /**
     * 取消订阅
     */
    public void unsubscribe() {
        closed.set(true);
        if(jedisList!=null){
            for (AutoCloseable autoCloseable : jedisList) {
                Util.close(autoCloseable);
            }
            jedisList.clear();
        }
    }
    /**
     * 添加jedis
     * @param jedis jedis
     */
    public void addJedis(AutoCloseable jedis) {
        if(jedisList==null){
            this.jedisList = new ArrayList<>(2);
        }
        jedisList.add(jedis);
    }
}
