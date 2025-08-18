package xyz.hashdog.rdm.redis.imp.client;

import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.commands.SortedSetCommands;
import redis.clients.jedis.commands.StreamCommands;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.StreamEntry;
import redis.clients.jedis.resps.Tuple;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.redis.imp.Util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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
    protected final Map<byte[],byte[]> hscanAll(byte[] key, BiFunction<byte[],ScanParams, ScanResult<Map.Entry<byte[],byte[]>>> function) {
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

    /**
     * 封装hscanAll获取hash所有键值对
     * @param key 键
     * @param function 由于子类客户端没有同一个父类，所以需要传入一个函数封装查询逻辑
     * @return 所有键值对
     */
    protected final Map<String,String> hscanAll(String key, BiFunction<String,ScanParams, ScanResult<Map.Entry<String,String>>> function) {
        Map<String,String> map = new LinkedHashMap<>();
        // 定义SCAN命令参数，匹配所有键
        ScanParams scanParams = new ScanParams();
        scanParams.count(5000);
        // 开始SCAN迭代
        String cursor = "0";
        do {
            ScanResult<Map.Entry<String, String>> scanResult =  function.apply(cursor,scanParams);
            for (Map.Entry<String, String> entry : scanResult.getResult()) {
                map.put(entry.getKey(),entry.getValue());
            }
            cursor = scanResult.getCursor();
        } while (!"0".equals(cursor));
        return map;
    }

    /**
     * 封装scanAll获取所有键
     * @param pattern key模糊查询条件
     * @param function 由于子类客户端查询逻辑不一样，所以需要传入一个函数封装查询逻辑
     * @return 所有键
     */
    protected final List<String> scanAll(String pattern, BiFunction<String,ScanParams, ScanResult<String>> function) {
        List<String> keys = new ArrayList<>();
        // 定义SCAN命令参数，匹配所有键
        ScanParams scanParams = new ScanParams();
        scanParams.count(5000);
        if(DataUtil.isNotBlank(pattern)){
            scanParams.match(pattern);
        }
        // 开始SCAN迭代
        String cursor = "0";
        do {
            ScanResult<String> scanResult = function.apply(cursor, scanParams);
            keys.addAll(scanResult.getResult());
            cursor = scanResult.getCursor();
        } while (!"0".equals(cursor));
        return keys;
    }

    public List<String> sscanAll(String key,BiFunction<String,ScanParams, ScanResult<String>> function) {
        List<String> res = new ArrayList<>();
        // 定义SSCAN命令参数，匹配所有键
        ScanParams scanParams = new ScanParams();
        scanParams.count(5000);
        // 开始SCAN迭代
        String cursor = "0";
        do {
            ScanResult<String> scanResult = function.apply(cursor, scanParams);
            res.addAll(scanResult.getResult());
            cursor = scanResult.getCursor();
        } while (!"0".equals(cursor));
        return res;
    }

    public List<byte[]> sscanAll(byte[] key,BiFunction<byte[],ScanParams, ScanResult<byte[]>> function) {
        List<byte[]> ress = new ArrayList<>();
        // 定义SSCAN命令参数，匹配所有键
        ScanParams scanParams = new ScanParams();
        scanParams.count(5000);
        // 开始SCAN迭代
        String cursor = "0";
        do {
            ScanResult<byte[]> scanResult = function.apply(cursor.getBytes(), scanParams);
            ress.addAll(scanResult.getResult());
            cursor = scanResult.getCursor();
        } while (!"0".equals(cursor));
        return ress;
    }
    public String xadd(StreamCommands jedis, String key, String id, String jsonValue) {
        Map<String, String> map = Util.json2MapString(jsonValue);
        StreamEntryID seid;
        if(StreamEntryID.NEW_ENTRY.toString().equals(id)){
            seid = StreamEntryID.NEW_ENTRY;
        }else {
            seid = new StreamEntryID(id);
        }
        return   jedis.xadd(key, seid , map).toString();
    }

    public Map<String, String> xrevrange(StreamCommands jedis,String key, String start, String end, int total) {
        Map<String,String> map = new LinkedHashMap<>();
        for (StreamEntry streamEntry : jedis.xrevrange(key, start, end, total)) {
            Map<String, String> fields = streamEntry.getFields();
            String jsonValue =Util.obj2Json(fields);
            map.put(streamEntry.getID().toString(),jsonValue);
        }
        return map;
    }

    public Map<Double,String> zrangeWithScores(SortedSetCommands jedis, String key, long start, long stop) {
        List<Tuple> tuples = jedis.zrangeWithScores(key, start, stop);
        Map<Double,String> map = new LinkedHashMap<>();
        tuples.forEach(e->map.put(e.getScore(),e.getElement()));
        return map;
    }

}
