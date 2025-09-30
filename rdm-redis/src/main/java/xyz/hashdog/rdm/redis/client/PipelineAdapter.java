package xyz.hashdog.rdm.redis.client;

/**
 * redis客户端管道操作
 * 支持的管道命令都在这儿
 *
 * @author th
 * @version 2.3.2
 * @since 2025/9/3 23:03
 */
public interface PipelineAdapter {
    void memoryUsage(String key, int i);

    void type(String key);

    void ttl(String key);

    void strlen(String key);

    void llen(String key);

    void hlen(String key);

    void scard(String key);

    void zcard(String key);

    void xlen(String key);


    void jsonObjLen(String key);

    void jsonStrLen(String key);

    void jsonArrLen(String key);

    void defaultValue(Object v);

    void dump(String key);

    void pttl(String key);
}
