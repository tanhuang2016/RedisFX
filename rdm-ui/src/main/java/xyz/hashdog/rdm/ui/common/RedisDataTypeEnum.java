package xyz.hashdog.rdm.ui.common;

import xyz.hashdog.rdm.redis.Message;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.ui.entity.PassParameter;
import xyz.hashdog.rdm.ui.exceptions.GeneralException;
import xyz.hashdog.rdm.ui.handler.NewKeyHandler;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/23 0:42
 */
public enum RedisDataTypeEnum {
    STRING("String","/fxml/StringTypeView.fxml", PassParameter.STRING,((redisClient, db, key, ttl) -> {
        checkDB(redisClient,db);
        redisClient.set(key, Applications.DEFAULT_VALUE);
        checkTTL(redisClient,ttl,key);
        return new Message(true);
    }),KeyTypeTagEnum.STRING),
    LIST("List","/fxml/ListTypeView.fxml", PassParameter.LIST,((redisClient, db, key, ttl) -> {
        checkDB(redisClient,db);
        redisClient.lpush(key, Applications.DEFAULT_VALUE);
        checkTTL(redisClient,ttl,key);
        return new Message(true);
    }),KeyTypeTagEnum.LIST),
    HASH("Hash","/fxml/HashTypeView.fxml", PassParameter.HASH,((redisClient, db, key, ttl) -> {
        checkDB(redisClient,db);
        redisClient.hsetnx(key, Applications.DEFAULT_VALUE,Applications.DEFAULT_VALUE);
        checkTTL(redisClient,ttl,key);
        return new Message(true);
    }),KeyTypeTagEnum.HASH),
    SET("Set","/fxml/SetTypeView.fxml", PassParameter.SET,((redisClient, db, key, ttl) -> {
        checkDB(redisClient,db);
        redisClient.sadd(key, Applications.DEFAULT_VALUE);
        checkTTL(redisClient,ttl,key);
        return new Message(true);
    }),KeyTypeTagEnum.SET),
    ZSET("Zset","/fxml/ZsetTypeView.fxml", PassParameter.ZSET,((redisClient, db, key, ttl) -> {
        checkDB(redisClient,db);
        redisClient.zadd(key,0, Applications.DEFAULT_VALUE);
        checkTTL(redisClient,ttl,key);
        return new Message(true);
    }),KeyTypeTagEnum.ZSET),
    JSON("ReJSON-RL","/fxml/JsonTypeView.fxml", PassParameter.JSON,((redisClient, db, key, ttl) -> {
        checkDB(redisClient,db);
        redisClient.jsonSet(key,Applications.DEFAULT_JSON_VALUE);
        checkTTL(redisClient,ttl,key);
        return new Message(true);
    }),KeyTypeTagEnum.JSON),
    STREAM("Stream","/fxml/StreamTypeView.fxml", PassParameter.STREAM,((redisClient, db, key, ttl) -> {
        checkDB(redisClient,db);
        redisClient.xadd(key,"*", Applications.DEFAULT_JSON_VALUE);
        checkTTL(redisClient,ttl,key);
        return new Message(true);
    }),KeyTypeTagEnum.STREAM),
    ;


    /**
     * 根据类型获取下标
     * @param type  类型
     * @return 下标
     */
    public static int getIndex(String type) {
        for (int i = 0; i < RedisDataTypeEnum.values().length; i++) {
            if (RedisDataTypeEnum.values()[i].type.equalsIgnoreCase(type)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 检查设置有效期
     * @param redisClient redisClient
     * @param ttl ttl
     * @param key  key
     */
    private static void checkTTL(RedisClient redisClient, long ttl, String key) {
        if(ttl>0){
            redisClient.expire(key,ttl);
        }
    }


    /**
     *检查设置db
     * @param redisClient redisClient
     * @param db  db
     */
    private static void checkDB(RedisClient redisClient, int db) {
        if(redisClient.getDb()!=db){
            redisClient.select(db);
        }
    }


    public final String type;
    public final String fxml;
    public final int tabType;
    public final NewKeyHandler newKeyHandler;
    public final KeyTypeTagEnum tagEnum;
    RedisDataTypeEnum(String type,String fxml,int tabType,NewKeyHandler newKeyHandler,KeyTypeTagEnum keyTypeTagEnum) {
        this.type=type;
        this.fxml=fxml;
        this.tabType=tabType;
        this.newKeyHandler=newKeyHandler;
        this.tagEnum=keyTypeTagEnum;
    }

    /**
     * 根据类型字符串获取
     * @param type 类型字符串
     * @return RedisDataTypeEnum
     */
    public static RedisDataTypeEnum getByType(String type) {
        //key不存在，返回的是none
        if("none".equals(type)){
            throw new GeneralException("Key with this name does not exist.");
        }
        for (RedisDataTypeEnum value : values()) {
            if(value.type.equalsIgnoreCase(type)){
                return value;
            }
        }
        throw new GeneralException("This type is not supported "+type);
    }
}
