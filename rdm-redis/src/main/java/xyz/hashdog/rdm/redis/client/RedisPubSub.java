package xyz.hashdog.rdm.redis.client;


/**
 * 发布订阅消息获取
 * @author th
 * @version 2.1.1
 * @since 2025/7/30 22:48
 */
@FunctionalInterface
public interface RedisPubSub {

     /**
      * 获取消息
      * @param channel 频道
      * @param msg 消息
      */
     void onMessage(String channel,String msg);
}
