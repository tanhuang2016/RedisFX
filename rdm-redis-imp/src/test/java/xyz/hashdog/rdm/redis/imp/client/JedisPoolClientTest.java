package xyz.hashdog.rdm.redis.imp.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPool;
import xyz.hashdog.rdm.common.util.FileUtil;
import xyz.hashdog.rdm.common.util.GzipUtil;
import xyz.hashdog.rdm.redis.RedisConfig;
import xyz.hashdog.rdm.redis.client.RedisClient;
import xyz.hashdog.rdm.redis.imp.Constant;
import xyz.hashdog.rdm.redis.imp.Util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/19 11:05
 */
public class JedisPoolClientTest {

    private RedisClient redisClient;

    @Before
    public void before(){
        RedisConfig redisConfig =new RedisConfig();
        redisConfig.setHost("localhost");
        redisConfig.setPort(6379);
        redisClient=new JedisPoolClient(new JedisPool(new GenericObjectPoolConfig<>(), redisConfig.getHost(), redisConfig.getPort()));
    }

    @Test
    public void isConnected(){
        boolean isConnected = redisClient.isConnected();
        System.out.println(isConnected);
    }

    @Test
    public void keys(){
        Set<String> keys = redisClient.keys("*");
        System.out.println(keys);
    }
    @Test
    public void type(){
        String type = redisClient.type("list");
        System.out.println(type);
    }

    @Test
    public void ttl(){
        long ttl = redisClient.ttl("AA");
        System.out.println(ttl);
    }
    @Test
    public void ping(){
        String ping = redisClient.ping();
        System.out.println(ping);
    }
    @Test
    public void info(){
        String info = redisClient.info();
        System.out.println(info);
    }
    @Test
    public void rename(){
        String rename = redisClient.rename("AA","AA");
        System.out.println(rename);
    }
    @Test
    public void expire(){
        long expire = redisClient.expire("AA",100000000);
        System.out.println(expire);
    }

    @Test
    public void exists(){
        boolean exists = redisClient.exists("AA");
        System.out.println(exists);
    }

    @Test
    public void del(){
        long del = redisClient.del("AA1");
        System.out.println(del);
    }

    @Test
    public void persist(){
        long persist = redisClient.persist("AA");
        System.out.println(persist);
    }
    @Test
    public void dump(){
        byte[] dump = redisClient.dump("AA");
        System.out.println(dump);
    }
    @Test
    public void restore(){
        byte[] dump = redisClient.dump("AA");

        String restore = redisClient.restore("AA2",980000l,dump);
        System.out.println(restore);
    }

    @Test
    public void flushDB (){
//        String flushDB = redisClient.flushDB();
//        System.out.println(flushDB);
    }

    @Test
    public void set (){
        String set = redisClient.set("AA","中文44ik");
        System.out.println(set);
    }

    @Test
    public void get (){
        String get = redisClient.get("AA");
        System.out.println(get);
    }

    @Test
    public void llen (){
        long list = redisClient.llen("list");
        System.out.println(list);
    }
    @Test
    public void lrange (){
        List<String> lrange = redisClient.lrange("list", 0, 50);
        System.out.println(lrange);
    }
    @Test
    public void lrange2 (){
        List<byte[]> lrange = redisClient.lrange("list".getBytes(), 0, 50);
        System.out.println(lrange);
    }

    @Test
    public void lset (){
        String lset = redisClient.lset("list".getBytes(), 7, "7".getBytes());
        System.out.println(lset);
    }
    @Test
    public void lset2 (){
        String lset = redisClient.lset("list", 7, "7");
        System.out.println(lset);
    }
    @Test
    public void lrem (){
        long lrem = redisClient.lrem("list".getBytes(), 7, "7".getBytes());
        System.out.println(lrem);
    }
    @Test
    public void lrem2 (){
        long lrem = redisClient.lrem("list", 7, "7");
        System.out.println(lrem);
    }

    @Test
    public void lpop (){
        String list = redisClient.lpop("list");
        System.out.println(list);
    }
    @Test
    public void rpop (){
        //删除尾巴
        String list2 = redisClient.rpop("list");
        System.out.println(list2);
    }
    @Test
    public void lpush (){
        long list1 = redisClient.lpush("list", "7");
        System.out.println(list1);
    }
    @Test
    public void lpush2 (){
        long list3 = redisClient.lpush("list".getBytes(), "7".getBytes());
        System.out.println(list3);
    }

    @Test
    public void rpush (){
        long list1 = redisClient.rpush("list", "7");
        System.out.println(list1);
    }
    @Test
    public void rpush2 (){
        long list3 = redisClient.rpush("list".getBytes(), "7".getBytes());
        System.out.println(list3);
    }


    @Test
    public void hlen (){
        long hlen = redisClient.hlen("hash");
        System.out.println(hlen);
    }

    @Test
    public void hscanAll (){
        System.out.println(redisClient.hscanAll("hash"));
    }
    @Test
    public void hscanAll2 (){
        System.out.println(redisClient.hscanAll("hash".getBytes()));
    }

    @Test
    public void hset (){
        System.out.println(redisClient.hset("hash","aa12","12sd"));
    }
    @Test
    public void hset2 (){
        System.out.println(redisClient.hset("hash".getBytes(),"aa12".getBytes(),"12sd0".getBytes()));
    }
    @Test
    public void hsetnx (){
        System.out.println(redisClient.hsetnx("hash","aa12","12sd"));
    }
    @Test
    public void hsetnx2 (){
        System.out.println(redisClient.hsetnx("hash".getBytes(),"aa12".getBytes(),"12sd".getBytes()));
    }

    @Test
    public void hdel (){
        System.out.println(redisClient.hdel("hash","aa12"));
    }
    @Test
    public void hdel2 (){
        System.out.println(redisClient.hdel("hash".getBytes(),"aa12".getBytes()));
    }
    @Test
    public void scard (){
        System.out.println(redisClient.scard("set"));
    }
    @Test
    public void srem (){
        System.out.println(redisClient.srem("set","aa12"));
    }
    @Test
    public void srem2 (){
        System.out.println(redisClient.srem("set".getBytes(),"aa12".getBytes()));
    }
    @Test
    public void sadd (){
        System.out.println(redisClient.sadd("set","aa12"));
    }
    @Test
    public void sadd2 (){
        System.out.println(redisClient.sadd("set".getBytes(),"aa12".getBytes()));
    }

    @Test
    public void sscanAll (){
        System.out.println(redisClient.sscanAll("set"));
    }
    @Test
    public void sscanAll2 (){
        System.out.println(redisClient.sscanAll("set".getBytes()));
    }

    @Test
    public void zadd (){
        System.out.println(redisClient.zadd("zset".getBytes(),1d,"你也好".getBytes()));
    }
    @Test
    public void zadd2 (){
        System.out.println(redisClient.zadd("zset",1d,"你也好"));
    }


    @Test
    public void zrem (){
        System.out.println(redisClient.zrem("zset".getBytes(),"你也好".getBytes()));
    }
    @Test
    public void zrem2 (){
        System.out.println(redisClient.zrem("zset","你也好"));
    }

    @Test
    public void zcard (){
        System.out.println(redisClient.zcard("zset".getBytes()));
    }
    @Test
    public void zcard2 (){
        System.out.println(redisClient.zcard("zset"));
    }
    @Test
    public void zrangeWithScores (){
        System.out.println(redisClient.zrangeWithScores("zset".getBytes(),0,50));
    }
    @Test
    public void zrangeWithScores2 (){
        System.out.println(redisClient.zrangeWithScores("zset",5,55));
    }






    @Test
    public void setTestData () throws IOException {

        String set = redisClient.set("image".getBytes(), FileUtil.file2byte("C:\\Users\\11036\\Desktop\\123.png"));
        String set2 = redisClient.set("image:big".getBytes(), FileUtil.file2byte("C:\\Users\\11036\\Desktop\\QQ录屏20230807174249.gif"));
        String set3 = redisClient.set("image:base64".getBytes(), Base64.getEncoder().encode(FileUtil.file2byte("C:\\Users\\11036\\Desktop\\123.png")));
        String set41 = redisClient.set("string:utf-8".getBytes(),"你好".getBytes(Charset.forName("utf-8")));
        String set4 = redisClient.set("string:gbk".getBytes(),"你好".getBytes(Charset.forName("gbk")));
        String set5 = redisClient.set("string:iso-8859-1".getBytes(),"你好".getBytes(StandardCharsets.ISO_8859_1));
        String set6 = redisClient.set("string:gzip:utf8".getBytes(), GzipUtil.compress("你好",StandardCharsets.UTF_8));
        String set7 = redisClient.set("string:gzip:gbk".getBytes(),GzipUtil.compress("你好",Charset.forName("gbk")));
        String set8 = redisClient.set("string:utf-16".getBytes(),"你好".getBytes(StandardCharsets.UTF_16));


        System.out.println(set);
    }




}
