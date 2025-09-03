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
}
