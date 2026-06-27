package redisfx.tanh.rdm.redis.imp.util;

import org.junit.Test;

import javax.net.ssl.SSLSocketFactory;

public class UtilTest {

    @Test
    public void test(){
        SSLSocketFactory socketFactory = Util.getSocketFactory("E:\\ha\\BF\\compose\\bitnami_redis_ssl\\certs\\ca.crt", "E:\\ha\\BF\\compose\\bitnami_redis_ssl\\certs\\redis.crt", "E:\\ha\\BF\\compose\\bitnami_redis_ssl\\certs\\redis.key", "redis123");
        System.out.println(socketFactory);
    }
}
