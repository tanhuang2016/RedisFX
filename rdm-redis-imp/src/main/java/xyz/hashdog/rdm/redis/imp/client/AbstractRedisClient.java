package xyz.hashdog.rdm.redis.imp.client;

import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import xyz.hashdog.rdm.common.function.TriFunction;
import xyz.hashdog.rdm.redis.client.RedisClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 封装通用方法
 * @author th
 * @version 2.2.1
 * @since 2025/8/17 17:05
 */
public abstract class AbstractRedisClient implements RedisClient {

    /**
     * 封装hscanAll获取hash所有键值对
     * @param function 由于子类客户端没有同一个父类，所以需要传入一个函数封装查询逻辑
     * @return 所有键值对
     */
    public Map<byte[],byte[]> hscanAll( BiFunction<byte[],ScanParams, ScanResult<Map.Entry<byte[],byte[]>>> function) {
        Map<byte[],byte[]> map = new LinkedHashMap<>();
        // 定义SCAN命令参数，匹配所有键
        ScanParams scanParams = new ScanParams();
        scanParams.count(5000);
        // 开始SCAN迭代
        String cursor = "0";
        do {
            ScanResult<Map.Entry<byte[],byte[]>> scanResult = function.apply(cursor.getBytes(),scanParams);
            for (Map.Entry<byte[],byte[]> entry : scanResult.getResult()) {
                map.put(entry.getKey(),entry.getValue());
            }
            cursor = scanResult.getCursor();
        } while (!"0".equals(cursor));
        return map;
    }
}
