package redisfx.tanh.rdm.redis.exceptions;

/**
 * redis 客户端api操作一切异常统一转化为RedisException
 * 目的是为了方便客户端进行统一处理
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 10:30
 */
public class RedisException extends RuntimeException {

    public RedisException(String message) {
        super(message);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisException(Throwable cause) {
        super(cause);
    }
}
