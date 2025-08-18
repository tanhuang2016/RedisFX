package xyz.hashdog.rdm.redis.client;


/**
 * 命令监控功能，封装消息获取
 * 命令监控功能
 * @author th
 * @version 2.0.1
 * @since 2025/7/30 22:48
 */
@FunctionalInterface
public interface RedisMonitor {

     /**
      * 命令监控功能
      * @param msg 命令消息
      */
     void onCommand(String msg);
}
