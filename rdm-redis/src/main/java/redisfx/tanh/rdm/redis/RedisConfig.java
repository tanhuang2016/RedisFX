package redisfx.tanh.rdm.redis;

import lombok.Data;

import java.util.Objects;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 10:49
 */
@Data
public class RedisConfig {

    private String id;

    /**
     * 地址
     */
    private String host;
    /**
     * 名称
     */
    private String name;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 授权/哨兵密码
     */
    private String auth;
    private String userName;
    /**
     * 主节点密码
     */
    private String masterAuth;
    /**
     * 是否集群模式
     */
    private Boolean cluster;
    /**
     * 是否哨兵模式
     */
    private Boolean sentinel;
    /**
     * 主节点名称
     */
    private String masterName;

    /**
     * 是否ssl
     */
    private Boolean ssl;
    /**
     * ca证书
     */
    private String caCrt;
    /**
     * 服务端证书
     */
    private String redisCrt;
    /**
     * 私钥文件
     */
    private String redisKey;
    /**
     * 私钥密码
     */
    private String redisKeyPassword;

    /**
     * 是否ssh
     */
    private Boolean ssh;
    /**
     * ssh主机
     */
    private String sshHost;
    /**
     * ssh端口
     */
    private Integer sshPort;
    /**
     * ssh用户名
     */
    private String sshUserName;
    /**
     * ssh密码
     */
    private String sshPassword;
    /**
     * 私钥文件
     */
    private String sshPrivateKey;
    /**
     * 私钥密码
     */
    private String sshPassphrase;
    /**
     * 连接超时
     */
    private Integer connectionTimeout;
    /**
     * 读超时
     */
    private Integer soTimeout;
    /**
     * key 分隔符
     */
    private String keySeparator;

    private Boolean keySeparatorRegex;
    /**
     * 树结构显示
     */
    private Boolean treeShow;

    public RedisConfig() {
    }


    public Boolean getKeySeparatorRegex() {
        return Objects.requireNonNullElse(keySeparatorRegex, false);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedisConfig that = (RedisConfig) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public boolean isSentinel() {
        return Objects.requireNonNullElse(getSentinel(), false);
    }

    public boolean isSsl() {
        return Objects.requireNonNullElse(getSsl(), false);
    }

    public boolean isCluster() {
        return Objects.requireNonNullElse(getCluster(), false);
    }

    public boolean isSsh() {
        return Objects.requireNonNullElse(getSsh(), false);
    }

    public boolean isTreeShow() {
        return Objects.requireNonNullElse(getTreeShow(), false);
    }
}
