package redisfx.tanh.rdm.ui.handler;

import redisfx.tanh.rdm.redis.Message;
import redisfx.tanh.rdm.redis.client.RedisClient;

/**
 * 新增key的处理
 *
 * @author th
 * @version 1.0.0
 * @since 2023/8/12 13:28
 */
public interface NewKeyHandler {

    /**
     * 新增key
     *
     * @param redisClient 客户端啊
     * @param db          数据库号
     * @param key         key名称
     * @param ttl         有效期
     * @return
     */
    Message newKey(RedisClient redisClient, int db, String key, long ttl);
}
