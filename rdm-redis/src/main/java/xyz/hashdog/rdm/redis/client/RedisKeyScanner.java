package xyz.hashdog.rdm.redis.client;

import xyz.hashdog.rdm.common.tuple.Tuple2;

import java.util.List;

/**
 * redis key查询器
 *
 * @author th
 * @version 2.2.2
 * @since 2025/8/18 23:03
 */
public abstract class RedisKeyScanner {

    protected String pattern;
    protected int count;
    protected String type;
    protected boolean isLike;
    protected String cursor;
    /**
     * 查询总数统计
     */
    private int sum;

    protected RedisKeyScanner() {

    }

    public RedisKeyScanner init (String pattern, int count, String type, boolean isLike){
        this.pattern=pattern;
        this.count=count;
        this.type=type;
        this.isLike=isLike;
        this.cursor="0";
        this.sum=0;
        return this;
    }
    public final List<String> scan(){
        List<String> keys = doScan();
        sum+=keys.size();
        return keys;
    }
    public abstract List<String> doScan();

    public int getSum() {
        return sum;
    }
}
