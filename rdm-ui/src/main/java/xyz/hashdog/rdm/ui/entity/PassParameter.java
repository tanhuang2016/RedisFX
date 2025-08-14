package xyz.hashdog.rdm.ui.entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import xyz.hashdog.rdm.redis.RedisContext;
import xyz.hashdog.rdm.redis.client.RedisClient;

/**
 * 参数传递
 * 父向子传递
 * @author th
 * @version 1.0.0
 * @since 2023/7/23 22:28
 */
public class PassParameter {
    public static final int NONE = 1;
    public static final int REDIS = 2;
    public final  static int CONSOLE=3;
    public final  static int STRING=4;
    public final  static int HASH=5;
    public final  static int LIST=6;
    public static final int SET = 7;
    public static final int ZSET = 8;
    public static final int JSON = 9;
    public static final int STREAM = 10;
    public final  static int MONITOR=11;
    public final  static int PUBSUB=12;

    private int tabType;
    private int db;
    private final StringProperty key=new SimpleStringProperty();
    private String keyType;
    private RedisContext redisContext;
    private RedisClient redisClient;


    public PassParameter(int tabType) {
        this.tabType = tabType;
    }


    public RedisContext getRedisContext() {
        return redisContext;
    }

    public void setRedisContext(RedisContext redisContext) {
        this.redisContext = redisContext;
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public int getTabType() {
        return tabType;
    }

    public void setTabType(int tabType) {
        this.tabType = tabType;
    }

    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
}
