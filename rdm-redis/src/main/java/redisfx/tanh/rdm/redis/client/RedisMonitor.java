package redisfx.tanh.rdm.redis.client;


import redisfx.tanh.rdm.common.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 命令监控功能，封装消息获取
 * 命令监控功能
 * @author th
 * @version 2.0.1
 * @since 2025/7/30 22:48
 */
public abstract class RedisMonitor implements AutoCloseable {
     /**
      * jedis列表
      * 集群的时候，需要多个节点单独进行监控
      * 存在这里，用于监控关闭的时候释放连接
      */
     private  List<AutoCloseable> jedisList;
     private boolean closed;

     public RedisMonitor() {

     }

     /**
      * 关闭
      */
     @Override
     public void close() {
          if(jedisList==null){
               return;
          }
          closed=true;
          for (AutoCloseable autoCloseable : jedisList) {
               Util.close(autoCloseable);
          }

     }

     /**
      * 添加jedis
      * @param jedis jedis
      */
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

     public boolean isClosed() {
          return closed;
     }
}
