package xyz.hashdog.rdm.redis.client;


import xyz.hashdog.rdm.common.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 命令监控功能，封装消息获取
 * 命令监控功能
 * @author th
 * @version 2.0.1
 * @since 2025/7/30 22:48
 */
public abstract class RedisMonitor {
     private  List<AutoCloseable> jedisList;

     public RedisMonitor() {

     }

     public void close() {
          for (AutoCloseable autoCloseable : jedisList) {
               Util.close(autoCloseable);
          }
     }

     public void addJedis(AutoCloseable jedis) {
          if(jedisList==null){
               //一般中等规模12个节点
               this.jedisList = new ArrayList<>(12);
          }
          jedisList.add(jedis);
     }

     /**
      * 命令监控功能
      * @param msg 命令消息
      */
     public abstract void onCommand(String msg);
}
