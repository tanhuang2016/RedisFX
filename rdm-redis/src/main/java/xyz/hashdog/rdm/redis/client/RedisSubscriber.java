package xyz.hashdog.rdm.redis.client;

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
     * 订阅的线程
     */
    protected volatile Thread subscribeThread;
    /**
     * 是否订阅中
     */
    protected final AtomicBoolean isSub = new AtomicBoolean(false);

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
        this.subscribeThread = new Thread(() -> {
            isSub.set(true);
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
        isSub.set(false);
        if (subscribeThread != null) {
            subscribeThread.interrupt();
        }
    }
}
