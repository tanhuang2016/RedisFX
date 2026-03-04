package redisfx.tanh.rdm.redis.client;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
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
    public final ScannerResult scan(){
        ScannerResult result = doScan();
        sum+=result.keys.size();
        return result;
    }
    public final List<String> scanFirst(){
        RedisKeyScanner.ScannerResult result = this.scan();
        List<String> keys = new ArrayList<>(result.getKeys());
        while (!result.isEnd()&&keys.size()<count){
            result = this.scan();
            keys.addAll(result.getKeys());
        }
        return keys;
    }
    public abstract ScannerResult doScan();

    public int getSum() {
        return sum;
    }

    public RedisKeyScanner setCount(int count) {
        this.count = count;
        return this;
    }
    @Getter
    @AllArgsConstructor
    public static class ScannerResult{
        public static final String END="-1";
        private List<String> cursor;
        private List<String> keys;

        public boolean isEnd(){
            for (String s : cursor) {
                if(!END.equals(s)){
                    return false;
                }
            }
            return true;
        }

    }
}
