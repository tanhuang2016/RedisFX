package xyz.hashdog.rdm.redis.imp.console;

import java.net.Socket;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 21:35
 */
@FunctionalInterface
public interface SocketAcquirer {
    /**
     * 获取套接字,每次都是拿去当前客户端最新的
     * @return 套接字
     */
    Socket getSocket();
}
